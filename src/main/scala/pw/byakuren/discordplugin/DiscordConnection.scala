package pw.byakuren.discordplugin

import java.awt.Color
import java.util.logging.Logger

import javax.security.auth.login.LoginException
import net.dv8tion.jda.api.entities.{Message, TextChannel}
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.{EmbedBuilder, JDA, JDABuilder}
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.{AsyncPlayerChatEvent, PlayerLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.plugin.java.JavaPlugin
import pw.byakuren.discordplugin.commands.TestCommand
import pw.byakuren.discordplugin.contexts.DiscordContext
import pw.byakuren.discordplugin.link.LinkUserFactory

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

  def sendMessageToBukkit(str: String) : Unit = {
    if (enabled)
      Bukkit.broadcastMessage(str)
  }

  def sendMessageToBukkit(msg: Message) : Unit = {
    sendMessageToBukkit(s"{${msg.getAuthor.getName}} ${msg.getContentRaw}")
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

  override def onReady(event: ReadyEvent): Unit = {
    CommandRegistry.register(TestCommand)
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
        new DiscordContext(msg.getMember, msg.getTextChannel, msg))
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

  def shutdown(): Unit = jda.shutdown()

}
