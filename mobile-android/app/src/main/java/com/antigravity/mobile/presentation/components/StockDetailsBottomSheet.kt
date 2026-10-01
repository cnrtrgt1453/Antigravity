package com.antigravity.mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.domain.model.OHLCData
import com.antigravity.mobile.domain.model.Stock
import com.antigravity.mobile.domain.util.BistCompanyNames
import com.antigravity.mobile.ui.theme.ErrorRed
import com.antigravity.mobile.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsBottomSheet(
    symbol: String?,
    name: String?,
    stock: Stock? = null,
    ohlcData: OHLCData = OHLCData(),
    isLoading: Boolean = false,
    onDismiss: () -> Unit
) {
    if (symbol == null) return
    val cleanSymbol = remember(symbol) { symbol.split(".")[0] }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF30363D)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cleanSymbol,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        stock?.dailyChangePercent?.let { change ->
                            Spacer(modifier = Modifier.width(8.dp))
                            val isPos = change > 0
                            val isNeg = change < 0
                            val badgeBg = when {
                                isPos -> SuccessGreen.copy(alpha = 0.2f)
                                isNeg -> ErrorRed.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.1f)
                            }
                            val badgeColor = when {
                                isPos -> SuccessGreen
                                isNeg -> ErrorRed
                                else -> Color.White.copy(alpha = 0.8f)
                            }
                            val prefix = if (isPos) "▲ +" else if (isNeg) "▼ " else ""
                            Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "$prefix${String.format("%.2f", change)}%",
                                    color = badgeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    val companyName = remember(symbol, name) {
                        BistCompanyNames.getCompanyName(symbol, name)
                    }
                    Text(
                        text = companyName,
                        color = Color(0xFF8B949E),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0xFF30363D), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                }
            }

            // Stock KPI Summary
            if (stock != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PriceItem("Son Fiyat", String.format("%.2f ₺", stock.currentPrice ?: 0.0))
                    PriceItem("SMA50", String.format("%.2f", stock.sma50 ?: 0.0))
                    PriceItem("SMA200", String.format("%.2f", stock.sma200 ?: 0.0))
                    if (stock.crossPrice != null && stock.crossPrice > 0) {
                        PriceItem("Kesişim Fiyatı", String.format("%.2f ₺", stock.crossPrice))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (signalLabel, badgeBg, badgeTextColor) = when (stock.signal) {
                            "GOLDEN_CROSS" -> Triple("GOLDEN CROSS", SuccessGreen.copy(alpha = 0.2f), SuccessGreen)
                            "DEAD_CROSS" -> Triple("DEAD CROSS", ErrorRed.copy(alpha = 0.2f), ErrorRed)
                            else -> Triple("SİNYAL YOK", Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.6f))
                        }
                        Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = signalLabel,
                                color = badgeTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!stock.crossDate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatCrossDate(stock.crossDate),
                                color = Color(0xFF8B949E),
                                fontSize = 12.sp
                            )
                        }
                    }

                    val diff = stock.crossDiff ?: if (stock.currentPrice != null && stock.crossPrice != null && stock.crossPrice > 0) {
                        stock.currentPrice - stock.crossPrice
                    } else null

                    val diffPercent = stock.crossDiffPercent ?: if (stock.currentPrice != null && stock.crossPrice != null && stock.crossPrice > 0) {
                        ((stock.currentPrice - stock.crossPrice) / stock.crossPrice) * 100.0
                    } else null

                    val diffColor = when {
                        diff == null -> Color(0xFF8B949E)
                        diff > 0 -> SuccessGreen
                        diff < 0 -> ErrorRed
                        else -> Color.White
                    }

                    val diffText = if (diff != null) {
                        "${String.format("%+.2f ₺", diff)}${diffPercent?.let { " (%+.2f%%)".format(it) } ?: ""}"
                    } else "-"

                    Text(
                        text = diffText,
                        color = diffColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive TradingView Chart
            StockChart(
                symbol = symbol,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Text(
                text = "⚡ TradingView Canlı Mum Grafiği (Yakınlaştırma ve Zaman Dilimleri Desteklenir)",
                color = Color(0xFF8B949E),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
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
