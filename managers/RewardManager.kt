package notzcrates.managers

import notzcrates.entities.Crate
import notzcrates.entities.Reward
import kotlin.random.Random

object RewardManager {
    fun getRewardByChance(crate: Crate): Reward {
        val chanceIndex = Random.nextDouble(crate.rewards.sumOf { it.getChance() })
        val tempRews = hashMapOf<Reward, Double>()
        var tempchance = 0.0

        crate.rewards.forEach { tempchance += it.getChance(); tempRews[it] = tempchance }

        return tempRews.keys.find { tempRews[it]!! >= chanceIndex }!!
    }
}