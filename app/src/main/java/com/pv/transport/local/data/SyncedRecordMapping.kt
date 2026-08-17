package com.pv.transport.local.data

data class SyncedRecordMapping(
    val uuid: String,
    val serverRecordId: String,
    val clientTimestamp: Long
)
