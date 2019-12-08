package pw.byakuren.discordplugin.contexts

import net.dv8tion.jda.api.entities.{Member, Message, TextChannel}
import org.bukkit.configuration.file.FileConfiguration


class DiscordContext(val author: Member, val channel: TextChannel, val msg: Message, val config: FileConfiguration) extends Context {

}
