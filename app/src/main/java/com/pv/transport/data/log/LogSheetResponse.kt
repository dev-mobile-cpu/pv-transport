package com.pv.transport.data.log

data class LogSheetResponse(
    val message: String,
    val errors: Errors
)
data class Errors(
    val logSheet: List<String>
)
