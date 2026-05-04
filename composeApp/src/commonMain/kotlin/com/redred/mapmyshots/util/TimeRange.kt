package com.redred.mapmyshots.util

import com.redred.mapmyshots.model.TimeWindow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun getTimeRangeDuration(timeWindow: TimeWindow): Duration = timeWindow.hours.hours
