package notzcrates.commands

import notzcrates.Main.Companion.crateGui
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class CratesC : TabExecutor {
    override fun onCommand(p: CommandSender?, cmd: Command?, label: String?, args: Array<String?>): Boolean {
        if (p !is Player)
            return false

        if (args.isEmpty())
            crateGui.open(p)
        else send(p, "&cUtilize apenas &f/&ccrates")

        return true
    }

    override fun onTabComplete(sender: CommandSender?, command: Command?, alias: String?, args: Array<String?>?): List<String> {
        return emptyList()
    }
}