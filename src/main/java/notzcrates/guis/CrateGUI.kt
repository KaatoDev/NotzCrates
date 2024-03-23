package notzcrates.guis

import notzcrates.entities.Crate
import notzcrates.entities.PlayerCrate
import notzcrates.enums.Rarity
import notzcrates.enums.Rarity.*
import notzcrates.managers.CrateManager.deleteCrate
import notzcrates.managers.CrateManager.getCrateByBlock
import notzcrates.managers.CrateManager.getCrateById
import notzcrates.managers.CrateManager.getCrateByTitle
import notzcrates.managers.CrateManager.getCrates
import notzcrates.managers.CrateManager.isCrateKey
import notzcrates.managers.CrateManager.isCrateTitle
import notzcrates.managers.GuiManager.getPlayerHead
import notzcrates.managers.GuiManager.loadItems
import notzcrates.managers.PlayerManager.getPlayer
import notzcrates.znotzapi.NotzAPI.Companion.itemManager
import notzcrates.znotzapi.apis.NotzGUI
import notzcrates.znotzapi.apis.NotzItems.buildItem
import notzcrates.znotzapi.apis.NotzItems.getHead
import notzcrates.znotzapi.apis.NotzItems.glass
import notzcrates.znotzapi.gui.ChestPages
import notzcrates.znotzapi.utils.EventU.setFunction
import notzcrates.znotzapi.utils.MenuU.getLastMenu
import notzcrates.znotzapi.utils.MenuU.openInv
import notzcrates.znotzapi.utils.MenuU.openMenu
import notzcrates.znotzapi.utils.MenuU.resetLastMenu
import notzcrates.znotzapi.utils.MessageU.c
import notzcrates.znotzapi.utils.MessageU.send
import org.bukkit.Material.REDSTONE
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CrateGUI {
    val crateTitle = c("&6&l[&f&lCrates&6&l]&r")

    private var crates: List<Crate> = getCrates()
    private var cratesSort: List<Crate> = crates.sortedBy { it.name }.sortedBy { it.getRarity() }

    fun update() {
        crates = getCrates()
        cratesSort = crates.sortedBy { it.name }.sortedBy { it.getRarity() }
    }

    init {
        loadItems()

        // --------- FUNCTIONS
        setFunction("crates") { openMenu(it, crateList(getPlayer(it))) }

        setFunction("criar") { it.performCommand("ncrates create") }
        setFunction("warp") { it.performCommand("warp crates") }

        setFunction("rewards") { openMenu(it, editRewards(getCrates().find { crate -> it.openInventory.topInventory.title == c("$crateTitle &e&lEdit ${crate.getDisplayCrate()}") }!!)) }
        setFunction("raridade") { openMenu(it, editRarity(getCrates().find { crate -> it.openInventory.topInventory.title == c("$crateTitle &e&lEdit ${crate.getDisplayCrate()}") }!!)) }
        setFunction("deletar") { openMenu(it, confirmDel(getCrates().find { crate -> it.openInventory.topInventory.title == c("$crateTitle &e&lEdit ${crate.getDisplayCrate()}") }!!)) }
        setFunction("crateonedit") { val crate = getCrateByTitle(it.openInventory.topInventory.title, "$crateTitle &e&lEdit crate.getDisplayCrate()")!!; crate.disable(); openInv(it, editCrate(getPlayer(it), crate)) }
        setFunction("crateoffedit") { val crate = getCrateByTitle(it.openInventory.topInventory.title, "$crateTitle &e&lEdit crate.getDisplayCrate()")!!; crate.enable(); openInv(it, editCrate(getPlayer(it), crate)) }

        setFunction("normal") { openMenu(it, editRarity(getCrateByBlock(it.openInventory.topInventory.getItem(8))!!, NORMAL)) }
        setFunction("rare") { openMenu(it, editRarity(getCrateByBlock(it.openInventory.topInventory.getItem(8))!!, RARE)) }
        setFunction("epic") { openMenu(it, editRarity(getCrateByBlock(it.openInventory.topInventory.getItem(8))!!, EPIC)) }
        setFunction("premium") { openMenu(it, editRarity(getCrateByBlock(it.openInventory.topInventory.getItem(8))!!, PREMIUM)) }

        setFunction("delete") {
            val crate = getCrates().find { cr -> it.openInventory.topInventory.title == c("$crateTitle &4&lDelete ${cr.getDisplayCrate()}&f&l?") }!!
            val name = crate.name

            deleteCrate(crate)
            send(it, "&eA crate &f$name&e foi &cexcluída &ecom sucesso!")
            it.closeInventory()
        }
        setFunction("cancelar") { if (getCrates().any { crate -> it.openInventory.topInventory.title == c("$crateTitle &4&lDelete ${crate.getDisplayCrate()}&f&l?") }) getLastMenu(it) }

        setFunction("rkeyon") { val pc = getPlayer(it); pc.claimRewardsKeys(); openInv(it, playerRewards(getPlayer(it))) }
        setFunction("settings") { openMenu(it, playerSettings(getPlayer(it))) }
        setFunction("keys") { openMenu(it, playerKeys(getPlayer(it))) }
        setFunction("cheio") { val pc = getPlayer(it); pc.claimRewards(); openMenu(it, playerRewards(pc)) }

        setFunction("virtualon") { val pc = getPlayer(it); pc.disableVirtual(); openInv(it, playerSettings(pc)) }
        setFunction("virtualoff") { val pc = getPlayer(it); pc.enableVirtual(); openInv(it, playerSettings(pc)) }
        setFunction("noton") { val pc = getPlayer(it); pc.disableNotify(); openInv(it, playerSettings(pc)) }
        setFunction("notoff") { val pc = getPlayer(it); pc.enableNotify(); openInv(it, playerSettings(pc)) }

        setFunction("pinv") { openMenu(it, playerRewards(getPlayer(it))) }

        setFunction("set01%") { setInvItemChance(it.openInventory.topInventory.getItem(13), 0.1) }
        setFunction("-01%") { addInvItemChance(it.openInventory.topInventory.getItem(13), -0.1) }
        setFunction("-1%") { addInvItemChance(it.openInventory.topInventory.getItem(13), -1.0) }
        setFunction("-10%") { addInvItemChance(it.openInventory.topInventory.getItem(13), -10.0) }
        setFunction("+10%") { addInvItemChance(it.openInventory.topInventory.getItem(13), 10.0) }
        setFunction("+1%") { addInvItemChance(it.openInventory.topInventory.getItem(13), 1.0) }
        setFunction("+01%") { addInvItemChance(it.openInventory.topInventory.getItem(13), 0.1) }
        setFunction("set100%") { setInvItemChance(it.openInventory.topInventory.getItem(13), 100.0) }

        setFunction("set1") { setInvItemQuantity(it.openInventory.topInventory.getItem(13), 1) }
        setFunction("-1") { addInvItemQuantity(it.openInventory.topInventory.getItem(13), -1) }
        setFunction("-10") { addInvItemQuantity(it.openInventory.topInventory.getItem(13), -10) }
        setFunction("set32") { setInvItemQuantity(it.openInventory.topInventory.getItem(13), it.openInventory.topInventory.getItem(13).maxStackSize/2) }
        setFunction("+10") { addInvItemQuantity(it.openInventory.topInventory.getItem(13), 10) }
        setFunction("+1") { addInvItemQuantity(it.openInventory.topInventory.getItem(13), 1) }
        setFunction("set64") { setInvItemQuantity(it.openInventory.topInventory.getItem(13), it.openInventory.topInventory.getItem(13).maxStackSize) }

        // ---- SAVE ---- SAVE ---- SAVE ---- SAVE ---- SAVE
        setFunction("save") { p ->
            if (getCrateByBlock(p.openInventory.topInventory.getItem(8)) == null) {
                send(p, "&cNão foi possível encontrar a crate.")
                return@setFunction
            }

            val crate: Crate = getCrateByBlock(p.openInventory.topInventory.getItem(8))!!
            val title = p.openInventory.topInventory.title

            if (isCrateTitle(crate, title, "$crateTitle &e&lEdit Rarity {crate}")) {
                val rarity: Rarity? =
                    if (p.openInventory.getItem(10) == itemManager.getItem("normal2")) NORMAL
                    else if (p.openInventory.getItem(12) == itemManager.getItem("rare2")) RARE
                    else if (p.openInventory.getItem(14) == itemManager.getItem("epic2")) EPIC
                    else if (p.openInventory.getItem(16) == itemManager.getItem("premium2")) PREMIUM
                    else null

                if (rarity != null) {
                    crate.alterRarity(rarity)
                    openInv(p, editRarity(crate))
                }

            } else if (isCrateTitle(crate, title, "$crateTitle &e&lEdit Chance {crate}")) {
                if (saveRewardChance(p.openInventory.topInventory.getItem(2), p.openInventory.topInventory.getItem(13), crate))
                    send(p, "&aChance do item alterada com sucesso!")
                else send(p, "&cNão foi possível alterar a chance do item.")

                openInv(p, editRewards(crate))

            } else if (isCrateTitle(crate, title, "$crateTitle &e&lEdit Quantity {crate}")) {
                if (saveRewardQuantity(p.openInventory.topInventory.getItem(13), crate))
                    send(p, "&aQuantidade do item alterada com sucesso!")
                else send(p, "&cNão foi possível alterar a quantidade do item.")

                openInv(p, editRewards(crate))
            }
        }
        // ---- SAVE ---- SAVE ---- SAVE ---- SAVE ---- SAVE
    }
    // --------- FUNCTIONS


    //---------------------------------- GERAL

    fun open(p: Player) {
        resetLastMenu(p)
        openMenu(p, crateMenu(p))
    }

    private fun crateMenu(p: Player): NotzGUI {
        val crateMenu = NotzGUI(null, 3, "crateMenu${p.name}", "$crateTitle &e&lMenu")
        crateMenu.setPanel(0, false)
        crateMenu.setPanel(1, true)
        crateMenu.setupExit()

        crateMenu.setItem(11, "crates")
        crateMenu.setItem(15, getPlayerHead(p))

        setFunction(getPlayerHead(p)) { openMenu(p, playerRewards(getPlayer(p)))}

        return crateMenu
    }

    fun clickToCrate(crate: Crate, playerCrate: PlayerCrate, isLeft: Boolean) {
        openMenu(playerCrate.player.player, if (isLeft) previewCrate(crate) else editCrate(playerCrate, crate))
    }

    fun clickToReward(crate: Crate, playerCrate: PlayerCrate, reward: ItemStack, isLeft: Boolean) {
        openMenu(playerCrate.player.player, if (isLeft) editQuantity(crate, crate.rewards.find { it.item.isSimilar( reward) }!!.id) else editChance(crate, crate.rewards.find { it.item.isSimilar( reward) }!!.id))
    }

    //---------------------------------- GERAL

//---------------------------------------------

    //----------------------------- CRATES

    private fun crateList(pc: PlayerCrate): NotzGUI {
        val isAdmin = pc.player.player.hasPermission("notzcrates.admin")
        val menu = NotzGUI(null, 6, null, "$crateTitle &a&lDisponíveis")

        menu.setPanel(0, true)
        menu.setup()

        menu.setItem(3, if (isAdmin) "info1" else "info2")
        menu.setItem(5, if (isAdmin) "criar" else "warp")

        val cratesOn = if (isAdmin) crates.map { it.block } else crates.filter { it.isEnabled }.map { it.block }
        menu.addItemsEach(cratesOn.toMutableList())

        return menu
    }

    fun previewCrate(crate: Crate): NotzGUI {
        val menu = NotzGUI(null, 6, null, "$crateTitle &e&lPreview ${crate.getDisplayCrate()}")

        menu.setPanel(0, true)
        menu.setup()

        menu.setItem(8, crate.block)
        menu.setItem(27, if (crate.isEnabled) "crateenabled" else "cratedisabled")
        menu.setItem(35, if (crate.isEnabled) "crateenabled" else "cratedisabled")

        menu.addItemsEach(crate.rewards.map { it.item }.toMutableList())

        return menu
    }

    private fun editCrate(pc: PlayerCrate, crate: Crate): NotzGUI {
        val menu = NotzGUI(null, 3, null, "$crateTitle &e&lEdit ${crate.getDisplayCrate()}")

        menu.setPanel(0, false)
        menu.setPanel(3, true)
        menu.setup()

        if (pc.player.player.hasPermission("notzcrats.admin")) {
            menu.setItem(9, if (crate.isEnabled) "crateonedit" else "crateoffedit")
            menu.setItem(17, if (crate.isEnabled) "crateonedit" else "crateoffedit")
        }

        setItems(menu, hashMapOf(
        11 to "rewards",
        13 to "raridade",
        15 to "deletar"
        ))

        return menu
    }

    private fun editRarity(crate: Crate): NotzGUI {
        val menu = NotzGUI(null, 3, null, "$crateTitle &e&lEdit Rarity ${crate.getDisplayCrate()}")

        menu.setPanel(3, false)
        menu.setup()

        menu.setItem(8, crate.block)
        menu.setItem("9 11 13 15 17", glass(11))

        setItems(menu, hashMapOf(
            10 to "normal",
            12 to "rare",
            14 to "epic",
            16 to "premium",
            22 to "save"
        ))

        when (crate.getRarity()) {
            NORMAL -> {
                menu.setItem(10, "normal1")
                menu.setPanel(11, "1 19")
            }
            RARE -> {
                menu.setItem(12, "rare1")
                menu.setPanel(11, "3 21")
            }
            EPIC -> {
                menu.setItem(14, "epic1")
                menu.setPanel(11, "5 23")
            }
            PREMIUM -> {
                menu.setItem(16, "premium1")
                menu.setPanel(11, "7 25")
            }
        }

        return menu
    }

    private fun editRarity(crate: Crate, selRarity: Rarity): NotzGUI {
        val menu = editRarity(crate)

        when (selRarity) {
            NORMAL -> {
                menu.setItem(10, "normal2")
                menu.setPanel(4, "1 19")
            }
            RARE -> {
                menu.setItem(12, "rare2")
                menu.setPanel(4, "3 21")
            }
            EPIC -> {
                menu.setItem(14, "epic2")
                menu.setPanel(4, "5 23")
            }
            PREMIUM -> {
                menu.setItem(16, "premium2")
                menu.setPanel(4, "7 25")
            }
        }

        return menu
    }

    private fun confirmDel(crate: Crate): NotzGUI {
        val menu = NotzGUI(null, 1, null, "$crateTitle &4&lDelete ${crate.getDisplayCrate()}&f&l?")

        menu.setPanel(0, false)
        menu.setup()

        menu.setItem(2, "delete")
        menu.setItem(6, "cancelar")

        return menu
    }


    fun editRewards(crate: Crate): NotzGUI {
        val menu = NotzGUI(null, 6, null, "$crateTitle &e&lRewards ${crate.getDisplayCrate()}")

        menu.setPanel(0, true)
        menu.setup()

        menu.setItem(4, "info3")
        menu.setItem(8, crate.block)

        if (crate.rewards.isNotEmpty())
            menu.addItemsEach(crate.rewards.map { it.item }.toMutableList())

        return menu
    }


    private fun editChance(crate: Crate, rewardID: Int): NotzGUI {
        val menu = NotzGUI(null, 5, null, "$crateTitle &e&lEdit Chance ${crate.getDisplayCrate()}")

        menu.setPanel(0, false)
        menu.setPanel(13, true)
        menu.setPanel(13, "4 13 22")
        menu.setPanel(3, "9-17 18-26")
        menu.setup()

        menu.setItem(2, crate.getRewardById(rewardID).item)
        menu.setItem(8, crate.block)
        menu.setItem(13,
            buildItem(
                REDSTONE,
                "&e&lChance: &f${crate.getRewardById(rewardID).getChance()}&e%",
                listOf("&7&oEsta é a chance do item."),
                false
            )
        )

        setItems(menu, hashMapOf(
            6 to "save",
            20 to "set01%",
            24 to "set100%",
            28 to "-01%",
            29 to "-1%",
            30 to "-10%",
            32 to "+10%",
            33 to "+1%",
            34 to "+01%"
        ))

        return menu
    }

    private fun editQuantity(crate: Crate, rewardID: Int): NotzGUI {
        val menu = NotzGUI(null, 4, null, "$crateTitle &e&lEdit Quantity ${crate.getDisplayCrate()}")
        val reward = crate.getRewardById(rewardID)
        val rew = reward.item.clone()
        rew.amount = reward.getQuantity()

        menu.setPanel(0, false)
        menu.setPanel(13, true)
        menu.setPanel(13, "4 13 22 31")
        menu.setPanel(3, "9-17 27-35")
        menu.setup()

        menu.setItem(2, reward.item.clone())
        menu.setItem(8, crate.block)
        menu.setItem(13, rew)

        setItems(menu, hashMapOf(
            6 to "save",
            19 to "set1",
            20 to "-1",
            21 to "-10",
            22 to "set32",
            23 to "+10",
            24 to "+1",
            25 to "set64"
        ))

        return menu
    }

    //----------------------------- CRATES

//---------------------------------------------

    //----------------------- PLAYER

    fun playerRewards(pc: PlayerCrate): NotzGUI {
        if (pc.rewards.isEmpty())
            return playerRewardsEmpty(pc)

        pc.organizeRewards()

        val menu = if (pc.rewards.size >  28)
            ChestPages(pc.player.player, "playerrewards${pc.player.name}", "$crateTitle &e&lRecompensas &7[${pc.player.name}&7]", 36, pc.rewards, true)
        else ChestPages(pc.player.player, "playerrewards${pc.player.name}", "$crateTitle &e&lRecompensas &7[${pc.player.name}&7]", 28, pc.rewards, true)

            menu.setItemPage(45, if (pc.rewards.any { isCrateKey(it) }) "rkeyon" else "rkeyoff")
            menu.setItemPage(47, "settings")
            menu.setItemPage(51, "keys")
            menu.setItemPage(53, if (pc.rewards.isNotEmpty()) "cheio" else "vazio")

            return menu.pageRaw(1)
    }

    private fun playerRewardsEmpty(pc: PlayerCrate): NotzGUI {
        val menu = NotzGUI(null, 3,null, "$crateTitle &e&lRecompensas &7[${pc.player.name}&7]")

        menu.setPanel(0, true)
        menu.setup()

        menu.setItem(8, getHead(pc.player.player))
        menu.setItem(18, "rkeyoff")
        menu.setItem(20, "settings")
        menu.setItem(24, "keys")
        menu.setItem(26, "vazio")

        return menu
    }

    private fun playerSettings(pc: PlayerCrate): NotzGUI {
        val menu = NotzGUI(null, 3, null, "$crateTitle &d&lSettings &7[${pc.player.name}]")

        val pvirtual = pc.isVirtual
        val pnotify = pc.isNotify
        
        menu.setPanel(0, false)
        menu.setup()

        menu.setItem("2 10 12 20", glass(if (pvirtual) 5 else 14))
        menu.setItem(11, if (pvirtual) "virtualon" else "virtualoff")
        menu.setItem("6 14 16 24", glass(if (pnotify) 5 else 14))
        menu.setItem(15, if (pnotify) "noton" else "notoff")
        return menu
    }

    private fun playerKeys(pc: PlayerCrate): NotzGUI {
        val menu = NotzGUI(null, pc.keys.size/7 + if (pc.keys.size%7 != 0) 3 else 2, null, "$crateTitle &d&lKeys &7[${pc.player.name}]")

            menu.setPanel(0, false)
            menu.setPanel(2, true)

        menu.setup()
        menu.setItem(8, getHead(pc.player.player))

        if (pc.keys.size < 6) {
            if (pc.keys.isNotEmpty()) {
                menu.setItem(4, "settings")

                val tempKeys = pc.keys.map {
                    buildItem(
                        getCrateById(it.key)!!.key.clone(),
                        null,
                        listOf("&fQuantidade: &a${it.value}"),
                        null
                    )
                }

                val slots = "13, 12 14, 11 13 15, 11 12 14 15, 10 11 13 15 16".split(", ").map { it.split(" ") }
                slots[pc.keys.size - 1].indices.forEach { menu.setItem(slots[pc.keys.size - 1][it], tempKeys[it]) }

            } else menu.setItem(4, "nokeys")
        } else menu.addItemsEach(pc.keys.map { buildItem(getCrateById(it.key)!!.key.clone(), null, listOf("&fQuantidade: &a${it.value}"), null) }.toMutableList())

        return menu
    }

    //----------------------- PLAYER

//---------------------------------------------

    //----------------- VIEW
    fun viewRewards(playerTarget: PlayerCrate): NotzGUI {
        val menu = NotzGUI(null, 7, null, "$crateTitle &e&lRewards do &f${playerTarget.player.name}")

        menu.setPanel(0, "54-62")

        menu.setItem(62, "save")

        menu.addItemsEach(playerTarget.rewards)

        return menu
    }

    fun viewKeys(playerTarget: PlayerCrate): NotzGUI {
        val menu = NotzGUI(null, 4, null, "$crateTitle &e&lKeys do &f${playerTarget.player.name}")

        menu.setPanel(0, "27-35")

        menu.setItem(35, "save")

        menu.addItemsEach(playerTarget.keys.map { val key = getCrateById(it.key)!!.key.clone(); key.amount = it.value; key }.toMutableList())

        return menu
    }

    //----------------- VIEW

    private fun setItems(menu: NotzGUI, items: HashMap<Int, String>) {
        items.forEach { menu.setItem(it.key, it.value)}
    }

    private fun setInvItemChance(item: ItemStack, percentage: Double) {
        val chanceString = item.itemMeta.displayName
        val chance = chanceString.substring(chanceString.indexOf(" §f")+3, chanceString.indexOf("§e%")).toDouble()

        if (chance != percentage) {
            val meta = item.itemMeta
            meta.displayName = c("&e&lChance: &f${String.format("%.2f", percentage)}&e%")
            item.setItemMeta(meta)
        }
    }

    private fun addInvItemChance(item: ItemStack, percentage: Double) {
        val chanceString = item.itemMeta.displayName
        val chance = chanceString.substring(chanceString.indexOf(" §f")+3, chanceString.indexOf("§e%")).toDouble()
        println(chanceString.substring(chanceString.indexOf(" §f") + 3, chanceString.indexOf("§e%")))
        println(percentage)
        println(percentage + chance)

        if (chance + percentage in 0.1..100.0)
           setInvItemChance(item, chance + percentage)
    }

    private fun saveRewardChance(reward: ItemStack, itemChance: ItemStack, crate: Crate): Boolean {
        val chanceString = itemChance.itemMeta.displayName
        val chance = chanceString.substring(chanceString.indexOf(" §f")+3, chanceString.indexOf("§e%")).toDouble()

        return try {
            crate.rewards.find { it.item.isSimilar(reward) }!!.setChance(chance)
            true

        } catch (e: Exception) {
            false
        }
    }

    private fun setInvItemQuantity(item: ItemStack, quantity: Int) {
        item.amount = quantity
    }

    private fun addInvItemQuantity(item: ItemStack, quantity: Int) {
        if (item.amount + quantity < 1)
            setInvItemQuantity(item, 1)

        else if (item.amount + quantity > item.maxStackSize)
            setInvItemQuantity(item, item.maxStackSize)

        else if (item.amount + quantity in 1..item.maxStackSize)
            setInvItemQuantity(item, item.amount + quantity)
    }

    private fun saveRewardQuantity(reward: ItemStack, crate: Crate): Boolean {
        return try {
            crate.rewards.find { it.item.isSimilar(reward) }!!.setQuantity(reward.amount)
            true

        } catch (e: Exception) {
            false
        }
    }
}