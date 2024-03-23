package notzcrates.entities

import notzcrates.Main.Companion.crateGui
import notzcrates.managers.CrateManager.getCrateById
import notzcrates.managers.CrateManager.getCrateByKey
import notzcrates.managers.CrateManager.isCrateKey
import notzcrates.managers.CrateManager.repulsePlayer
import notzcrates.managers.DatabaseManager.updatePlayerDB
import notzcrates.znotzapi.apis.NotzItems.glass
import notzcrates.znotzapi.utils.MenuU.openInv
import notzcrates.znotzapi.utils.MessageU.c
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import java.io.Serializable

data class PlayerCrate(val id: Int, val player: OfflinePlayer, var isNotify: Boolean, var isVirtual: Boolean, val rewards: MutableList<ItemStack>, val keys: HashMap<Int, Int>) {
    data class PlayerCrateModel(val id: Int, val player: OfflinePlayer, val isNotify: Boolean, val isVirtual: Boolean, val rewards: MutableList<ItemStack>, val keys: HashMap<Int, Int>, val serialVersionUID: Long) : Serializable
    constructor(id: Int, player: OfflinePlayer) : this(id, player, true, false, mutableListOf(), hashMapOf())

    fun enableNotify() {
        if (!isNotify) {
            isNotify = true
            update()
            send(player.player, "&eNotificações &aativadas&e!")

        } else send(player.player, "&eAs suas notificações já estão &cdesativadas&e.")
    }

    fun disableNotify() {
        if (isNotify) {
            isNotify = false
            update()
            send(player.player, "&eNotificações &cdesativadas&e!")

        } else send(player.player, "&eAs suas notificações já estão &aativadas&e.")
    }

    fun enableVirtual() {
        if (!isVirtual) {
            isVirtual = true
            update()
            send(player.player, "&eInventário virtual &aativado&e!")

        } else send(player.player, "&eO seu inventário virtual já está &aativado&e.")
    }

    fun disableVirtual() {
        if (isVirtual) {
            isVirtual = false
            update()
            send(player.player, "&eInventário virtual &cdesativado&e!")

        } else send(player.player, "&eO seu inventário virtual já está &cdesativado&e.")
    }

    private fun addReward(rewards: MutableList<Reward>) {
        if (isVirtual)
            addRewardToVirtual(rewards)

        else while (player.player.inventory.firstEmpty() > -1) {
            rewards.toList().forEach { addReward(it); rewards.remove(it) }
        }

        if (rewards.isNotEmpty())
            addRewardToVirtual(rewards)
    }

    private fun addReward(reward: Reward) {
        if (isCrateKey(reward.item) && getCrateById(reward.crateID)!!.isVirtual()) {
            if (keys.containsKey(reward.crateID))
                keys[reward.crateID] = keys[reward.crateID]!! + reward.getQuantity()

            else keys[reward.crateID] = reward.getQuantity()

            return
        }

        if (isVirtual) {
            addRewardToVirtual(reward)
            return
        } else addToInventoy(reward)

        update()
    }

    private fun addRewardToVirtual(reward: Reward) {
        addRewardToVirtual(reward.item, reward.getQuantity())
    }

    private fun addRewardToVirtual(rewardList: List<Reward>) {
        val items = hashMapOf<ItemStack, Int>()

        rewardList.forEach { if (items.contains(it.item)) items[it.item] = items[it.item]!! + it.getQuantity() else items[it.item] = it.getQuantity()}
        items.forEach { addRewardToVirtual(it.key, it.value) }
    }

    private fun addRewardToVirtual(reward: ItemStack, quantity: Int) {
        val rewItem = reward.clone()
        rewItem.amount = quantity

        rewards.add(rewItem)
        organizeRewards()
    }

    // player.player.inventory.firstEmpty() -> sempre será {-1} caso cheio
    fun claimReward(reward: ItemStack, slot: Int, page: Int) {
        if (player.player.openInventory.topInventory.getItem(slot).isSimilar(glass(0)))
            return

        val ss = slot - (slot/9)*2 - 8

        val index = if (page == 1) when (slot) {
            17, 18, 26, 27, 35, 36, 44 -> return
            else -> ss

        } else (page-1)*36 + slot-9

        if (rewards.isEmpty()) {
            send(player.player, "&cErro de rewards vazio. Contate um staff superior!\n &7[Erro playercrate160]")
            return
        }

        if (rewards[index] == reward) {
            if (isCrateKey(reward) && !getCrateByKey(reward)!!.isVirtual()) {
                addKey(reward)

                rewards.removeAt(index)
                openInv(player.player, crateGui.playerRewards(this))

                update()

            } else if (isCrateKey(reward)) {
                addKey(reward)
            } else if (player.player.inventory.firstEmpty() > -1 && player.player.inventory.addItem(rewards[index]).isEmpty()) {

                if (isNotify) send(player.player, "&aA recompensa foi resgatada!")

                rewards.removeAt(index)
                openInv(player.player, crateGui.playerRewards(this))

                update()
            } else send(player.player, "&eO seu inventário já está cheio!")
        } else send(player.player, "&cNão foi possível resgatar a recompensa.")
    }

    fun claimRewards() {
        if (player.player.inventory.firstEmpty() == -1) {
            send(player.player, "&cEsvazie seu inventário para resgatar suas recompensas.")
            return
        }

        rewards.toList().forEach {
            if (addToInventoy(it)) {
                rewards.remove(it)

            } else return
        }
    }

    fun claimRewardsKeys() {
        if (player.player.inventory.firstEmpty() == -1 || rewards.filter { isCrateKey(it) }.any { val item = it.clone(); item.amount = it.maxStackSize - it.amount; player.player.inventory.contains(item) }) {
            send(player.player, "&cEsvazie seu inventário para resgatar suas recompensas.")
            return
        }

        val rews = rewards.filter { isCrateKey(it) }
        rews.forEach {
            if (player.player.inventory.firstEmpty() > -1 && addKey(it)) {
                rewards.removeFirst()

            } else return
        }
    }

    fun containsKey(crate: Crate, isShift: Boolean): Boolean {
        if (!crate.isVirtual())
            return false

        if (keys.containsKey(crate.id)) {
            claimVirtualKey(crate,  isShift)

        } else send(player.player, "&cVocê não possui key desta crate!")

        return true
    }

    fun addKey(key: ItemStack): Boolean {
        return getCrateByKey(key) != null && addKey(getCrateByKey(key)!!, key.amount)
    }

    fun addKey(crate: Crate, quantity: Int): Boolean {
        return try {
            if (crate.isVirtual()) {
                if (keys.keys.contains(crate.id)) {
                    keys[crate.id] = keys[crate.id]!! + quantity

                } else keys[crate.id] = quantity

            } else {
                val item = crate.key.clone()
                item.amount = quantity

                player.player.inventory.addItem(item)
            }

            update()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun claimVirtualKey(crate: Crate, isShift: Boolean): Boolean {
        val p = player.player

        return try {
            if (keys.containsKey(crate.id) && keys[crate.id]!! > 0) {
                if (isShift && keys[crate.id]!! > 1) {
                    val tempRewards = hashMapOf<Reward, Int>()

                    for (i in 1..keys[crate.id]!!) {
                        val rew = crate.getRandomReward()

                        addReward(rew)

                        if (tempRewards.containsKey(rew))
                            tempRewards[rew] = tempRewards[rew]!! + 1
                        else tempRewards[rew] = 1
                    }

                    if (isNotify) {
                        send(player.player, "&eVocê abriu uma ${crate.getDisplayCrate()}&e e recebeu:")
                        tempRewards.forEach {
                            p.sendMessage(c("&a+ &f${if (it.key.item.hasItemMeta()) it.key.item.itemMeta.displayName else it.key.item.type.name.lowercase()}&f: ${it.key.getQuantity() * it.value}"))
                        }
                    }

                } else {
                    if (keys[crate.id]!! > 1)
                        keys[crate.id] = keys[crate.id]!! - 1
                    else keys.remove(crate.id)

                    val reward = crate.getRandomReward()
                    addReward(reward)

                    if (isNotify) send(p, "&eVocê abriu uma ${crate.getDisplayCrate()}&e e recebeu: ${if (reward.item.hasItemMeta()) reward.item.itemMeta.displayName else reward.item.type.name.lowercase()}")
                }

            } else {
                send(p, "&cVocê não possui uma ${crate.getDisplayKey()}&c armazenada.")
                repulsePlayer(p)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    fun claimKey(crate: Crate, quantity: Int, isShift: Boolean): Boolean {
        val p = player.player

        return try {
            val key = crate.key.clone()

            if (isShift) {
                val tempRewards = hashMapOf<Reward, Int>()

                for (i in 1..quantity) {
                    val rew = crate.getRandomReward()

                    addReward(rew)

                    if (tempRewards.containsKey(rew))
                        tempRewards[rew] = tempRewards[rew]!! + 1
                    else tempRewards[rew] = 1
                }

                if (isNotify) {
                    send(player.player, "&eVocê abriu uma ${crate.getDisplayCrate()}&e e recebeu:")
                    tempRewards.forEach {
                        p.sendMessage(c("&a+ &f${if (it.key.item.hasItemMeta()) it.key.item.itemMeta.displayName else it.key.item.type.name.lowercase()}&f: ${it.key.getQuantity() * it.value}"))
                    }
                }

                key.amount = quantity
                p.inventory.removeItem(key)

            } else {
                key.amount = 1
                p.inventory.removeItem(key)

                val reward = crate.getRandomReward()
                addReward(reward)

                if (isNotify) send(p, "&eVocê abriu uma ${crate.getDisplayCrate()}&e e recebeu: ${if (reward.item.hasItemMeta()) reward.item.itemMeta.displayName else reward.item.type.name.lowercase()}")

            }


            player.player.playSound(player.player.location, Sound.LEVEL_UP, 100f, 1f)

            update()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateKeys(crate: Crate, oldKey: ItemStack, toVirtual: Boolean) {
        if (rewards.isNotEmpty())
            rewards.indices.filter { rewards[it].isSimilar(oldKey) }.forEach { rewards[it].setItemMeta(crate.key.itemMeta) }

        if (keys.isNotEmpty() && keys.containsKey(crate.id)) {
            val keyItem = crate.key.clone()
            keyItem.amount = keys[crate.id]!!
            keys.remove(crate.id)
            rewards.add(keyItem)
            organizeRewards()
        }

        if (player.isOnline) {
            player.player.inventory.contents.toList().filter {it != null && it.isSimilar(oldKey)}.forEach {
                addKey(crate, it.amount)
                player.player.inventory.removeItem(it)
            }
        }

        update()
    }

    fun addToInventoy(rewItem: ItemStack): Boolean {
        return try {
            val p = player.player

            if (p.inventory.firstEmpty() > -1) {
                p.inventory.addItem(rewItem)

            } else if (p.inventory.any { it.isSimilar(rewItem) && it.maxStackSize > 1 && it.amount < it.maxStackSize }) {
                var amo = 0
                p.inventory.filter { it != null && it.isSimilar(rewItem) && it.amount < it.maxStackSize }
                    .forEach { amo += it.maxStackSize - it.amount }

                if (amo != 0 && amo < rewItem.amount) {
                    rewItem.amount = amo
                    p.inventory.addItem(rewItem)
                    addRewardToVirtual(rewItem, rewItem.amount - amo)

                } else p.inventory.addItem(rewItem)

            } else return false

            true
        } catch (e: Exception) {
            false
        }
    }

    fun addToInventoy(reward: Reward): Boolean {
        return try {
            val rewItem = reward.item.clone()
            rewItem.amount = reward.getQuantity()

            if (!addToInventoy(rewItem))
                addRewardToVirtual(reward)

            true
        } catch (e: Exception) {
            false
        }
    }

    fun organizeRewards() {
        val size = if (rewards.size < 9) 9 else rewards.size + 9 - rewards.size % 9
        val inv = Bukkit.createInventory(player.player, size)

        rewards.forEach { inv.addItem(it) }
        rewards.clear()
        rewards.addAll(inv.contents.filterNotNull())
    }

    fun getPlayerCrateModel(): PlayerCrateModel {
        return PlayerCrateModel(id, player, isNotify, isVirtual, rewards, keys, id.toLong())
    }

    fun update() {
        updatePlayerDB(this)
    }
}