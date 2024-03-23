package notzcrates.events

import notzcrates.Main.Companion.crateGui
import notzcrates.Main.Companion.started
import notzcrates.enums.Rarity
import notzcrates.managers.CrateManager.getAllLocations
import notzcrates.managers.CrateManager.getCrateByBlock
import notzcrates.managers.CrateManager.getCrateByKey
import notzcrates.managers.CrateManager.getCrateByLocation
import notzcrates.managers.CrateManager.getCrates
import notzcrates.managers.CrateManager.isCrateBlock
import notzcrates.managers.CrateManager.isCrateKey
import notzcrates.managers.CrateManager.repulsePlayer
import notzcrates.managers.PlayerManager.getPlayer
import notzcrates.znotzapi.utils.MenuU.openMenu
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*

class KeyEv : Listener {
    private val contador = HashMap<UUID, Long>()
    private var cont = 0

    @EventHandler
    fun openKey(e: PlayerInteractEvent) {
        if (!started) {
            e.isCancelled = true
            return
        }

        if (e.clickedBlock != null && getCrates().isNotEmpty() && getAllLocations().contains(e.clickedBlock.location) && Rarity.entries.any { it.material == e.clickedBlock.type }) {
            if (spam(e.player)) {
                val p = e.player
                repulsePlayer(p)
                send(e.player, "spamGUI")
                e.isCancelled = true
                return
            }

            contador[e.player.uniqueId] = System.currentTimeMillis()
            cont = 0

            if (e.item == null) {
                if (e.action == RIGHT_CLICK_BLOCK) {
                    val p = e.player

                    if (!getPlayer(p).containsKey(getCrateByLocation(e.clickedBlock.location), p.isSneaking)) {
                        repulsePlayer(p)
                        send(p, "&cVocê não está segurando uma key na mão!")
                    }

                } else if (e.action == LEFT_CLICK_BLOCK)
                    openMenu(e.player, crateGui.previewCrate(getCrateByLocation(e.clickedBlock.location)))

                e.isCancelled = true
                return
            }

            if (isCrateBlock(e.item) && getCrateByBlock(e.item)!!.locations.contains(e.clickedBlock.location))
                return

            e.isCancelled = true

            val p = e.player
            val pm = getPlayer(p)
            val crate = getCrateByLocation(e.clickedBlock.location)

            if (!crate.isEnabled) {
                send(p, "&eA ${crate.getDisplayCrate()}&e está temporariamente desativada.")
                return
            }

            when (e.action) {
                RIGHT_CLICK_BLOCK -> {
                    if (crate.isVirtual()) {
                        if (!pm.claimVirtualKey(crate, p.isSneaking))
                            send(p, "&cNão foi possível abrir esta crate! Contate um staff superior.\n&7[Erro: keyev78]\n")

                    } else {
                        if (p.itemInHand == null || !isCrateKey(p.itemInHand)) {
                            repulsePlayer(p)
                            send(p, "&cVocê não está segurando uma key na mão!")

                        } else if (isCrateKey(p.itemInHand) && getCrateByKey(p.itemInHand)!! != crate) {
                            repulsePlayer(p)
                            send(p, "&eA key da sua mão é da ${getCrateByKey(p.itemInHand)!!.getDisplayCrate()}&e.")

                        } else {
                            if (!pm.claimKey(crate, e.item.amount, p.isSneaking))
                                send(p, "&cNão foi possível abrir esta crate! Contate um staff superior.\n&7[Erro: keyev91\n")
                        }
                    }
                }

                LEFT_CLICK_BLOCK -> {
                    openMenu(p, crateGui.previewCrate(crate))
                }

                else -> {}
            }

            return
        } else return
    }


    private fun spam(player: Player): Boolean {
        if (cont++ > 5) {
            player.kickPlayer("CALMA AE KRL")
            cont = 0
        }
        return contador.containsKey(player.uniqueId) && System.currentTimeMillis() - contador[player.uniqueId]!! < 1000
    }
}