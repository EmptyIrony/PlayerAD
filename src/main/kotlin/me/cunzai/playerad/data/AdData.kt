package me.cunzai.playerad.data

import java.util.UUID

data class AdData(
    val uuid: UUID,
    val sender: String,
    val startTime: Long,
    var endTime: Long,
    val durationName: String,
    val contents: String,
    var lastTickTime: Long = 0,
)