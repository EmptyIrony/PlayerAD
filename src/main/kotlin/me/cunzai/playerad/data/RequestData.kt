package me.cunzai.playerad.data

import java.util.UUID

data class RequestData(
    val requester: String,
    val uuid: UUID,
    val contents: String,
    val durationName: String,
)
