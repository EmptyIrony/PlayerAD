package me.cunzai.playerad.ui

import com.google.common.cache.CacheBuilder
import me.cunzai.playerad.config.ConfigLoader
import me.cunzai.playerad.data.RequestData
import me.cunzai.playerad.database.MySQLHandler
import me.cunzai.playerad.util.millisToRoundedTime
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submitAsync
import taboolib.library.xseries.getItemStack
import taboolib.module.chat.Components
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringColored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.compat.getBalance
import taboolib.platform.compat.withdrawBalance
import taboolib.platform.util.*
import java.util.UUID
import java.util.concurrent.TimeUnit

object AdUI {
    private val sendCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<UUID, RequestData>()

    @Config("ui.yml")
    lateinit var config: Configuration

    @Awake(LifeCycle.ENABLE)
    fun i() {
        command("adcommit", permissionDefault = PermissionDefault.TRUE) {
            dynamic("uuid") {
                execute<Player> { sender, _, argument ->
                    val uuid = kotlin.runCatching { UUID.fromString(argument) }.getOrNull() ?: return@execute
                    val requestData = sendCache.getIfPresent(uuid)
                    if (requestData == null) {
                        sender.sendLang("expired")
                        return@execute
                    }

                    if (requestData.requester != sender.name) {
                        sender.sendLang("expired")
                        return@execute
                    }

                    val adSet = ConfigLoader.sets[requestData.durationName] ?: return@execute

                    if (sender.getBalance() < adSet.costMoney) {
                        sender.sendLang("no_money")
                        return@execute
                    }

                    if (PlayerPoints.getInstance().api.look(sender.uniqueId) < adSet.costPoints) {
                        sender.sendLang("no_points")
                        return@execute
                    }

                    sender.withdrawBalance(adSet.costMoney.toDouble())
                    PlayerPoints.getInstance().api.take(sender.uniqueId, adSet.costPoints)

                    submitAsync {
                        MySQLHandler.insertRequest(requestData)
                        sender.sendLang("commit_success")
                    }
                }
            }
        }
    }

    fun open(player: Player) {
        player.openMenu<Chest>(config.getStringColored("title")!!) {
            map(*config.getStringList("format").toTypedArray())
            set(
                '#',
                config.getItemStack("placeholder")!!
            ) {
                isCancelled = true
            }
            val slot = getSlots('!')
            val setList = ConfigLoader.sets.values.sortedBy {
                it.duration
            }

            val icon = config.getItemStack("ad")!!.clone()

            setList.forEachIndexed { index, adSet ->
                val slotIndex = slot.getOrNull(index) ?: return@forEachIndexed

                val replace = mapOf(
                    "%name%" to adSet.name,
                    "%duration%" to millisToRoundedTime(adSet.duration),
                    "%split%" to millisToRoundedTime(adSet.split),
                    "%length%" to adSet.lengthLimit.toString(),
                    "%money%" to adSet.costMoney.toString(),
                    "%points%" to adSet.costPoints.toString()
                )

                set(slotIndex, icon.clone().replaceName(replace).replaceLore(replace)) {
                    isCancelled = true
                    player.closeInventory()

                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

                    player.sendLang("input_chat")

                    player.nextChatInTick(600L, { msg ->
                        val uuid = UUID.randomUUID()
                        val requestData = RequestData(
                            player.name,
                            uuid,
                            msg,
                            adSet.name
                        )

                        sendCache.put(uuid, requestData)

                        val text = player.asLangText("input_confirm", msg)
                        Components.text(text)
                            .clickRunCommand("/adcommit $uuid")
                            .sendTo(adaptPlayer(player))
                    }, {
                        player.sendLang("input_timeout")
                    })
                }
            }
        }
    }

}