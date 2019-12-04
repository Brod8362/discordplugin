package pw.byakuren.discordplugin

import pw.byakuren.discordplugin.link.LinkUser

trait Command {

  def getName: String

  final def run(executor: LinkUser, args: Array[String], context: Context): Unit = {
    context match {
      case DiscordContext => { discordExecute(executor, args)}
      case BukkitContext => {}
    }
  }

  def discordExecute(executor: LinkUser, args: Array[String]): Unit

  def bukkitExecute(executor: LinkUser, args: Array[String]): Unit

}
