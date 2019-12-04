package pw.byakuren.discordplugin

import pw.byakuren.discordplugin.contexts.{BukkitContext, Context, DiscordContext}
import pw.byakuren.discordplugin.link.LinkUser

trait Command {

  def getName: String

  final def run(executor: LinkUser, args: Array[String], context: Context): Boolean = {
    context match {
      case a:DiscordContext => discordExecute(executor, args, a)
      case b:BukkitContext => bukkitExecute(executor, args, b)
      case _ => false
    }
  }

  def discordExecute(executor: LinkUser, args: Array[String], context:DiscordContext): Boolean

  def bukkitExecute(executor: LinkUser, args: Array[String], context:BukkitContext): Boolean

}
