package pw.byakuren.discordplugin

import net.dv8tion.jda.api.entities.Message.MessageFlag
import net.dv8tion.jda.api.entities._
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.voice.{GuildVoiceJoinEvent, GuildVoiceLeaveEvent}
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
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

import java.awt.Color
import java.util.function.Consumer
import java.util.logging.Logger
import javax.security.auth.login.LoginException
import scala.jdk.CollectionConverters.CollectionHasAsScala

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
      JDABuilder.createDefault(config.getString("token")).addEventListeners(this).build()
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
      val img_str = if (img_count > 0) s"${ChatColor.DARK_PURPLE}[File${if (img_count==1) "" else s"×$img_count"}] ${ChatColor.RESET}" else ""
      Bukkit.broadcastMessage(s"{$name} $img_str$msg")
    }
  }

  def sendMessageToBukkit(msg: Message) : Unit = {
    val extra = Option(msg.getMessageReference) match {
      case Some(orig) => s"${ChatColor.ITALIC}${ChatColor.GRAY}-> ${orig.getMessage.getAuthor.getName}${ChatColor.RESET} "
      case _ => ""
    }
    sendMessageToBukkit(msg.getAuthor.getName, extra+pingHighlight(msg), msg.getAttachments.size())
  }

  def alertUserConnectionChange(usr: String, joined: Boolean) : Unit = {
    val eb = new EmbedBuilder
    eb.setColor(Color.YELLOW)
    eb.setTitle(s"$usr ${if (joined) "joined" else "left"} the game")
    channel.sendMessageEmbeds(eb.build()).queue()
    val offset = if (joined) 1 else -1
    updatePresence(Bukkit.getServer.getOnlinePlayers.size+offset)
  }

  def alertDeath(msg: String) : Unit = {
    val eb = new EmbedBuilder
    eb.setColor(Color.RED)
    eb.setTitle(msg)
    channel.sendMessageEmbeds(eb.build()).queue()
  }

  def alertVCUpdate(user: Member, channel: AudioChannel, joined: Boolean): Unit = {
    Bukkit.broadcastMessage(s"${ChatColor.GRAY}${ChatColor.ITALIC}" +
      s"${user.getUser.getName} ${if (joined) "joined" else "left"} voice channel #${channel.getName}")
  }

  /* Discord Listener Events */

  override def onMessageReceived(event: MessageReceivedEvent): Unit = {
    if (event.isFromGuild) {
      val msg = event.getMessage
      if (msg.getChannel.getId != CHANNEL_ID || msg.getAuthor.isBot) return
      if (msg.getContentRaw.startsWith(prefix)) {
        executeCommand(msg)
      } else {
        sendMessageToBukkit(msg)
      }
    }
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
    val cmdOption = CommandRegistry.getCommand(parsed)
    cmdOption match {
      case Some(cmd) => cmd.run(LinkUserFactory.fromMember(msg.getMember),
        argsWithCommand.slice(1, argsWithCommand.length),
        new DiscordContext(msg.getMember, msg.getTextChannel, msg, config))
      case None => msg.getChannel.sendMessage(s"Command `$parsed` not found").queue()
    }
  }

  def updatePresence(players: Int): Unit = {
    try {
      this.jda.getPresence.setActivity(Activity.playing(s"$players players online"))
    } catch {
      case e: Exception =>
        logger.warning("Failed to change presence")
        e.printStackTrace()
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
    val regex = "<@\\d{17,18}>".r
    var content = event.getMessage
    val matches = regex.findAllIn(content)
    for (found <- matches) {
      val idOpt = found.substring(2, found.length-1).toLongOption
      idOpt match {
        case Some(id)  =>
          try {
            val user = jda.retrieveUserById(id).complete()
            content = content.replaceAll(found, s"${ChatColor.BLUE}@${user.getName}${ChatColor.RESET}")
          } catch {
            case _: Throwable =>
              content = content.replaceAll(found, s"${ChatColor.RED}@unknown user${ChatColor.RESET}")
          }
        case _ =>
          content = content.replaceAll(found, s"${ChatColor.RED}@invalid${ChatColor.RESET}")
      }
    }
    event.setMessage(content)
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
