package notzcrates.events

import notzcrates.Main.Companion.crateGui
import notzcrates.managers.CrateManager.getCrateByBlock
import notzcrates.managers.CrateManager.isCrateBlock
import notzcrates.managers.CrateManager.isCrateReward
import notzcrates.managers.PlayerManager.getPlayer
import notzcrates.znotzapi.utils.MenuU.openInv
import notzcrates.znotzapi.utils.MenuU.openMenu
import notzcrates.znotzapi.utils.MenuU.resetLastMenu
import notzcrates.znotzapi.utils.MessageU.c
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import java.util.*

class CrateGuiEv : Listener {
    private val contador = HashMap<UUID, Long>()
    private var cont = 0

    @EventHandler
    fun cratesMenu(e: InventoryClickEvent) {
        if (e.currentItem == null || e.currentItem.type == Material.AIR || !pass(e.inventory.title))
            return

        val p = e.whoClicked as Player
        val pc = getPlayer(p)

        if (e.click == ClickType.DOUBLE_CLICK)
            return

        if (spam(p)) {
            send(p, "spamGUI")
            e.isCancelled = true
            return
        }

        if (!p.isOnline) {
            e.isCancelled = true
            return
        }

        val title = e.view.title
        val item = e.currentItem.clone()
        contador[p.uniqueId] = System.currentTimeMillis()
        cont = 0

        p.updateInventory()
        e.isCancelled = true

        if (openMenu(p, item)) {
            return
        }

        val menus = hashMapOf(
            "crates" to c("${crateGui.crateTitle} &a&lDisponíveis"),
            "preview" to c(" &e&lPreview "),
            "rewards" to c(" &e&lRewards "),
            "playerrewards" to c("&e&lRecompensas &7[${pc.player.name}&7]"),
        )

        if (menus["menu"] == title) {
            resetLastMenu(p)

        } else if (menus["crates"] == title || title.contains(menus["preview"]!!)) {
            if (isCrateBlock(item)) {
                crateGui.clickToCrate(getCrateByBlock(item)!!, pc, e.isLeftClick)
            }

        } else if (title.contains(menus["rewards"]!!)) {
            if (e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (e.isShiftClick) {
                    val crate = getCrateByBlock(e.inventory.getItem(8))!!

                    if (e.clickedInventory === p.openInventory.bottomInventory) {

                        if (crate.addReward(item)) {
                            p.inventory.removeItem(item)
                            send(p, "&eReward &f${if (item.hasItemMeta()) item.itemMeta.displayName else item.type.name.lowercase()}&e de &fid ${crate.getRewardID(item)}&a adicionada &ecom sucesso!")
                        }

                        openInv(p, crateGui.editRewards(crate))

                    } else if (e.clickedInventory === p.openInventory.topInventory && checkFrameSlot(e.slot, e.inventory.size)) {
                        if (crate.rewards.size == 1) {
                            send(p, "&cVocê não pode deixar a lista de recompensa de uma crate vazia!")
                            return
                        }

                        send(p, "&eReward ${if (item.hasItemMeta()) item.itemMeta.displayName else item.type.name.lowercase()}&e de &fid ${crate.getRewardID(item)}&c removida &ecom sucesso!")
                        crate.remReward(item)

                        openInv(p, crateGui.editRewards(crate))
                    }
                }

            } else if (isCrateReward(item, getCrateByBlock(e.inventory.getItem(8))!!)) {
                crateGui.clickToReward(getCrateByBlock(e.inventory.getItem(8))!!, pc, item, e.isLeftClick)

            } else if (isCrateBlock(item)) {
                crateGui.clickToCrate(getCrateByBlock(e.inventory.getItem(8))!!, pc, e.isLeftClick)
            }

        } else if (title.contains(menus["playerrewards"]!!)) {
            if (e.slot > 8 && e.slot < e.inventory.size-9) {
                pc.claimReward(item, e.slot, title.substringAfterLast('#').toInt())
            }
        }
    }

    @EventHandler
    fun openInventory(e: InventoryOpenEvent) {
        if (pass(e.inventory.title))
            contador.remove(e.player.uniqueId)
    }


    private fun pass(title: String): Boolean {
        return title.contains(crateGui.crateTitle)
    }

    private fun spam(player: Player): Boolean {
        if (cont++ > 5) {
            player.kickPlayer("CALMA AE KRL")
            cont = 0
        }
        return contador.containsKey(player.uniqueId) && System.currentTimeMillis() - contador[player.uniqueId]!! < 500
    }

    private fun checkFrameSlot(slot: Int, size: Int): Boolean {
        val slots = "17 18 26 27 35 36 44 46 53 54 62 63".split(" ").map { it.toInt() }.toList()
        return slot > 9 && slot < size-10 && !slots.contains(slot)
    }
}