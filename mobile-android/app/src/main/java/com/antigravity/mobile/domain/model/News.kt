package com.antigravity.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class News(
    val id: Int,
    val title: String,
    val content: String,
    val publishedAt: String,
    val stockSymbol: String,
    val sourceUrl: String
)
