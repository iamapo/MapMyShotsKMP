package com.redred.mapmyshots.util

import com.redred.mapmyshots.model.Asset
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun groupByMonth(photos: List<Asset>, tz: TimeZone = TimeZone.currentSystemDefault())
        : Map<String, List<Asset>> {
    val out = LinkedHashMap<String, MutableList<Asset>>()
    for (a in photos) {
        val ldt: LocalDateTime = a.takenAt.toLocalDateTime(tz)
        val key = "${ldt.month.name.lowercase().replaceFirstChar{ it.titlecase() }} ${ldt.year}"
        out.getOrPut(key) { mutableListOf() }.add(a)
    }
    return out
}