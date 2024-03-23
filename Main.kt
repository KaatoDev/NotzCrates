package notzcrates

import eu.decentsoftware.holograms.api.DecentHolograms
import eu.decentsoftware.holograms.api.DecentHologramsAPI
import notzcrates.commands.CratesC
import notzcrates.commands.NCratesC
import notzcrates.events.*
import notzcrates.guis.CrateGUI
import notzcrates.managers.CrateManager
import notzcrates.managers.CrateManager.loadCrates
import notzcrates.managers.PlayerManager.loadPlayers
import notzcrates.znotzapi.NotzAPI
import notzcrates.znotzapi.apis.NotzYAML
import notzcrates.znotzapi.utils.MessageU.c
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var pathRaw: String
        lateinit var notzAPI: NotzAPI
        lateinit var hologramAPI: DecentHolograms

        lateinit var cf: NotzYAML
        lateinit var sqlf: NotzYAML
        lateinit var msgf: NotzYAML

        lateinit var crateGui: CrateGUI

        var started = false
    }

    override fun onEnable() {
        pathRaw = dataFolder.absolutePath
        cf = NotzYAML(this, "config")
        sqlf = NotzYAML(this, "notzCrates")
        msgf = NotzYAML(this, "messages")

        notzAPI = NotzAPI(msgf)
        hologramAPI = DecentHologramsAPI.get()

        server.scheduler.runTaskLater(this, {
            startPlugin()
            Bukkit.getOnlinePlayers().forEach { if (it.hasPermission("notzcrates.admin")) send(it, "&6NotzCratesV3 &ainiciado!") }
        }, 5 * 20)

        Bukkit.getScheduler().runTaskTimer(this, CrateManager.getInstance(), 20 * 60, 20 * 60)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun startPlugin() {
        crateGui = CrateGUI()

        loadCrates()
        loadPlayers()

        regCommands()
        regEvents()
        regTab()

        letters()
        started = true
    }


    private fun regCommands() {
        getCommand("crates").executor = CratesC()
        getCommand("ncrates").executor = NCratesC()
    }

    private fun regEvents() {
        Bukkit.getPluginManager().registerEvents(CrateEv(), this)
        Bukkit.getPluginManager().registerEvents(CrateGuiEv(), this)
        Bukkit.getPluginManager().registerEvents(JoinLeaveEv(), this)
        Bukkit.getPluginManager().registerEvents(KeyEv(), this)
        Bukkit.getPluginManager().registerEvents(HoloEv(), this)
    }

    private fun regTab() {
        getCommand("crates").tabCompleter = CratesC()
    }

    private fun letters() {
        Bukkit.getConsoleSender().sendMessage(
            c("&2Inicializado com sucesso.\n") +
                    c("""
                &f┳┓    &6┏┓        
                &f┃┃┏┓╋┓&6┃ ┏┓┏┓╋┏┓┏
                &f┛┗┗┛┗┗&6┗┛┛ ┗┻┗┗ ┛
                """.trimIndent()
                    )
        )
    }
}
