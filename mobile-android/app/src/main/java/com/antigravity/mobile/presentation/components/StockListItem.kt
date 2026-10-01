package com.antigravity.mobile.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.util.BistCompanyNames
import com.antigravity.mobile.ui.theme.SuccessGreen
import com.antigravity.mobile.ui.theme.ErrorRed

@Composable
fun StockListItem(
    stock: Stock,
    onItemClick: (Stock) -> Unit,
    onWatchlistToggle: (Stock) -> Unit
) {
    val borderColor = when (stock.signal) {
        "GOLDEN_CROSS" -> SuccessGreen
        "DEAD_CROSS" -> ErrorRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val borderWidth = if (stock.signal == "NO_SIGNAL") 1.dp else 2.dp

    ElevatedCard(
        onClick = { onItemClick(stock) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol.split(".")[0],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        stock.dailyChangePercent?.let { change ->
                            Spacer(modifier = Modifier.width(8.dp))
                            val isPos = change > 0
                            val isNeg = change < 0
                            val badgeBg = when {
                                isPos -> SuccessGreen.copy(alpha = 0.15f)
                                isNeg -> ErrorRed.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            }
                            val badgeColor = when {
                                isPos -> SuccessGreen
                                isNeg -> ErrorRed
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            }
                            val prefix = if (isPos) "▲ +" else if (isNeg) "▼ " else ""
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$prefix${String.format("%.2f", change)}%",
                                    color = badgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    val companyName = remember(stock.symbol, stock.name) {
                        BistCompanyNames.getCompanyName(stock.symbol, stock.name)
                    }
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { onWatchlistToggle(stock) }) {
                    Icon(
                        imageVector = if (stock.isWatched) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Watchlist",
                        tint = if (stock.isWatched) Color(0xFFF6C90E) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PriceItem("Fiyat", String.format("%.2f ₺", stock.currentPrice ?: 0.0))
                PriceItem("SMA50", String.format("%.2f", stock.sma50 ?: 0.0))
                PriceItem("SMA200", String.format("%.2f", stock.sma200 ?: 0.0))
                if (stock.crossPrice != null && stock.crossPrice > 0) {
                    PriceItem("Kesişim Fiyatı", String.format("%.2f ₺", stock.crossPrice))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Kesişim Sinyali Rozeti ve Tarihi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (signalLabel, badgeBg, badgeTextColor) = when (stock.signal) {
                        "GOLDEN_CROSS" -> Triple("GOLDEN CROSS", SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                        "DEAD_CROSS" -> Triple("DEAD CROSS", ErrorRed.copy(alpha = 0.15f), ErrorRed)
                        else -> Triple("SİNYAL YOK", MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = signalLabel,
                            color = badgeTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (!stock.crossDate.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatCrossDate(stock.crossDate),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                if (stock.crossPrice != null && stock.crossPrice > 0 && stock.crossDate.isNullOrBlank()) {
                    Text(
                        text = "Kesişim: ${String.format("%.2f ₺", stock.crossPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Kesişimden Sonraki Fiyat Farkı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val diffLabel = when (stock.signal) {
                    "GOLDEN_CROSS" -> "Golden Cross Sonrası Fark:"
                    "DEAD_CROSS" -> "Dead Cross Sonrası Fark:"
                    else -> "Kesişimden Beri Fark:"
                }

                Text(
                    text = diffLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                val diff = stock.crossDiff ?: if (stock.currentPrice != null && stock.crossPrice != null && stock.crossPrice > 0) {
                    stock.currentPrice - stock.crossPrice
                } else null

                val diffPercent = stock.crossDiffPercent ?: if (stock.currentPrice != null && stock.crossPrice != null && stock.crossPrice > 0) {
                    ((stock.currentPrice - stock.crossPrice) / stock.crossPrice) * 100.0
                } else null

                val diffColor = when {
                    diff == null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    diff > 0 -> SuccessGreen
                    diff < 0 -> ErrorRed
                    else -> MaterialTheme.colorScheme.onSurface
                }

                val pctText = diffPercent?.let { " (${String.format("%+.2f", it)}%)" } ?: ""

                Text(
                    text = if (diff != null) "${String.format("%+.2f ₺", diff)}$pctText" else "-",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = diffColor
                )
            }
        }
    }
}

private fun formatCrossDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val parts = dateStr.trim().split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1]
            val day = parts[2].take(2)
            "$day.$month.$year"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
fun PriceItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
