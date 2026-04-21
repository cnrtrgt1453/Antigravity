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
                PriceItem("Fiyat", stock.currentPrice?.toString() ?: "-")
                PriceItem("Sinyal", stock.signal ?: "YOK")
                PriceItem("Kesişim", stock.crossPrice?.toString() ?: "-")
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
