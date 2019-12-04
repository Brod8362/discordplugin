package pw.byakuren.discordplugin.link

import java.util.UUID

import net.dv8tion.jda.api.entities.Member
import org.bukkit.configuration.file.FileConfiguration

import scala.collection.mutable

object LinkUserFactory {

  private val path = "pairs"
  private def registered = mutable.HashSet[LinkUser]()

  def fromUUID(uuid: UUID): LinkUser = {
    if (exists(uuid)) {
      registered.find { _.getUUID == uuid }
    }
    //todo init link process
    new LinkUser(uuid, null)
  }

  def fromMember(mem: Member): LinkUser = {
    if (exists(mem)) {
      registered.find { _.getMember.getId == mem.getId }
    }
    //todo init link process
    new LinkUser(null, mem)
  }

  private def exists(uuid: UUID): Boolean = registered.map(_.getUUID) contains uuid

  private def exists(mem: Member): Boolean = registered.map(_.getMember.getId) contains mem.getId


  def getPair(config: FileConfiguration, uuid: String): (String, String) = {
    ("","")
  }

  def addPair(config: FileConfiguration, data:(String, String)): Unit = {
    ("","")
  }

}
