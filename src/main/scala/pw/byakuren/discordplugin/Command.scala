package pw.byakuren.discordplugin

import pw.byakuren.discordplugin.contexts.{BukkitContext, Context, DiscordContext}

trait Command {

  def getName: String

  final def run(args: Array[String], context: Context): Boolean = {
    context match {
      case a:DiscordContext => discordExecute(args, a)
      case b:BukkitContext => bukkitExecute(args, b)
      case _ => false
    }
  }

  def discordExecute(args: Array[String], context:DiscordContext): Boolean

  def bukkitExecute(args: Array[String], context:BukkitContext): Boolean

}
