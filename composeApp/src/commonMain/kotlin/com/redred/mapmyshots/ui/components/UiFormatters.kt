package com.redred.mapmyshots.ui.components

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun formatTakenAt(value: Instant): String {
    val ldt = value.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = ldt.day.twoDigits()
    val month = (ldt.month.ordinal + 1).twoDigits()
    val year = ldt.year
    val hour = ldt.hour.twoDigits()
    val minute = ldt.minute.twoDigits()
    return "$day.$month.$year $hour:$minute"
}

@OptIn(ExperimentalTime::class)
internal fun formatDate(value: Instant): String {
    val ldt = value.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${ldt.day.twoDigits()}.${(ldt.month.ordinal + 1).twoDigits()}.${ldt.year}"
}

@OptIn(ExperimentalTime::class)
internal fun formatTime(value: Instant): String {
    val ldt = value.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${ldt.hour.twoDigits()}:${ldt.minute.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')
