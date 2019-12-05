package pw.byakuren.discordplugin.contexts

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class BukkitContext(val player: Player, val config: FileConfiguration) extends Context {

}
