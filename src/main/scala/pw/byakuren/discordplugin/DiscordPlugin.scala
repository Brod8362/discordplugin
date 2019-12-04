package pw.byakuren.discordplugin

import org.bukkit.{Bukkit, command}
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import pw.byakuren.discordplugin.contexts.BukkitContext
import pw.byakuren.discordplugin.link.LinkUserFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DiscordPlugin extends JavaPlugin {

  saveDefaultConfig()
  private lazy val discordFuture = Future { new DiscordConnection(getConfig, getLogger) }


  override def onLoad(): Unit = {
  }

  override def onDisable(): Unit = {
    for (discord <- discordFuture) {
      discord.disable()
      discord.shutdown()
    }
  }

  override def onEnable(): Unit = {
    for (discord <- discordFuture) {
      discord.enable()
      getServer.getPluginManager.registerEvents(discord, this)
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
        commandOption match {
          case a:Command => a.run(LinkUserFactory.fromUUID(player.getUniqueId), args, new BukkitContext(player))
          case _ => false
        }
      case _ => false //this entity cannot run commands.
    }
  }

}
