package me.cunzai.playerad.database

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.expansion.AlkaidRedis
import taboolib.expansion.fromConfig
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object RedisHandler {

    @Config("database.yml")
    lateinit var config: Configuration

    private val redis by lazy {
        AlkaidRedis.create()
            .fromConfig(config.getConfigurationSection("redis")!!)
            .connect()
            .connection()
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        redis.subscribe(
            "player_ad_broadcast"
        ) {

        }
    }

}