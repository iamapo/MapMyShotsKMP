package com.redred.mapmyshots.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun getTimeRangeDuration(label: String?): Duration = when (label) {
    "4 hours" -> 4.hours
    "12 hours" -> 12.hours
    else -> 1.hours
}