package notzcrates.enums

import notzcrates.znotzapi.utils.MessageU.c
import org.bukkit.Material

enum class Rarity(color: String, var material: Material, var isVirtual: Boolean) {
    NORMAL("&a", Material.CHEST, false),
    RARE("&b", Material.TRAPPED_CHEST, false),
    EPIC("&6", Material.ENDER_CHEST, false),
    PREMIUM("&5", Material.ENDER_PORTAL_FRAME, true);

    var display: String = c(color + this.name)
    var displayItalic: String = c(color + "&o" + this.name)

}