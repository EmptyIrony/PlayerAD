package me.cunzai.playerad.util

import java.text.SimpleDateFormat

fun millisToRoundedTime(millisInput: Long): String {
    var millis = millisInput
    millis += 1L
    val seconds = millis / 1000L
    val minutes = seconds / 60L
    val hours = minutes / 60L
    val days = hours / 24L
    return if (days > 0) {
        days.toString() + " 天 " + (hours - 24 * days) + " 小时"
    } else if (hours > 0) {
        hours.toString() + " 小时 " + (minutes - 60 * hours) + " 分钟"
    } else if (minutes > 0) {
        minutes.toString() + " 分钟 " + (seconds - 60 * minutes) + " 秒"
    } else {
        "$seconds 秒"
    }
}