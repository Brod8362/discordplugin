package pw.byakuren.discordplugin

import org.bukkit.plugin.java.JavaPlugin

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

}
