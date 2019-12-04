package pw.byakuren.discordplugin

import pw.byakuren.discordplugin.contexts.{BukkitContext, Context, DiscordContext}
import pw.byakuren.discordplugin.link.LinkUser

trait Command {

  def getName: String

  final def run(executor: LinkUser, args: Array[String], context: Context): Unit = {
    context match {
      case _:DiscordContext => discordExecute(executor, args, _)
      case _:BukkitContext => bukkitExecute(executor, args, _)
    }
  }

  def discordExecute(executor: LinkUser, args: Array[String], context:DiscordContext): Unit

  def bukkitExecute(executor: LinkUser, args: Array[String], context:BukkitContext): Unit

}
