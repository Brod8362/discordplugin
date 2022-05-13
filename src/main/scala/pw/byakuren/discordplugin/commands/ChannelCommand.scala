package pw.byakuren.discordplugin.commands

import pw.byakuren.discordplugin.Command
import pw.byakuren.discordplugin.contexts.{BukkitContext, DiscordContext}
import pw.byakuren.discordplugin.link.LinkUser
import pw.byakuren.discordplugin.util.Utility._

import scala.jdk.CollectionConverters._

object ChannelCommand extends Command {

  override def getName: String = "channel"

  override def discordExecute(executor: Option[LinkUser], args: Array[String], context: DiscordContext): Boolean = {
    context.msg.getMentionedChannels.asScala.headOption match {
      case Some(a) =>
        context.config.set("channel", a.getId)
        context.channel.sendMessage(s"Set channel to ${a.getAsMention}, reload the plugin for changes to take effect.").queue()
        true
      case None =>
        context.channel.sendMessage("No channel provided.").queue()
        false
    }
  }

  override def bukkitExecute(executor: Option[LinkUser], args: Array[String], context: BukkitContext): Boolean = {
    val jda = executor match {
      case Some(lu) => lu.getMember.getJDA
      case None =>
        context.player.errorMessage("You need to link your discord account to use this command.")
        return false
    }

    val result = for {
      channelId <- args.headOption.toRight("No channel provided")
      channel <- Option(jda.getGuildById(channelId)).toRight("Invalid channel ID")
    } yield {
      context.config.set("channel", channelId)
      context.player.successMessage(s"Channel set to ${channel.getName}. Reload the plugin for changes to take effect.")
    }
    for (msg <- result.left) {
      context.player.errorMessage(msg)
      return false
    }
    true
  }
}
