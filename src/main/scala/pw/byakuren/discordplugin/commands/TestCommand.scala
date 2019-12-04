package pw.byakuren.discordplugin.commands

import pw.byakuren.discordplugin.Command
import pw.byakuren.discordplugin.link.LinkUser

object TestCommand extends Command {

  override def getName: String = "test"

  override def discordExecute(executor: LinkUser, args: Array[String]): Unit = ???

  override def bukkitExecute(executor: LinkUser, args: Array[String]): Unit = ???
}
