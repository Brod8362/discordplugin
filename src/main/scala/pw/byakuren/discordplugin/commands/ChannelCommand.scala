package pw.byakuren.discordplugin.commands

import pw.byakuren.discordplugin.Command
import pw.byakuren.discordplugin.contexts.{BukkitContext, DiscordContext}
import pw.byakuren.discordplugin.link.LinkUser

object ChannelCommand extends Command {

  override def getName: String = "channel"

  override def discordExecute(executor: LinkUser, args: Array[String], context: DiscordContext): Boolean = {
    true
  }

  override def bukkitExecute(executor: LinkUser, args: Array[String], context: BukkitContext): Boolean = {
    true
  }
}
