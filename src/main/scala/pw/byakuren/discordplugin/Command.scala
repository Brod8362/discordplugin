package pw.byakuren.discordplugin

trait Command {

  def getName: String

  final def run(executor: String, args: Array[String], context: Context): Unit = {
    context match {
      case DiscordContext => { discordExecute(executor, args)}
      case BukkitContext => {}
    }
  }

  def discordExecute(executor:String, args: Array[String]): Unit = {

  }

  def bukkitExecute(executor:String, args: Array[String]): Unit = {

  }

}
