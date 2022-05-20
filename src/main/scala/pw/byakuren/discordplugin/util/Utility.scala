package pw.byakuren.discordplugin.util

import org.bukkit.ChatColor
import org.bukkit.entity.Player

object Utility {

  implicit class PlayerMethods(x: Player) {
    def errorMessage(m: String): Unit = {
      x.sendMessage(ChatColor.RED+m)
    }
    def successMessage(m: String): Unit = {
      x.sendMessage(ChatColor.GREEN+m)
    }
  }

  implicit class StringMethods(x: String) {
    def truncate(len: Int): String = {
      if (x.length < len) {
        x
      } else {
        x.take(len-3)+"..."
      }
    }
  }

}
