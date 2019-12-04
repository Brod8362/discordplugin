package pw.byakuren.discordplugin.commands

import pw.byakuren.discordplugin.{Command, CommandRegistry}
import pw.byakuren.discordplugin.contexts.{BukkitContext, DiscordContext}
import pw.byakuren.discordplugin.link.{LinkUser, LinkUserFactory}

object TestCommand extends Command {

  CommandRegistry.register(this)

  override def getName: String = "test"

  override def discordExecute(executor: LinkUser, args: Array[String], context: DiscordContext): Boolean = {
    context.channel.sendMessage("Test command!").queue()
    true
  }

  override def bukkitExecute(executor: LinkUser, args: Array[String], context: BukkitContext): Boolean = {
    context.player.getServer.broadcastMessage("Test Command!")
    context.player.sendMessage("Test command private!")
    true
  }
}
