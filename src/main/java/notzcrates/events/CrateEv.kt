package notzcrates.events

import notzcrates.Main
import notzcrates.managers.CrateManager.getAllLocations
import notzcrates.managers.CrateManager.getCrateByBlock
import notzcrates.managers.CrateManager.getCrateByLocation
import notzcrates.managers.CrateManager.isCrateBlock
import notzcrates.managers.CrateManager.verifyLocation
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class CrateEv : Listener {
    @EventHandler
    fun cratePlace(e: BlockPlaceEvent) {
        if (!Main.started) {
            e.isCancelled = true
            return
        }

        if (e.player.itemInHand != null && isCrateBlock(e.player.itemInHand)) {
            val p = e.player
            val blockItem = p.itemInHand
            val location = e.block.location

            if (!getAllLocations().contains(location) && verifyLocation(location)) {
                val crate = getCrateByBlock(blockItem)!!
                crate.placeCrate(location)

                send(p, "&aA ${crate.getDisplayCrate()}&a foi colocada com sucesso!")

            } else {
                send(p, "&cVocê não pode colocar uma crate perto de outra crate ou de algum baú!")
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun crateBreak(e: BlockBreakEvent) {
        if (!Main.started) {
            e.isCancelled = true
            return
        }

        if (e.player.itemInHand != null && isCrateBlock(e.player.itemInHand) && getCrateByBlock(e.player.itemInHand)!!.locations.contains(e.block.location)) {
            val p = e.player

            if (isCrateBlock(p.itemInHand)) {
                val location = e.block.location
                val crate = getCrateByLocation(location)

                if (crate.breakCrate(location)) {
                    send(p, "&eUma ${crate.getDisplayCrate()}&e foi &cquebrada &ecom sucesso!")

                } else {
                    send(p, "Não foi possível quebrar a crate.")
                    e.isCancelled = true
                }

            } else {
                send(p, "&cVocê só pode quebrar uma crate segurando o bloco de colocação dela!")
                e.isCancelled = true
            }
        }
    }
}