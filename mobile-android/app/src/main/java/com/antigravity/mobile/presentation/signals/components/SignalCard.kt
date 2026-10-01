package com.antigravity.mobile.presentation.signals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.domain.model.SignalDirection
import com.antigravity.mobile.domain.model.TradingSignal
import com.antigravity.mobile.domain.util.BistCompanyNames
import java.util.Locale

@Composable
fun SignalCard(
    signal: TradingSignal,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Başlık: Sembol, Yön Rozeti ve Risk/Ödül
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = signal.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val companyName = BistCompanyNames.getCompanyName(signal.symbol)
                        if (companyName.isNotBlank() && companyName != signal.symbol) {
                            Text(
                                text = companyName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SignalBadge(direction = signal.direction)
                }
                Text(
                    text = "R:R 1:${signal.riskRewardRatio}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fiyat Grid'i: Giriş, Stop-Loss, Hedef
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PriceMetric(
                    label = "Giriş Seviyesi",
                    value = "${signal.entryPrice} ₺",
                    subValue = "Tetik Fiyatı"
                )
                PriceMetric(
                    label = "Zarar Kes (SL)",
                    value = "${signal.stopLoss} ₺",
                    subValue = "-%${String.format(Locale.US, "%.1f", signal.riskPercent)}",
                    valueColor = Color(0xFFE53935)
                )
                PriceMetric(
                    label = "Hedef (TP)",
                    value = "${signal.takeProfit} ₺",
                    subValue = "+%${String.format(Locale.US, "%.1f", signal.potentialProfitPercent)}",
                    valueColor = Color(0xFF43A047)
                )
            }
        }
    }
}

@Composable
private fun SignalBadge(direction: SignalDirection) {
    val isBuy = direction == SignalDirection.BUY
    val bgColor = if (isBuy) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (isBuy) Color(0xFF2E7D32) else Color(0xFFC62828)
    val text = if (isBuy) "AL" else "SAT"

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PriceMetric(
    label: String,
    value: String,
    subValue: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = subValue, style = MaterialTheme.typography.labelSmall, color = valueColor)
    }
}
