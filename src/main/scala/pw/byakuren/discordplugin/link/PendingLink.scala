package pw.byakuren.discordplugin.link

import java.util.UUID

import net.dv8tion.jda.api.entities.Member
import org.bukkit.configuration.file.FileConfiguration

class PendingLink(val uuid: UUID, val mem: Member) {

  def complete(config: FileConfiguration): Unit = {
    //config.getList("pairs").add(toString())
    /* this is throwing a ?0 where type ?0 thingy. will deal with later

     */
  }

  override def toString: String = s"${uuid.toString}:${mem.getId}"

}
