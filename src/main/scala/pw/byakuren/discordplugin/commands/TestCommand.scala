package pw.byakuren.discordplugin.commands

import pw.byakuren.discordplugin.{Command, CommandRegistry}
import pw.byakuren.discordplugin.contexts.{BukkitContext, DiscordContext}
import pw.byakuren.discordplugin.link.{LinkUser, LinkUserFactory}

object TestCommand extends Command {

  override def getName: String = "test"

  override def discordExecute(executor: Option[LinkUser], args: Array[String], context: DiscordContext): Boolean = {
    context.channel.sendMessage("Test command!").queue()
    true
  }

  override def bukkitExecute(executor: Option[LinkUser], args: Array[String], context: BukkitContext): Boolean = {
    context.player.sendMessage("Test command!")
    true
  }
}
