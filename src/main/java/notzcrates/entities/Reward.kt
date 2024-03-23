package notzcrates.entities

import notzcrates.managers.DatabaseManager.updateRewardDB
import org.bukkit.inventory.ItemStack
import java.io.Serializable

data class Reward(val id: Int, val crateID: Int, val item: ItemStack, private var chance: Double, private var quantity: Int) {
    data class RewardModel(val id: Int, val crateID: Int, val item: ItemStack, val chance: Double, val quantity: Int, val serialVersionUID: Long) : Serializable
    constructor(id: Int, crate: Int, item: ItemStack) : this(id, crate, item, 10.0, item.amount)

    fun alterKey(key: ItemStack) {
        item.setItemMeta(key.itemMeta.clone())
        update()
    }

    fun getChance(): Double {
        return chance
    }

    fun getQuantity(): Int {
        return quantity
    }

    fun setChance(chanc: Double) {
        chance = chanc
        update()
    }

    fun setQuantity(amount: Int) {
        quantity = amount
        update()
    }

    fun getRewardModel(): RewardModel {
        return RewardModel(id, crateID, item, chance, quantity, id.toLong())
    }

    fun update() {
        updateRewardDB(this)
    }
}