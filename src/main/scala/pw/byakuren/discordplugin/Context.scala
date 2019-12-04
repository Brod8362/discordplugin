package pw.byakuren.discordplugin

sealed trait Context
case object DiscordContext extends Context
case object BukkitContext extends Context
