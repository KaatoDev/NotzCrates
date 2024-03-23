package notzcrates.events

import notzcrates.managers.HoloManager.holoRemover
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class HoloEv : Listener {
    @EventHandler
    fun holoClickEntity(e: PlayerInteractAtEntityEvent) {
        if (e.player.hasPermission("notzcrates.admin")) {
            if (e.player.itemInHand != null && !e.player.itemInHand.isSimilar(holoRemover))
                return

            if (e.rightClicked is ArmorStand) {
                send(e.player, "&eHolograma removido! (&f${e.rightClicked.location.x.toInt()}&e, &f${e.rightClicked.location.y.toInt()}&e, &f${e.rightClicked.location.z.toInt()})")
                e.rightClicked.remove()
            }

            e.isCancelled = true
        }
    }

    @EventHandler
    fun holoClickBlock(e: PlayerInteractEvent) {
        if (e.item != null && e.player.hasPermission("notzcrates.admin") && e.item.isSimilar(holoRemover))
            e.isCancelled = true
    }
}