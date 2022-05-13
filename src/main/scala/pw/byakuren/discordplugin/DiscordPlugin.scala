package pw.byakuren.discordplugin

import org.bukkit.command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import pw.byakuren.discordplugin.contexts.BukkitContext
import pw.byakuren.discordplugin.link.LinkUserFactory

class DiscordPlugin extends JavaPlugin {

  saveDefaultConfig()
  private var discordOption: Option[DiscordConnection] = None

  override def onLoad(): Unit = {
    loadConfig()
  }

  override def onDisable(): Unit = {
    saveConfig()

    for (discord <- discordOption) {
      discord.disable()
      discord.shutdown()
    }
  }

  override def onEnable(): Unit = {
    discordOption =  Some(new DiscordConnection(this, getConfig, getLogger))
    for (discord <- discordOption) {
      discord.enable()
    }
  }

  def loadConfig(): Unit = {
//    getConfig.addDefault("token", "token")
//    getConfig.addDefault("guild", "-1")
//    getConfig.addDefault("channel", "-1")
//    getConfig.addDefault("report-deaths", true)
//    getConfig.addDefault("report-connect", true)
//    getConfig.addDefault("report-disconnect", true):q!
//    getConfig.addDefault("bot-prefix", "!")
//    getConfig.options.copyDefaults(true)
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
