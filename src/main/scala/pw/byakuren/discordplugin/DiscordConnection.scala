package pw.byakuren.discordplugin

import java.awt.Color
import java.util.logging.Logger

import javax.security.auth.login.LoginException
import net.dv8tion.jda.api.entities.{Message, TextChannel}
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.{EmbedBuilder, JDA, JDABuilder}
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.{AsyncPlayerChatEvent, PlayerLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, Listener}

class DiscordConnection(config: FileConfiguration, logger: Logger) extends ListenerAdapter with Listener {

  var enabled = true

  val jda: JDA = init()

  val CHANNEL_ID:String = config.getString("channel")
  val prefix:String = config.getString("bot-prefix")
  val SELF_USER_ID:Long = jda.getSelfUser.getIdLong
  val channel:TextChannel = jda.getTextChannelById(CHANNEL_ID)

  def init() : JDA = {
    try {
      new JDABuilder(config.getString("token")).build()
    } catch {
      case _: LoginException => {
        logger.warning("Failed to load discord - Invalid bot token")
        throw new RuntimeException("Failed to load DiscordPlugin - invalid token")
      }
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
    sendMessageToBukkit(f"{${msg.getAuthor.getName}} ${msg.getContentRaw}")
  }

  def alertUserConnectionChange(usr: String, joined: Boolean) : Unit = {
    val eb = new EmbedBuilder
    eb.setColor(Color.YELLOW)
    eb.setTitle(f"$usr ${if (joined) "joined" else "left"} the game")
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
    if (msg.getChannel.getId != CHANNEL_ID || msg.getAuthor.getIdLong==SELF_USER_ID) return
    if (msg.getContentRaw.startsWith(prefix+" ")) {
      executeCommand(msg.getContentRaw)
      return
    }
    sendMessageToBukkit(msg)
  }

  /* Discord-Specific Events */

  def executeCommand(msg: String): Unit = {
    //todo convert stuff into stuff
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
    sendMessageToDiscord(f"<${event.getPlayer.getName}> ${event.getMessage}")
  }

  @EventHandler
  def onDeath(event: PlayerDeathEvent): Unit = {
    alertDeath(event.getDeathMessage)
  }

  /* Plugin handling methods */
  def enable(): Unit = enabled = true
  def disable(): Unit = enabled = false

}
