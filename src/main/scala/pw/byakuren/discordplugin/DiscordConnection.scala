package pw.byakuren.discordplugin

import net.dv8tion.jda.api.entities._
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.voice.{GuildVoiceJoinEvent, GuildVoiceLeaveEvent}
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.{EmbedBuilder, JDA, JDABuilder}
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.{AsyncPlayerChatEvent, PlayerLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.{Bukkit, ChatColor}
import pw.byakuren.discordplugin.util.Utility.StringMethods

import java.awt.Color
import java.util.logging.Logger
import javax.security.auth.login.LoginException
import scala.jdk.CollectionConverters.CollectionHasAsScala

class DiscordConnection(plugin: JavaPlugin, config: FileConfiguration, logger: Logger) extends ListenerAdapter with Listener {

  var enabled = true

  val jda: JDA = init()

  val CHANNEL_ID: String = config.getString("channel")
  val prefix: String = config.getString("bot-prefix")
  jda.awaitReady()
  val SELF_USER_ID: Long = jda.getSelfUser.getIdLong
  var channel: TextChannel = jda.getTextChannelById(CHANNEL_ID)

  def init(): JDA = {
    try {
      Bukkit.getServer.getPluginManager.registerEvents(this, plugin)
      JDABuilder.createDefault(config.getString("token")).addEventListeners(this).build()
    } catch {
      case _: LoginException =>
        logger.warning("Failed to load discord - Invalid bot token")
        throw new RuntimeException("Failed to load DiscordPlugin - invalid token")
    }
  }

  def sendMessageToDiscord(str: String): Unit = {
    if (enabled)
      channel.sendMessage(str).queue()
  }

  def sendMessageToBukkit(name: String, msg: String, img_count: Int, replyContent: Option[String] = None): Unit = {
    if (enabled) {
      val img_str = if (img_count > 0) s"${ChatColor.DARK_PURPLE}[File${if (img_count == 1) "" else s"×$img_count"}] ${ChatColor.RESET}" else ""
      replyContent match {
        case Some(rc) =>
          val top = s"${ChatColor.GRAY}╔⇒ ${ChatColor.ITALIC}$rc${ChatColor.RESET}\n"
          val main = s"${ChatColor.GRAY}╚ ${ChatColor.RESET}{$name} $img_str$msg"
          Bukkit.broadcastMessage(top + main)
        case _ =>
          Bukkit.broadcastMessage(s"{$name} $img_str$msg")
      }

    }
  }

  def sendMessageToBukkit(msg: Message): Unit = {
    val mainContent = pingHighlight(msg)
    Option(msg.getMessageReference) match {
      case Some(orig) =>
        val rc = s"${orig.getMessage.getAuthor.getName}: ${orig.getMessage.getContentDisplay.truncate(30)}"
        sendMessageToBukkit(msg.getAuthor.getName, mainContent, msg.getAttachments.size(), Some(rc))
      case _ =>
        sendMessageToBukkit(msg.getAuthor.getName, mainContent, msg.getAttachments.size())
    }

  }

  def alertUserConnectionChange(usr: String, joined: Boolean): Unit = {
    val eb = new EmbedBuilder
    eb.setColor(Color.YELLOW)
    eb.setTitle(s"$usr ${if (joined) "joined" else "left"} the game")
    channel.sendMessageEmbeds(eb.build()).queue()
    val offset = if (joined) 1 else -1
    updatePresence(Bukkit.getServer.getOnlinePlayers.size + offset)
  }

  def alertDeath(msg: String): Unit = {
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
      sendMessageToBukkit(msg)
    }
  }

  override def onGuildVoiceJoin(event: GuildVoiceJoinEvent): Unit = {
    alertVCUpdate(event.getMember, event.getChannelJoined, joined = true)
  }

  override def onGuildVoiceLeave(event: GuildVoiceLeaveEvent): Unit = {
    alertVCUpdate(event.getMember, event.getChannelLeft, joined = false)
  }

  override def onReady(event: ReadyEvent): Unit = {
    logger.info(s"Loaded ${CommandRegistry.size} commands.")
  }

  /* Discord-Specific Events */

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

  @EventHandler(priority = EventPriority.MONITOR)
  def onLogin(event: PlayerLoginEvent): Unit = {
    if (event.getResult == PlayerLoginEvent.Result.ALLOWED) {
      alertUserConnectionChange(event.getPlayer.getName, joined = true)
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  def onLogout(event: PlayerQuitEvent): Unit = {
    alertUserConnectionChange(event.getPlayer.getName, joined = false)
  }

  @EventHandler
  def onMessage(event: AsyncPlayerChatEvent): Unit = {
    // try to substitute pings when sending to discord
    var discordContent = event.getMessage
    var minecraftContent = event.getMessage

    // find new-style usernames
    val matches = "@[a-z._0-9]{2,32}".r.findAllIn(minecraftContent)
    // do not uncomment this: checking the size of the iterator consumes the entries
    // logger.info(s"Found ${matches.size} regex matches")

    for (found <- matches) {
      logger.info(s"Regex match $found")
      val username = found.substring(1)
      //try to find a user with that username
      jda.getUsersByName(username, true).asScala.headOption match {
        case Some(user) =>
          discordContent = discordContent.replaceAll(found, s"<@${user.getId}>")
          minecraftContent = minecraftContent.replaceAll(found, s"${ChatColor.BLUE}@${user.getName}${ChatColor.RESET}")
        case None =>
          minecraftContent = minecraftContent.replaceAll(found, s"${ChatColor.RED}@?$username${ChatColor.RESET}")
      }
    }

    sendMessageToDiscord(s"<${event.getPlayer.getName}> $discordContent")
    event.setMessage(minecraftContent)
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
