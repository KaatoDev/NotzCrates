package notzcrates.entities

import eu.decentsoftware.holograms.api.holograms.Hologram
import eu.decentsoftware.holograms.api.holograms.HologramLine
import notzcrates.Main.Companion.hologramAPI
import notzcrates.enums.Rarity
import notzcrates.managers.CrateManager.updateCrateKeys
import notzcrates.managers.DatabaseManager.addRewardDB
import notzcrates.managers.DatabaseManager.remRewardDB
import notzcrates.managers.DatabaseManager.updateCrateDatabase
import notzcrates.managers.PlayerManager.updatePlayerKeys
import notzcrates.managers.RewardManager.getRewardByChance
import notzcrates.znotzapi.utils.MessageU.c
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.io.Serializable

data class Crate(val id: Int, val name: String, private var display: String, private var rarity: Rarity, var isEnabled: Boolean, val locations: MutableList<Location>, var key: ItemStack, var block: ItemStack, val rewards: MutableList<Reward>) {
    data class CrateModel(val id: Int, val name: String, val display: String, val rarity: Rarity, val isEnabled: Boolean, val locations: MutableList<Location>, val key: ItemStack, val block: ItemStack, val serialVersionUID: Long) : Serializable
    constructor(id: Int, name: String, display: String, rarity: Rarity) : this(id, name, display, rarity, true, mutableListOf(), ItemStack(Material.TRIPWIRE_HOOK), ItemStack(Material.CHEST), mutableListOf())

    fun createCrate() {
        buildKey()
        buildBlock()

        if (rewards.isEmpty()) {
            rewards.add(addRewardDB(id, key))
        }
        update()
    }

    fun isVirtual(): Boolean {
        return rarity.isVirtual
    }

    fun addReward(rewardItem: ItemStack): Boolean {
        return try {
            val rew = rewardItem.clone()
            rew.amount = 1
            rewards.add(addRewardDB(id, rewardItem))
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    fun loadRewards(rewardList: List<Reward>) {
        rewards.addAll(rewardList)
    }

    fun remReward(reward: Reward) {
        rewards.remove(reward)
        remRewardDB(reward)
    }

    fun remReward(reward: ItemStack) {
        val rew = rewards.find { it.item == reward }!!

        remReward(rew)
    }

    fun getRarity(): Rarity {
        return rarity
    }

    fun getDisplay(): String {
        return display
    }

    fun getDisplayCrate(): String {
        return c("&f&lCrate &r${display}")
    }

    fun getDisplayKey(): String {
        return c("&f&lKey &r${display}")
    }

    fun getRewardById(rewardId: Int): Reward {
        return rewards.find { it.id == rewardId }!!
    }

    fun getRewardID(reward: ItemStack): Int {
        return rewards.find { it.item.isSimilar(reward) }!!.id
    }

    fun getRandomReward(): Reward {
        return getRewardByChance(this)
    }

    fun placeCrate(location: Location) {
        locations.add(location)

        registerHologram(location)

        update()
    }

    fun breakCrate(location: Location): Boolean {
        if (locations.isEmpty() || !locations.contains(location))
            return false

        locations.remove(location)

        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            val holoname = "$name${location.x}${location.y}${location.z}"
            hologramAPI.hologramManager.getHologram(holoname).delete()
        }

        if (location.block != null || location.block.type == Material.AIR)
            location.block.type = Material.AIR

        update()
        return true
    }

    fun alterDisplay(newDisplay: String) {
        display = c(newDisplay)
        buildKey()
        buildBlock()

        locations.forEach {
            val holoname = "$name${it.x}${it.y}${it.z}"
            if (hologramAPI.hologramManager.containsHologram(holoname)) {
                hologramAPI.hologramManager.getHologram(holoname).delete()
                registerHologram(it)
            }
        }

        update()
    }

    fun alterRarity(newRarity: Rarity): Boolean {
        if (rarity == newRarity)
            return false

        val oldkey = key.clone()

        rarity = newRarity
        buildKey()
        buildBlock()

        updatePlayerKeys(this, oldkey, newRarity == Rarity.PREMIUM)
        updateCrateKeys(this, oldkey)

        load()
        update()

        return true
    }

    fun updateKeys(crate: Crate, oldKey: ItemStack) {
        rewards.forEach { if (it.item.isSimilar(oldKey)) it.alterKey(crate.key) }
        update()
    }

    fun enable(): Boolean {
        return if (!isEnabled) {
            isEnabled = true
            update()
            true
        } else false
    }

    fun disable(): Boolean {
        return if (isEnabled) {
            isEnabled = false
            update()
            true
        } else false
    }

    fun clearLocations() {
        val locs = locations.toList()
        locs.forEach { it.block.type = Material.AIR; breakCrate(it) }
        update()
    }

    fun clearRewards() {
        rewards.toList().forEach { remReward(it) }
        rewards.add(addRewardDB(id, key))

        update()
    }

    fun load() {
        locations.forEach {
            if (it.block == null || it.block!!.isEmpty || it.block!!.type != rarity.material) {
                it.block.type = rarity.material
            }
            if (hologramAPI.hologramManager.containsHologram("$name${it.x}${it.y}${it.z}"))
                hologramAPI.hologramManager.getHologram("$name${it.x}${it.y}${it.z}").updateAll()
            else registerHologram(it)
        }
    }

    private fun buildKey() {
        val itemKey = ItemStack(Material.TRIPWIRE_HOOK)
        val metaKey = itemKey.itemMeta
        val lore = mutableListOf<String>()

        metaKey.displayName = c("&f&lKey &r$display&r")

        lore.add(c("&7&oUtilize essa key" + "&r"))
        lore.add(c("&7&oem &f&o/warp crates&7&o." + "&r"))
        lore.add(c(rarity.displayItalic + "&r"))
        metaKey.lore = lore

        metaKey.addEnchant(Enchantment.LUCK, id, true)
        metaKey.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        itemKey.setItemMeta(metaKey)

        if (!itemKey.isSimilar(key)) {
            key = itemKey
            update()
        }
    }

    private fun buildBlock() {
        val itemBlock = ItemStack(rarity.material)
        val meta = itemBlock.itemMeta
        val lore: MutableList<String> = ArrayList()

        meta.displayName = c("&f&lCrate &r$display&r")

        lore.add(c("&7&oKey Utilizada: &f&lKey &r$display&r"))
        lore.add(c(rarity.display + "&r"))
        meta.lore = lore

        meta.addEnchant(Enchantment.LUCK, id, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        itemBlock.setItemMeta(meta)

        if (!itemBlock.isSimilar(block)) {
            block = itemBlock
            update()
        }
    }

    fun registerHologram(location: Location) {
        val titleLoc = location.clone()
        titleLoc.add(0.5, 1.75, 0.5)

        val holoname = "$name${location.x}${location.y}${location.z}"
        val holo = Hologram(holoname, titleLoc, true)
        holo.getPage(0).addLine(HologramLine(holo.getPage(0), titleLoc, getDisplayCrate()))
        holo.save()


        hologramAPI.hologramManager.registerHologram(holo)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Crate) return false

        if (id != other.id || name != other.name || rarity != other.rarity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + rarity.hashCode()
        return result
    }

    fun getCrateModel(): CrateModel {
        return CrateModel(id, name, display, rarity, isEnabled, locations, key, block, id.toLong())
    }

    fun update() {
        updateCrateDatabase(this)
    }
}