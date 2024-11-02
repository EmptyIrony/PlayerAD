package me.cunzai.playerad.ui

import me.cunzai.playerad.config.ConfigLoader
import me.cunzai.playerad.data.AdData
import me.cunzai.playerad.data.RequestData
import me.cunzai.playerad.database.MySQLHandler
import me.cunzai.playerad.database.RedisHandler
import me.cunzai.playerad.ui.CheckUI.pass
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.function.submitAsync
import taboolib.expansion.submitChain
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.PageableChest
import taboolib.platform.compat.depositBalance
import taboolib.platform.util.buildItem

object CheckUI {

    fun open(player: Player) {
        submitChain {
            val request = async {
                MySQLHandler.getAllRequests()
            }
            sync {
                player.openMenu<PageableChest<RequestData>>("广告审核") {
                    rows(6)
                    setPreviousPage(45) { _, _ ->
                        buildItem(Material.ARROW) {
                            name = "上一页"
                        }
                    }
                    setPreviousPage(53) { _, _ ->
                        buildItem(Material.ARROW) {
                            name = "下一页"
                        }
                    }

                    elements { request }
                    slots((0 .. 44).toList())
                    onGenerate { _, element, _, _ ->
                        buildItem(Material.BOOK) {
                            name = element.requester
                            lore += "&r${element.contents}"
                            lore += ""
                            lore += "&f套餐: &7${element.durationName}"
                            lore += ""
                            lore += "&a左键通过 &c右键拒绝"
                            colored()
                        }
                    }
                    onClick { event, element ->
                        event.isCancelled = true
                        val clickEvent = event.clickEventOrNull() ?: return@onClick
                        if (clickEvent.isLeftClick) {
                            submitChain {
                                element.pass()
                                sync {
                                    open(player)
                                }
                            }
                            return@onClick
                        }
                        if (clickEvent.isRightClick) {
                            submitChain {
                                element.reject()
                                sync {
                                    open(player)
                                }
                            }
                            return@onClick
                        }
                    }

                }
            }
        }
    }

    private fun RequestData.pass() {
        val set = ConfigLoader.sets[durationName]!!
        MySQLHandler.deleteRequest(this)
        MySQLHandler.insertAD(
            AdData(
                uuid, requester,
                System.currentTimeMillis(),
                System.currentTimeMillis() + set.duration,
                durationName, contents
            )
        )

        RedisHandler.redis.publish(
            "player_ad_request_status",
            RedisHandler.gson.toJson(
                RedisHandler.AdRequestStatus(this, true)
            )
        )
    }

    private fun RequestData.reject() {
        MySQLHandler.deleteRequest(this)

        RedisHandler.redis.publish(
            "player_ad_request_status",
            RedisHandler.gson.toJson(
                RedisHandler.AdRequestStatus(this, false)
            )
        )

        val set = ConfigLoader.sets[durationName]!!
        val refundPoints = set.costPoints / 2
        val refundCoins = set.costMoney / 2.0

        Bukkit.getOfflinePlayer(requester).apply {
            depositBalance(refundCoins)
            PlayerPoints.getInstance().api.give(uniqueId, refundPoints)
        }

    }

}