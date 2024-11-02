package me.cunzai.playerad.handler

import me.cunzai.playerad.config.ConfigLoader
import me.cunzai.playerad.data.AdData
import org.bukkit.Bukkit
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.console
import taboolib.module.lang.asLangText

object AdHandler {

    var activeAds: List<AdData> = ArrayList()

    @Schedule(period = 20L, async = true)
    fun schedule() {
        val now = System.currentTimeMillis()
        activeAds = activeAds.filter {
            it.endTime > now
        }
        val ads = activeAds

        val nowSeconds = now / 1000L

        for (ad in ads) {
            val adSet = ConfigLoader.sets[ad.durationName] ?: continue
            if ((nowSeconds - (ad.startTime / 1000L)) % (adSet.split / 1000L) == 0L) {
                Bukkit.broadcastMessage(console().asLangText("ad_contents", ad.contents))
            }
        }
    }

}