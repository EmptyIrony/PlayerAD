package me.cunzai.playerad.database

import com.google.gson.Gson
import me.cunzai.playerad.data.RequestData
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.expansion.AlkaidRedis
import taboolib.expansion.fromConfig
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.util.onlinePlayers
import java.util.UUID

object RedisHandler {

    @Config("database.yml")
    lateinit var config: Configuration

    val gson = Gson()

    private val redis by lazy {
        AlkaidRedis.create()
            .fromConfig(config.getConfigurationSection("redis")!!)
            .connect()
            .connection()
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        redis.subscribe(
            "player_ad_request", "player_ad_request_status",
        ) {
            when(channel) {
                "player_ad_request" -> {
                    val data = gson.fromJson(message, RequestData::class.java)
                    onlinePlayers.filter {
                        it.hasPermission("ad.admin")
                    }.forEach {
                        it.sendMessage("&a你收到了一条来自${data.requester}的广告审核! 输入&e/playerad check&a 进行审核".colored())
                    }
                }
                "player_ad_request_status" -> {

                }
            }
        }
    }

    class AdRequestStatus(
        val requester: String,
        val requestUuid: UUID,
        val passed: Boolean
    )

}