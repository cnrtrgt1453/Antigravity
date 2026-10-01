package com.antigravity.mobile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.ui.theme.SuccessGreen
import com.antigravity.mobile.ui.theme.ErrorRed
import com.antigravity.mobile.presentation.game.formatTurkishLira

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeBottomSheet(
    symbol: String,
    price: Double,
    tradeType: String, // BUY, SELL
    availableBalance: Double = 0.0,
    maxQuantity: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }
    val isBuy = tradeType == "BUY"
    val qty = quantityText.toLongOrNull() ?: 0L
    val totalAmount = qty * price

    val isInsufficientBalance = isBuy && totalAmount > availableBalance
    val isExceedingQuantity = !isBuy && maxQuantity != null && qty > maxQuantity
    val isValid = qty > 0 && !isInsufficientBalance && !isExceedingQuantity

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Başlık ve Hisse Bilgisi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBuy) "Hisse Alımı (Simülasyon)" else "Hisse Satımı (Simülasyon)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$symbol - ${String.format("%.2f TL", price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B949E),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isBuy) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isBuy) SuccessGreen else ErrorRed)
                ) {
                    Text(
                        text = if (isBuy) "AL" else "SAT",
                        color = if (isBuy) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bakiye veya Mevcut Lot Bilgisi
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF30363D))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBuy) "Kullanılabilir Bakiye:" else "Portföydeki Adet:",
                        color = Color(0xFF8B949E),
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isBuy) formatTurkishLira(availableBalance) else "${maxQuantity ?: 0} Lot",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lot Miktarı Girişi
            Text(
                text = "İşlem Adedi (Lot)",
                color = Color(0xFF8B949E),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Azalt Butonu
                IconButton(
                    onClick = {
                        val current = quantityText.toLongOrNull() ?: 1L
                        if (current > 1) quantityText = (current - 1).toString()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF21262D), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Azalt", tint = Color.White)
                }

                // Input Box
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            quantityText = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0D1117),
                        unfocusedContainerColor = Color(0xFF0D1117),
                        focusedBorderColor = Color(0xFF58A6FF),
                        unfocusedBorderColor = Color(0xFF30363D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Artır Butonu
                IconButton(
                    onClick = {
                        val current = quantityText.toLongOrNull() ?: 0L
                        if (!isBuy && maxQuantity != null && current >= maxQuantity) {
                            // Satışta mevcuttan fazla olamaz
                        } else {
                            quantityText = (current + 1).toString()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF21262D), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Artır", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hızlı Lot Seçim Çipleri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5L, 10L, 50L, 100L).forEach { amount ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val current = quantityText.toLongOrNull() ?: 0L
                                quantityText = (current + amount).toString()
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF21262D),
                        border = BorderStroke(1.dp, Color(0xFF30363D))
                    ) {
                        Text(
                            text = "+$amount",
                            color = Color(0xFFC9D1D9),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                if (!isBuy && maxQuantity != null && maxQuantity > 0) {
                    Surface(
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { quantityText = maxQuantity.toString() },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF85149).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFF85149))
                    ) {
                        Text(
                            text = "Tümü",
                            color = Color(0xFFF85149),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toplam Tutar Özeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Toplam Tutar", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF8B949E))
                Text(
                    text = formatTurkishLira(totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isBuy) Color(0xFFF6C90E) else Color(0xFF3FB950)
                )
            }

            if (isInsufficientBalance) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Yetersiz bakiye! Mevcut bakiyeniz: ${formatTurkishLira(availableBalance)}",
                    color = ErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExceedingQuantity) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Portföyünüzde yalnızca $maxQuantity adet hisse bulunmaktadır.",
                    color = ErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Onay Butonu
            Button(
                onClick = { if (isValid) onConfirm(qty) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuy) SuccessGreen else ErrorRed,
                    disabledContainerColor = Color(0xFF21262D)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isBuy) "ALIMI ONAYLA ($qty LOT)" else "SATIŞI ONAYLA ($qty LOT)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (isValid) Color.White else Color(0xFF8B949E)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
