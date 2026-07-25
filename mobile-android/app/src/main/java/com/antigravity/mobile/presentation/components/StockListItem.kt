package com.antigravity.mobile.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.domain.model.Stock
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
                    Text(
                        text = stock.symbol.split(".")[0],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stock.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                PriceItem("Fiyat", String.format("%.2f", stock.currentPrice ?: 0.0))
                PriceItem("SMA50", String.format("%.2f", stock.sma50 ?: 0.0))
                PriceItem("SMA200", String.format("%.2f", stock.sma200 ?: 0.0))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kesişimden Beri Fark:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                val diff = if (stock.currentPrice != null && stock.crossPrice != null) {
                    stock.currentPrice - stock.crossPrice
                } else null
                
                val diffColor = when {
                    diff == null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    diff > 0 -> SuccessGreen
                    diff < 0 -> ErrorRed
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = if (diff != null) String.format("%+.2f", diff) else "-",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = diffColor
                )
            }
        }
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
