package com.antigravity.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OHLCPoint(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

@Serializable
data class LinePoint(
    val time: Long,
    val value: Double
)

@Serializable
data class MarkerPoint(
    val time: Long,
    val position: String,
    val color: String,
    val shape: String,
    val text: String
)

@Serializable
data class OHLCData(
    val ohlc: List<OHLCPoint> = emptyList(),
    val sma50: List<LinePoint> = emptyList(),
    val sma200: List<LinePoint> = emptyList(),
    val markers: List<MarkerPoint> = emptyList()
)

@Serializable
data class CooldownStatus(
    val can_scan: Boolean,
    val remaining_seconds: Int
)

@Serializable
data class ScanResponse(
    val success: Boolean,
    val message: String
)
