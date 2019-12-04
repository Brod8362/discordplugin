package pw.byakuren.discordplugin

import scala.collection.mutable

object CommandRegistry {

  private val commands = mutable.HashMap[String,Command]()

  def register(c: Command): Unit = {
    commands += c.getName -> c
  }

  def register(c: Command*): Unit = {
    c.foreach(register)
  }

  def isCommand(n: String): Boolean = commands contains n

  def getCommand(n: String): Option[Command] = commands.get(n)

}
