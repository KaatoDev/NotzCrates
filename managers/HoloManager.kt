package notzcrates.managers

import notzcrates.znotzapi.apis.NotzItems.buildItem
import org.bukkit.Material
import org.bukkit.entity.Player

object HoloManager {
    val holoRemover = buildItem(Material.STICK, "&6&L&O&K;;; &6&l&OREMOVEDOR DE HOLÃO &6&L&O&K;;;", listOf(), true)

    fun getHoloRemover(p: Player) {
        p.inventory.addItem(holoRemover.clone())
    }
}