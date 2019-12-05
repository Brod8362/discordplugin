package pw.byakuren.discordplugin

import org.bukkit.command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import pw.byakuren.discordplugin.contexts.BukkitContext
import pw.byakuren.discordplugin.link.LinkUserFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future,blocking}

class DiscordPlugin extends JavaPlugin {

  saveDefaultConfig()
  private val discordFuture = Future {
    blocking {
      val t = new DiscordConnection(this, getConfig, getLogger)
      t
    }
  }

  override def onLoad(): Unit = {
    loadConfig()
  }

  override def onDisable(): Unit = {
    saveConfig()
    for (discord <- discordFuture) {
      discord.disable()
      discord.shutdown()
    }
  }

  override def onEnable(): Unit = {
    for (discord <- discordFuture) {
      discord.enable()
    }
  }

  def loadConfig(): Unit = {
    getConfig.addDefault("token", "token")
    getConfig.addDefault("guild", "-1")
    getConfig.addDefault("channel", "-1")
    getConfig.addDefault("report-deaths", true)
    getConfig.addDefault("report-connect", true)
    getConfig.addDefault("report-disconnect", true)
    getConfig.addDefault("bot-prefix", "!")
    getConfig.options.copyDefaults(true)
  }

  override def onCommand(sender: CommandSender, cmd: command.Command, label: String, args: Array[String]): Boolean = {
    val commandOption = CommandRegistry.getCommand(cmd.getName)
    sender match {
      case player:Player =>
        for (command <- commandOption) {
          command.run(LinkUserFactory.fromUUID(player.getUniqueId), args, new BukkitContext(player, getConfig))
        }
    }
    super.onCommand(sender, cmd, label, args)
  }

}
