package notzcrates.commands

import notzcrates.Main.Companion.crateGui
import notzcrates.entities.Crate
import notzcrates.enums.Rarity
import notzcrates.managers.CrateManager.createCrate
import notzcrates.managers.CrateManager.existCrate
import notzcrates.managers.CrateManager.getCrate
import notzcrates.managers.CrateManager.getCrates
import notzcrates.managers.CrateManager.isCrateKey
import notzcrates.managers.HoloManager.getHoloRemover
import notzcrates.managers.PlayerManager.getPlayer
import notzcrates.managers.PlayerManager.getPlayers
import notzcrates.znotzapi.utils.MessageU.c
import notzcrates.znotzapi.utils.MessageU.send
import notzcrates.znotzapi.utils.MessageU.sendHeader
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class NCratesC : CommandExecutor {
    lateinit var p: Player
    override fun onCommand(sender: CommandSender?, cmd: Command?, label: String?, aa: Array<out String>?): Boolean {
        if (sender !is Player)
            return false

        p = sender

        if (!p.hasPermission("notzcrates.admin")) {
            send(p, "noperm")
            return true
        }

        var crate: Crate? = null
        var args: Array<out String> = arrayOf()

        if (aa!!.isNotEmpty()) {
            args = aa.map { if (it.contains('&')) it else it.lowercase() }.toTypedArray()
            crate = getCrate(args[0].lowercase())
        }



        when (args.size) {
            0 -> help()
            1 -> {
                when (args[0]) {
                    "create" -> send(p, "&eUtilize &f/&encrates create &f(&eName&f) &f(&eDisplay&f) &f(&eRarity&f)")
                    "getholoremover" -> getHoloRemover(p)
                    "iskey" -> if (isCrateKey(p.itemInHand)) send(p, "&aEsta key é válida!") else send(p, "&cEste item não é reconhecida como uma key.")
                    "list" -> if (getCrates().isNotEmpty()) send(p, getCrates().map { it.name }.toString()) else send(p, "&cNão há crates setadas ainda.")
                    "view" -> if (p.hasPermission("notzcrates.viewadmin")) send(p, "&eUtilize &f/&encrates view &f<&ekeys&f/&erewards&f> &f<&ePlayer&f> ") else send(p, "noperm")
                    else -> if (crate != null) helpCrate(crate) else help()
                }
            }
            2 -> {
                if (crate != null) when (args[1]) {
                    "clearlocations" -> {
                        crate.clearLocations()
                        send(p, "&eAs localizações da Crate foram excluídas!")
                    }

                    "clearrewards" -> {
                        crate.clearRewards()
                        send(p, "&eAs recompensas da Crate foram excluídas!")
                    }

                    "disable" -> {
                        if (crate.disable())
                            send(p, "&aA ${crate.getDisplayCrate()}&a foi &edesabilitada&a!")
                        else send(p, "&eA ${crate.getDisplayCrate()}&e já está &cdesabilitada&e!")
                    }

                    "enable" -> {
                        if (crate.enable())
                            send(p, "&aA ${crate.getDisplayCrate()}&a foi &ehabilitada&a!")
                        else send(p, "&eA ${crate.getDisplayCrate()}&e já está &ahabilitada&e!")
                    }

                    "get" -> {
                        p.inventory.addItem(crate.block)
                        send(p, "&eVocê recebeu o item da ${crate.getDisplayCrate()}&e.")
                    }

                    "key" -> {
                        val pm = getPlayer(p)
                        pm.addKey(crate, 1)
                        send(p, "&eVocê recebeu uma ${crate.getDisplayKey()}&e.")
                    }

                    "keyall" -> {
                        getPlayers().forEach {
                            it.addKey(crate, 1)
                            send(it.player.player, "&eVocê recebeu uma ${crate.getDisplayKey()}&e.")
                        }

                        send(p, "&eVocê deu uma ${crate.getDisplayKey()}&e para todos os players online!")
                    }

                    "setdisplay" -> {
                        send(p, "&eUtilize: &f/&encrates ${crate.name} &esetDisplay &f<&eDisplay&f>&e.")
                    }

                    else -> helpCrate(crate)
                } else help()
            }
            3 -> {
                if (crate != null) when (args[1]) {
                    "key" -> {
                        if (Bukkit.getPlayer(args[2]) != null) {
                            val pm2 = getPlayer(Bukkit.getPlayer(args[2]))
                            pm2.addKey(crate, 1)

                            send(pm2.player.player, "&eVocê recebeu uma ${crate.getDisplayKey()}&e.")
                            send(p, "&eVocê enviou uma ${crate.getDisplayKey()}&e ao player &f${args[2]}.")

                        } else send(p, "&cEste player não existe ou está offline.")
                    }

                    "setdisplay" -> {
                        send(
                            p,
                            "&eDisplay da crate &f${crate.name}&e alterado com sucesso de &r${crate.getDisplay()}&e para &r${args[2]}&e!"
                        )
                        crate.alterDisplay(c(args[2]))
                    }

                    else -> helpCrate(crate)

                } else if (args[0] == "view") {
                    if (p.hasPermission("notzcrates.viewadmin")) {
                        if (Bukkit.getPlayer(args[2]) != null) {
                            when (args[1]) {
                                "keys" -> crateGui.viewKeys(getPlayer(Bukkit.getPlayer(args[2])))
                                "rewards" -> crateGui.viewRewards(getPlayer(Bukkit.getPlayer(args[2])))
                                else -> send(p, "&eUtilize &f/&encrates view &f<&ekeys&f/&erewards&f> &f<&ePlayer&f>")
                            }

                        } else send(p, "&eEste player não existe ou está offline.")

                    } else send(p, "noperm")

                } else help()
            }
            4 -> {
                if (crate != null) {
                    if (args[1] == "setdisplay") {
                        val newDisplay = c(args[2] + " " + args[3])
                        send(p, "&eDisplay da crate &f${crate.name}&e alterado com sucesso de &r${crate.getDisplay()}&e para &r$newDisplay&e!")
                        crate.alterDisplay(newDisplay)

                    } else helpCrate(crate)

                } else if (args[0] == "create") {
                    if (existCrate(args[1]))
                        send(p, "&cUma crate com o nome &f${args[1]}&c já existe!")

                    else if (!Rarity.entries.any { it.name == args[3].uppercase() })
                        send(p, "&cRaridade inválida.")

                    else {
                        createCrate(args[1], args[2], Rarity.valueOf(args[3].uppercase()))
                        send(p, "&aA ${getCrate(args[1])!!.getDisplayCrate()} &f(${args[1]}) &afoi criada com sucesso!")
                    }

                } else help()
            }
            else -> {
                if (crate != null) {
                    if (args[1] == "setdisplay") {
                        val newDisplay = c(args.slice(2..< args.size).joinToString(prefix = "", postfix = "", separator = " "))

                        send(p, "&eDisplay da crate &f${crate.name}&e alterado com sucesso de &r${crate.getDisplay()}&e para &r$newDisplay&e!")

                        crate.alterDisplay(newDisplay)

                    } else helpCrate(crate)

                } else help()
            }
        }

        return true
    }

    private fun help() {
        p.sendMessage("")
        sendHeader(p)
        p.sendMessage(c("&f/&encrates &f<&eNome da Crate&f> &7- Para entrar na edição da Crate."))
        p.sendMessage(c("&f/&encrates create &f(&eName&f) &f(&eDisplay&f) &f(&eRarity&f) &7- Cria uma crate."))
        p.sendMessage(c("&f/&encrates getHoloRemover &7- Recebe um removedor de holograma."))
        p.sendMessage(c("&f/&encrates iskey &7- Identifica se o item atual na mão é uma Key."))
        p.sendMessage(c("&f/&encrates list &7- Lista as crates."))
        p.sendMessage(c("&f/&encrates view &f<&ekeys&f/&erewards&f> &f<&ePlayer&f> &7- Abre o inventário de keys ou rewards de um player para editar."))
        p.sendMessage(" ")
    }

    private fun helpCrate(crate: Crate) {
        p.sendMessage("")
        sendHeader(p)
        p.sendMessage(c("&cUtilize: &f/&encrates ${crate.name}&7 +"))
        p.sendMessage(c("&7+ &eclearLocations &7- Reseta os locais onde há Crate setada."))
        p.sendMessage(c("&7+ &eclearRewards &7- Reseta as recompensas da Crate."))
        p.sendMessage(c("&7+ &edisable&f/&eenable &7- Desabilita ou habilita a Crate."))
        p.sendMessage(c("&7+ &eget &7- Recebe a Crate para ser colocada."))
        p.sendMessage(c("&7+ &ekey &7- Recebe uma Key da Crate."))
        p.sendMessage(c("&7+ &ekey &f(&ePlayer&f) &7- Dá uma Key da Crate."))
        p.sendMessage(c("&7+ &ekeyall &7- Dá uma Key da Crate para todos os players online."))
        p.sendMessage(c("&7+ &esetDisplay &f<&eDisplay&f> &7- Altera o display da Crate."))
        p.sendMessage(" ")
    }
}