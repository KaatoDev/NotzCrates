package notzcrates.managers

import notzcrates.Main.Companion.crateGui
import notzcrates.entities.Crate
import notzcrates.enums.Rarity
import notzcrates.managers.DatabaseManager.addCrateDB
import notzcrates.managers.DatabaseManager.loadCratesDatabase
import notzcrates.managers.DatabaseManager.remCrateDB
import notzcrates.managers.DatabaseManager.updateCrateDatabase
import notzcrates.znotzapi.utils.JoyU
import notzcrates.znotzapi.utils.MessageU.c
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object CrateManager : Runnable {
    private val instance = this
    private var crates = hashMapOf<String, Crate>()

    fun createCrate(name: String, display: String, rarity: Rarity) {
        crates[name] = addCrateDB(name, display, rarity)
        crates[name]!!.createCrate()
        crateGui.update()
    }

    fun deleteCrate(crate: Crate) {
        crate.clearLocations()
        crates.remove(crate.name)
        remCrateDB(crate)
        crateGui.update()
    }

    fun getCrates(): List<Crate> {
        return crates.values.toList()
    }

    fun getCrate(name: String): Crate? {
        return crates.getOrDefault(name, null)
    }

    fun getCrateByLocation(location: Location): Crate {
        return crates.values.find { it.locations.contains(location) }!!
    }

    fun existCrate(name: String): Boolean {
        return crates.keys.contains(name)
    }

    fun updateCrateKeys(crate: Crate, oldKey: ItemStack) {
        crates.values.forEach { it.updateKeys(crate, oldKey) }
    }

    fun updateCrate(crate: Crate) {
        updateCrateDatabase(crate)
    }

    fun getAllLocations(): List<Location> {
        return crates.values.flatMap { it.locations }
    }

    fun verifyLocation(loc: Location): Boolean {
        val locs = getAllLocations()

        for (x in -1..1)
            for (y in -1..1)
                for (z in -1..1) {
                    val tempLoc = loc.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                    if (locs.contains(tempLoc) || tempLoc.block.type == Material.CHEST || tempLoc.block.type == Material.TRAPPED_CHEST)
                        return false
                }

        return true
    }

    fun isCrateKey(item: ItemStack): Boolean {
        return crates.values.any { it.key.isSimilar(item) }
    }

    fun isCrateBlock(item: ItemStack): Boolean {
        return crates.values.any { it. block.isSimilar(item) }
    }

    fun isCrateReward(reward: ItemStack, crate: Crate): Boolean {
        return crate.rewards.any { it.item.isSimilar(reward) }
    }

    fun isCrateTitle(crate: Crate, title: String, crateTitle: String): Boolean {
        return title == c(crateTitle.replace("{crate}", crate.getDisplayCrate()))
    }

    fun getCrateByKey(key: ItemStack): Crate? {
        return crates.values.find { it.key.isSimilar(key) }
    }

    fun getCrateByBlock(key: ItemStack): Crate? {
        return crates.values.find { it.block.isSimilar(key) }
    }

    fun getCrateById(crateID: Int): Crate? {
        return crates.values.find { it.id == crateID }
    }

    fun getCrateByTitle(inventoryTitle: String, titleToFind: String): Crate? {
        return crates.values.find { inventoryTitle == c(titleToFind.replace("crate.getDisplayCrate()", it.getDisplayCrate())) }
    }

    fun loadCrates() {
        crates = loadCratesDatabase()
        crates.values.forEach { it.load() }
        crateGui.update()
    }

    override fun run() {
        crates.values.forEach {
            it.update()
            it.load()
        }

        PlayerManager.run()
    }

    fun getInstance(): CrateManager {
        return instance
    }

    fun repulsePlayer(p: Player) {
        JoyU.repulsePlayer(p)
    }
}