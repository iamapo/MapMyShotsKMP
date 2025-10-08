package com.redred.mapmyshots.model

import kotlin.time.ExperimentalTime

data class Asset @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val displayName: String?,
    val takenAt: kotlin.time.Instant,
    val uri: String
)