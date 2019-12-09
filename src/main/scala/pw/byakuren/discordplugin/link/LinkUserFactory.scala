package pw.byakuren.discordplugin.link

import java.util.UUID

import net.dv8tion.jda.api.entities.Member
import org.bukkit.configuration.file.FileConfiguration

import scala.collection.mutable

object LinkUserFactory {

  private val path = "pairs"
  private def registered = mutable.HashSet[LinkUser]()

  def fromUUID(uuid: UUID): Option[LinkUser] = {
    registered.find { _.getUUID == uuid }
  }

  def fromMember(mem: Member): Option[LinkUser] = registered.find { _.getMember.getId == mem.getId }

  def exists(uuid: UUID): Boolean = registered.map(_.getUUID) contains uuid

  def exists(mem: Member): Boolean = registered.map(_.getMember.getId) contains mem.getId

  def add(u: LinkUser): Unit = registered.add(u)


}
