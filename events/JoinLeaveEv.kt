package notzcrates.events

import notzcrates.managers.PlayerManager.loginPlayer
import notzcrates.managers.PlayerManager.logoutPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinLeaveEv : Listener {
    @EventHandler
    fun joinEvent(e: PlayerJoinEvent) {
        loginPlayer(e.player)
    }

    @EventHandler
    fun joinEvent(e: PlayerQuitEvent) {
        logoutPlayer(e.player)
    }
}