package notzcrates.managers

import notzcrates.enums.Rarity
import org.bukkit.Material

object RarityManager {
    fun setRarityBlock(rarity: Rarity, material: Material) {
        rarity.material = material
    }
}