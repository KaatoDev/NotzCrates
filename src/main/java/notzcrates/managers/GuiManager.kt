package notzcrates.managers


import notzcrates.Main.Companion.msgf
import notzcrates.enums.Rarity
import notzcrates.znotzapi.NotzAPI.Companion.itemManager
import notzcrates.znotzapi.apis.NotzItems
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object GuiManager {
    fun loadItems() {
        Rarity.entries.forEach { for (i in 0..2) { msgf.config.set("items.${it.name.lowercase()}${if (i>0)i else ""}.material", it.material.name) } }
        msgf.saveConfig()

        val items = "voltar sair criar warp info1 info2 info3 save raridade deletar rewards normal rare epic premium normal1 rare1 epic1 premium1 normal2 rare2 epic2 premium2 delete cancelar set01% -01% -1% -10% +10% +1% +01% set100% set1 -1 -10 set32 +10 +1 set64 crates keys pinv nokeys nokeys2 virtualon virtualoff noton notoff settings vazio cheio rkeyon rkeyoff crateonedit crateoffedit".split(" ")
        val items2 = itemManager.buildItemsFromFile(items.toSet()).toList()

        items.forEach { itemManager.addItem(it, items2[items.indexOf(it)]) }
    }

    fun getPlayerHead(p: Player): ItemStack {
        return NotzItems.buildItem(
            NotzItems.getHead(p).clone(),
            "&a&lVer Inventário",
            listOf("&fClique aqui para ver", "&fo seu inventário."),
            false
        )
    }
}