package pw.byakuren.discordplugin.link

import java.util.UUID

import net.dv8tion.jda.api.entities.Member

class LinkUser(uuid: UUID, member: Member) {

  def getUUID: UUID = uuid

  def getMember: Member = member

}
