package pw.byakuren.discordplugin

import java.awt.Color
import java.util.logging.Logger

import javax.security.auth.login.LoginException
import net.dv8tion.jda.api.entities.{Member, Message, TextChannel, VoiceChannel}
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.voice.{GuildVoiceJoinEvent, GuildVoiceLeaveEvent, GuildVoiceMoveEvent}
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.{EmbedBuilder, JDA, JDABuilder}
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.{AsyncPlayerChatEvent, PlayerLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.{Bukkit, ChatColor}
import pw.byakuren.discordplugin.commands.{ChannelCommand, LinkCommand, TestCommand}
import pw.byakuren.discordplugin.contexts.DiscordContext
import pw.byakuren.discordplugin.link.LinkUserFactory

import scala.collection.JavaConverters._

class DiscordConnection(plugin: JavaPlugin, config: FileConfiguration, logger: Logger) extends ListenerAdapter with Listener {

  var enabled = true

  val jda: JDA = init()

  val CHANNEL_ID:String = config.getString("channel")
  val prefix:String = config.getString("bot-prefix")
  jda.awaitReady()
  val SELF_USER_ID:Long = jda.getSelfUser.getIdLong
  var channel:TextChannel = jda.getTextChannelById(CHANNEL_ID)

  def init() : JDA = {
    try {
      Bukkit.getServer.getPluginManager.registerEvents(this, plugin)
      new JDABuilder(config.getString("token")).addEventListeners(this).build()
    } catch {
      case _: LoginException =>
        logger.warning("Failed to load discord - Invalid bot token")
        throw new RuntimeException("Failed to load DiscordPlugin - invalid token")
    }
  }

  def sendMessageToDiscord(str: String) : Unit = {
    if (enabled)
        channel.sendMessage(str).queue()
  }

  def sendMessageToBukkit(name: String, msg: String, img_count: Int) : Unit = {
    if (enabled) {
      val img_str = if (img_count > 0) s"${ChatColor.DARK_PURPLE}[File${if (img_count==1) "" else s"×${img_count}"}] ${ChatColor.RESET}" else ""
      Bukkit.broadcastMessage(s"{$name} $img_str$msg")
    }
  }

  def sendMessageToBukkit(msg: Message) : Unit = {
    sendMessageToBukkit(msg.getAuthor.getName, pingHighlight(msg), msg.getAttachments.size())
  }

  def alertUserConnectionChange(usr: String, joined: Boolean) : Unit = {
    val eb = new EmbedBuilder
    eb.setColor(Color.YELLOW)
    eb.setTitle(s"$usr ${if (joined) "joined" else "left"} the game")
    channel.sendMessage(eb.build()).queue()
  }

  def alertDeath(msg: String) : Unit = {
    val eb = new EmbedBuilder
    eb.setColor(Color.RED)
    eb.setTitle(msg)
    channel.sendMessage(eb.build()).queue()
  }

  def alertVCUpdate(user: Member, channel: VoiceChannel, joined: Boolean): Unit = {
    Bukkit.broadcastMessage(s"${ChatColor.GRAY}${ChatColor.ITALIC}" +
      s"${user.getUser.getName} ${if (joined) "joined" else "left"} voice channel #${channel.getName}")
  }

  /* Discord Listener Events */
  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    val msg = event.getMessage
    if (msg.getChannel.getId != CHANNEL_ID || msg.getAuthor.isBot) return
    if (msg.getContentRaw.startsWith(prefix)) {
      executeCommand(msg)
      return
    }
    sendMessageToBukkit(msg)
  }


  override def onGuildVoiceJoin(event: GuildVoiceJoinEvent): Unit = {
    alertVCUpdate(event.getMember, event.getChannelJoined, joined = true)
  }

  override def onGuildVoiceLeave(event: GuildVoiceLeaveEvent): Unit = {
    alertVCUpdate(event.getMember, event.getChannelLeft, joined = false)
  }

  override def onReady(event: ReadyEvent): Unit = {
    CommandRegistry register TestCommand
    CommandRegistry register LinkCommand
    CommandRegistry register ChannelCommand
    logger.info(s"Loaded ${CommandRegistry.size} commands.")
  }

  /* Discord-Specific Events */

  def executeCommand(msg: Message): Unit = {
    val argsWithCommand: Array[String] = msg.getContentRaw.substring(prefix.length).split(" ")
    val parsed = argsWithCommand{0}
    val cmdOption = CommandRegistry.getCmd(parsed)
    cmdOption match {
      case Some(cmd) => cmd.run(LinkUserFactory.fromMember(msg.getMember),
        argsWithCommand.slice(1, argsWithCommand.length),
        new DiscordContext(msg.getMember, msg.getTextChannel, msg, config))
      case None => msg.getChannel.sendMessage(s"Command `$parsed` not found").queue()
    }
  }

  /* Bukkit Listener Events */

  @EventHandler
  def onLogin(event: PlayerLoginEvent): Unit = {
    alertUserConnectionChange(event.getPlayer.getName, joined = true)
  }

  @EventHandler
  def onLogout(event: PlayerQuitEvent): Unit = {
    alertUserConnectionChange(event.getPlayer.getName, joined = false)
  }

  @EventHandler
  def onMessage(event: AsyncPlayerChatEvent): Unit = {
    sendMessageToDiscord(s"<${event.getPlayer.getName}> ${event.getMessage}")
  }

  @EventHandler
  def onDeath(event: PlayerDeathEvent): Unit = {
    alertDeath(event.getDeathMessage)
  }

  /* Plugin handling methods */
  def enable(): Unit = enabled = true
  def disable(): Unit = enabled = false

  def shutdown(): Unit = jda.shutdownNow()

  /* Utility methods */
  def pingHighlight(m: Message): String = {
    var f = m.getContentDisplay
    for (u <- m.getMentionedMembers.asScala) {
      println("x")
      f = f.replace(s"@${u.getEffectiveName}",
        s"${ChatColor.BLUE}@${u.getUser.getName}${ChatColor.RESET}")
    }
    f
  }

}
