package pw.byakuren.discordplugin.link

import net.dv8tion.jda.api.entities.Member

object LinkUserFactory {

  def createFromUUID(uuid: String): LinkUser = {
    new LinkUser("", null)
  }

  def createFromMember(mem: Member): LinkUser = {
    new LinkUser("", null)
  }

}
