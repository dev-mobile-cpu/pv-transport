package com.pv.transport.extension

import java.text.NumberFormat
import java.util.Locale

fun Long.withComma(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun Int.withComma(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun String.withComma(): String {
    return this.toLongOrNull()
        ?.let { NumberFormat.getNumberInstance(Locale.US).format(it) }
        ?: this
}