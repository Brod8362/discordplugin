package pw.byakuren.discordplugin.contexts

import net.dv8tion.jda.api.entities.{Member, Message, TextChannel}


class DiscordContext(val author: Member, val channel: TextChannel, val msg: Message) extends Context {

}
