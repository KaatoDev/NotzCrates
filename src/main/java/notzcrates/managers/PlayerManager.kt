package notzcrates.managers

import notzcrates.entities.Crate
import notzcrates.entities.PlayerCrate
import notzcrates.managers.CrateManager.getCrates
import notzcrates.managers.DatabaseManager.addPlayerDB
import notzcrates.managers.DatabaseManager.existsPlayerDB
import notzcrates.managers.DatabaseManager.loadPlayersDatabase
import notzcrates.managers.DatabaseManager.remPlayerDB
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object PlayerManager {
    private val players = hashMapOf<Player, PlayerCrate>()
    private val allPlayers = hashMapOf<String, PlayerCrate>()

    fun updatePlayerKeys(crate: Crate, oldKey: ItemStack, toVirtual: Boolean) {
        allPlayers.values.forEach { it.updateKeys(crate, oldKey, toVirtual) }
    }

    fun createPlayer(p: Player) {
        val pc = addPlayerDB(p)
        allPlayers[p.name] = pc
        players[p] = pc
    }

    fun removePlayer(p: Player) {
        if (players.containsKey(p))
            players.remove(p)
        allPlayers.remove(p.name)
        remPlayerDB(getPlayer(p))
    }

    fun getPlayer(p: Player): PlayerCrate {
        if (players[p] == null)
            loginPlayer(p)

        return players[p]!!
    }

    fun getPlayers(): Set<PlayerCrate> {
        return players.values.toSet()
    }

    fun loginPlayer(p: Player) {
        if (!allPlayers.containsKey(p.name) || !existsPlayerDB(p))
            createPlayer(p)

        else players[p] = allPlayers[p.name]!!
    }

    fun logoutPlayer(p: Player) {
        players.remove(p)
    }

    fun getKeysRaw(pm: PlayerCrate): Set<ItemStack> {
        return if (pm.keys.isNotEmpty())
            pm.keys.map { val item = getCrates().find { crate -> crate.id == it.key }!!.key.clone(); item.amount = it.value; item }.toSet()

        else setOf()
    }

    fun transformKeysFromRaw(keys: Set<ItemStack>): HashMap<Int, Int> {
        return if (keys.isNotEmpty())
            keys.associate { val id = getCrates().find { crate -> crate.key.isSimilar(it) }!!.id; id to it.amount } as HashMap<Int, Int>

        else hashMapOf()
    }

    fun loadPlayers() {
        if (loadPlayersDatabase().isNotEmpty())
            loadPlayersDatabase().forEach { if (it.player.name != null) { allPlayers[it.player.name] = it; if(it.player.isOnline) players[it.player.player] = it } }
        Bukkit.getOnlinePlayers().forEach { loginPlayer(it) }
    }

    fun run() {
        allPlayers.forEach { it.value.update()}
    }
}