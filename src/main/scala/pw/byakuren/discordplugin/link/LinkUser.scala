package pw.byakuren.discordplugin.link

import net.dv8tion.jda.api.entities.Member

class LinkUser(uuid: String, member: Member) {

  def getUUID(): String = uuid

  def getMember(): Member = member

}
