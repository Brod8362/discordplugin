package pw.byakuren.discordplugin.link

import java.util.UUID

import net.dv8tion.jda.api.entities.Member
import org.bukkit.configuration.file.FileConfiguration

class PendingLink(val uuid: UUID, val mem: Member) {

  def complete(config: FileConfiguration): LinkUser = {
    config.getStringList("pairs").add(toString)
    val t = new LinkUser(uuid, mem)
    LinkUserFactory.add(t)
    t
  }

  override def toString: String = s"${uuid.toString}:${mem.getId}"

}
