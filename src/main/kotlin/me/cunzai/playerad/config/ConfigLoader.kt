package me.cunzai.playerad.config

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import kotlin.time.Duration

object ConfigLoader {

    @Config("set.yml")
    lateinit var config: Configuration

    val sets = HashMap<String, AdSet>()

    @Awake(LifeCycle.ENABLE)
    fun i() {
        config.getKeys(false).forEach { setName ->
            val section = config.getConfigurationSection(setName)!!
            val duration = Duration.parse(section.getString("time")!!).inWholeMilliseconds
            val split = Duration.parse(section.getString("split")!!).inWholeMilliseconds

            val lengthLimit = section.getInt("length_limit")

            val costMoney = section.getInt("cost.money")
            val costPoints = section.getInt("cost.points")

            AdSet(setName, duration, split, lengthLimit, costMoney, costPoints).apply {
                sets[setName] = this
            }
        }
    }


    data class AdSet(
        val name: String,
        val duration: Long,
        val split: Long,
        val lengthLimit: Int,
        val costMoney: Int,
        val costPoints: Int
    )

}