package pw.byakuren.discordplugin.commands

import java.awt.TextComponent

import org.bukkit.{Bukkit, ChatColor}
import org.bukkit.entity.Player
import pw.byakuren.discordplugin.Command
import pw.byakuren.discordplugin.contexts.{BukkitContext, DiscordContext}
import pw.byakuren.discordplugin.link.{LinkUser, PendingLink}

import scala.collection.mutable

object LinkCommand extends Command {

  private val pending: mutable.HashSet[PendingLink] = mutable.HashSet()

  override def getName: String = "link"

  override def discordExecute(executor: LinkUser, args: Array[String], context: DiscordContext): Boolean = {
    if (args.length != 1) {
      context.channel.sendMessage("Minecraft username not provided!").queue()
      return false
    }
    val name = args{0}
    val players: Array[Player] = Bukkit.getOnlinePlayers.toArray(Array[Player]())

    for (p <- players.find{_.getName==name}) {
      val link = new PendingLink(p.getUniqueId, context.author)
      p.sendMessage(s"To complete the link, run /link ${link.hashCode().toHexString}")
      pending.add(link)
      context.channel.sendMessage(s"Run the command `/link ${link.hashCode().toHexString}` in chat to complete the pairing process.").queue()
      return true
    }
    context.channel.sendMessage(s"Player $name not found or is not online.").queue()
    false
  }

  override def bukkitExecute(executor: LinkUser, args: Array[String], context: BukkitContext): Boolean = {
    if (args.length != 1) {
      return false
    }
    val hash = args{0}
    val linkOption = pending.find{_.hashCode().toHexString == hash}
    linkOption match {
      case Some(p) =>
        if (p.uuid != context.player.getUniqueId) {
          context.player.sendMessage(ChatColor.RED+"That isn't for you!")
          return false
        }
        pending.remove(p)
        p.complete(context.config)
        context.player.sendMessage(ChatColor.GREEN+"Link completed!")
        true
      case None => {
        context.player.sendMessage("There is no pending link!")
        false
      }
    }
  }
}
