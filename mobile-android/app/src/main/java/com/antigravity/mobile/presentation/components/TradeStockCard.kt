package com.antigravity.mobile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
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
fun TradeStockCard(
    stock: Stock,
    onBuyClick: (Stock) -> Unit
) {
    val cleanSymbol = stock.symbol.substringBefore(".")
    val companyName = remember(stock.symbol, stock.name) {
        BistCompanyNames.getCompanyName(stock.symbol, stock.name)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sol: Sembol ve Şirket Adı
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cleanSymbol,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    stock.dailyChangePercent?.let { change ->
                        Spacer(modifier = Modifier.width(8.dp))
                        val isPos = change > 0
                        val isNeg = change < 0
                        val badgeBg = when {
                            isPos -> SuccessGreen.copy(alpha = 0.15f)
                            isNeg -> ErrorRed.copy(alpha = 0.15f)
                            else -> Color(0xFF30363D)
                        }
                        val badgeColor = when {
                            isPos -> SuccessGreen
                            isNeg -> ErrorRed
                            else -> Color(0xFF8B949E)
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
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = companyName,
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Orta-Sağ: Fiyat
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = String.format("%.2f TL", stock.currentPrice ?: 0.0),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Son Fiyat",
                    color = Color(0xFF8B949E),
                    fontSize = 10.sp
                )
            }

            // Sağ: "AL" Butonu
            Button(
                onClick = { onBuyClick(stock) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
