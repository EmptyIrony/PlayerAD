package me.cunzai.playerad.database

import me.cunzai.playerad.data.AdData
import me.cunzai.playerad.data.RequestData
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Index
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.util.UUID

object MySQLHandler {

    @Config("database.yml")
    lateinit var config: Configuration

    private val host by lazy {
        config.getHost("mysql")
    }

    private val datasource by lazy {
        host.createDataSource()
    }

    private val requestTable by lazy {
        Table("player_ad_request", host) {
            add {
                id()
            }

            // 请求uuid
            add("uuid") {
                type(ColumnTypeSQL.VARCHAR, 36)
            }

            add ("requester"){
                type(ColumnTypeSQL.VARCHAR, 36)
            }

            add("contents") {
                type(ColumnTypeSQL.TEXT)
            }

            add("duration_name") {
                type(ColumnTypeSQL.VARCHAR, 36)
            }
        }
    }

    private val adTable by lazy {
        Table("player_ad", host) {
            add {
                id()
            }

            add("uuid") {
                type(ColumnTypeSQL.VARCHAR, 36)
            }

            add("sender") {
                type(ColumnTypeSQL.VARCHAR, 36)
            }

            add("contents") {
                type(ColumnTypeSQL.TEXT)
            }

            add("start_time") {
                type(ColumnTypeSQL.BIGINT)
            }

            add("end_time") {
                type(ColumnTypeSQL.BIGINT)
            }

            add("duration_name") {
                type(ColumnTypeSQL.VARCHAR, 36)
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        adTable.workspace(datasource) {
            createTable(checkExists = true)
            createIndex(Index("idx_uuid", listOf("uuid"), checkExists = true))
            createIndex(Index("idx_sender", listOf("sender"), checkExists = true))
        }.run()

        requestTable.workspace(datasource) {
            createTable(checkExists = true)
            createIndex(Index("idx_requester", listOf("requester"), checkExists = true))
            createIndex(Index("idx_uuid", listOf("uuid"), checkExists = true))
        }.run()
    }

    fun getAllRequests(): List<RequestData> {
        return requestTable.select(datasource) {}.map {
            RequestData(
                getString("requester"),
                UUID.fromString(getString("uuid")),
                getString("contents"),
                getString("duration_name")
            )
        }
    }

    fun getAllAds(): List<AdData> {
        return adTable.select(datasource) {
            where {
                "end_time" lt System.currentTimeMillis()
            }
        }.map {
            AdData(
                UUID.fromString(getString("uuid")),
                getString("sender"),
                getLong("start_time"),
                getLong("end_time"),
                getString("duration_name"),
                getString("contents")
            )
        }
    }

    fun insertRequest(requestData: RequestData) {
        adTable.insert(datasource, "requester", "contents", "duration_name") {
            value(requestData.requester, requestData.contents, requestData.durationName)
        }
    }


}