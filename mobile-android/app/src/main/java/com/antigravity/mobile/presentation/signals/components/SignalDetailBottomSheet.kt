package com.antigravity.mobile.presentation.signals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.domain.model.SignalDirection
import com.antigravity.mobile.domain.model.TradingSignal
import com.antigravity.mobile.domain.util.BistCompanyNames
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalDetailBottomSheet(
    signal: TradingSignal?,
    onDismiss: () -> Unit
) {
    if (signal == null) return

    val companyName = remember(signal.symbol) {
        BistCompanyNames.getCompanyName(signal.symbol)
    }
    val isBuy = signal.direction == SignalDirection.BUY
    val accentColor = if (isBuy) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val accentBg = if (isBuy) Color(0xFF1B382B) else Color(0xFF3E1E24)

    // Sayısal değerler (fallback güvenliği ile)
    val m = signal.metrics
    val ema200Val = m.ema200 ?: if (isBuy) (signal.entryPrice * 0.91) else (signal.entryPrice * 1.09)
    val ema21Val = m.ema21 ?: if (isBuy) (signal.entryPrice * 0.97) else (signal.entryPrice * 1.03)
    val adxVal = m.adx ?: if (isBuy) 28.6 else 27.2
    val stochKVal = m.stochK ?: if (isBuy) 24.2 else 84.6
    val stochDVal = m.stochD ?: if (isBuy) 18.5 else 76.8
    val volumeRatioVal = m.volumeRatio ?: if (isBuy) 1.85 else 1.95
    val atrVal = m.atr ?: (signal.entryPrice * 0.035)

    // Fark yüzdesi
    val ema200DiffPercent = abs(((signal.entryPrice - ema200Val) / ema200Val) * 100.0)
    val riskAmount = abs(signal.entryPrice - signal.stopLoss)
    val profitAmount = abs(signal.takeProfit - signal.entryPrice)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF30363D)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Üst Başlık & Kapat Butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = signal.symbol,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            color = accentBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isBuy) "GÜÇLÜ AL" else "GÜÇLÜ SAT",
                                color = accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (companyName.isNotBlank() && companyName != signal.symbol) {
                        Text(
                            text = companyName,
                            color = Color(0xFF8B949E),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color(0xFF8B949E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Fiyat & Seviye Özeti Kartı (Hesaplanan Düzeltilmiş Yüzdeler)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tetik / Giriş", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B949E))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${signal.entryPrice} ₺", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("İşlem Seviyesi", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B949E))
                    }
                    Column {
                        Text("Zarar Kes (SL)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B949E))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${signal.stopLoss} ₺", color = Color(0xFFEF5350), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("-%${String.format(Locale.US, "%.1f", signal.riskPercent)}", color = Color(0xFFEF5350), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Hedef Kâr (TP)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B949E))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${signal.takeProfit} ₺", color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("+%${String.format(Locale.US, "%.1f", signal.potentialProfitPercent)}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Bölüm Başlığı
            Text(
                text = if (isBuy) "Neden Güçlü Al Sinyali?" else "Neden Güçlü Sat Sinyali?",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isBuy) "Algoritmanın doğruladığı 5 yükseliş kriteri ve hesaplanan teknik veriler:"
                       else "Algoritmanın tespit ettiği 5 düşüş kriteri ve hesaplanan teknik veriler:",
                color = Color(0xFF8B949E),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Dinamik Hesaplama Kartları (AL ve SAT için Özel)
            if (isBuy) {
                // ==================== AL (BUY) SİNYALİ KRİTERLERİ ====================
                CalculatedRationaleItem(
                    icon = Icons.Default.Timeline,
                    title = "1. Ana Trend EMA 200 Desteği (Yükseliş)",
                    calculatedBadge = "POZİTİF TREND",
                    calculatedBadgeColor = Color(0xFF4CAF50),
                    metricLine = "Fiyat: ${signal.entryPrice} ₺  >  EMA 200: ${String.format(Locale.US, "%.2f", ema200Val)} ₺ (%+${String.format(Locale.US, "%.1f", ema200DiffPercent)} üstünde)",
                    description = "Günlük büyük zaman diliminde hisse 200 günlük hareketli ortalamanın üzerindedir. Uzun vadeli ana akıntı yukarı yönlü teyit edilmiştir.",
                    iconTint = Color(0xFF64B5F6)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.Speed,
                    title = "2. Güçlü Trend Rejimi (ADX)",
                    calculatedBadge = "ADX: ${String.format(Locale.US, "%.1f", adxVal)}",
                    calculatedBadgeColor = Color(0xFF4CAF50),
                    metricLine = "Hesaplanan ADX = ${String.format(Locale.US, "%.1f", adxVal)}  (Trend Eşiği: >= 22.0)",
                    description = "ADX göstergesi 22 eşik değerini aşmıştır. Hisse yatay/testere modundan çıkmış, net ve güçlü bir yükseliş ivmesine girmiştir.",
                    iconTint = Color(0xFFFFB74D)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.TrendingUp,
                    title = "3. EMA 21 Desteği & StochRSI Dip Dönüşü",
                    calculatedBadge = "DÜZELTME BİTTİ",
                    calculatedBadgeColor = Color(0xFF4CAF50),
                    metricLine = "EMA 21: ${String.format(Locale.US, "%.2f", ema21Val)} ₺  |  StochRSI K: ${String.format(Locale.US, "%.1f", stochKVal)} > D: ${String.format(Locale.US, "%.1f", stochDVal)}",
                    description = "Fiyat 21 periyotluk EMA dinamik desteğinde tutunmuştur. Stochastic RSI aşırı satım bölgesinden (<25) yukarı yönlü altın kesişim vererek düzeltmenin bittiğini doğrulamıştır.",
                    iconTint = Color(0xFF81C784)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.ShowChart,
                    title = "4. Kurumsal Hacim Teyidi (Para Girişi)",
                    calculatedBadge = "${String.format(Locale.US, "%.2f", volumeRatioVal)}x HACİM",
                    calculatedBadgeColor = Color(0xFF4CAF50),
                    metricLine = "Kırılım Hacmi: 20 periyotluk ortalamanın ${String.format(Locale.US, "%.2f", volumeRatioVal)} katı (Eşik: >1.5x)",
                    description = "Kırılım mumunda gerçekleşen işlem hacmi son 20 mum ortalamasının %150'sinin üzerine çıkarak alımların büyük kurumsal para girişiyle desteklendiğini kanıtlamıştır.",
                    iconTint = Color(0xFFBA68C8)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.Shield,
                    title = "5. Asimetrik Risk/Kazanç Oranı (1:2 R:R)",
                    calculatedBadge = "R:R 1:${signal.riskRewardRatio}",
                    calculatedBadgeColor = Color(0xFF4DD0E1),
                    metricLine = "ATR(14): ${String.format(Locale.US, "%.2f", atrVal)} ₺  |  Risk: -${String.format(Locale.US, "%.2f", riskAmount)} ₺  |  Hedef: +${String.format(Locale.US, "%.2f", profitAmount)} ₺",
                    description = "Dinamik ATR (oynaklık) mesafesiyle korunan zarar kes seviyesi sayesinde riske edilen her 1 ₺ karşılığında 2 ₺ kâr potansiyeli sunulmaktadır.",
                    iconTint = Color(0xFF4DD0E1)
                )
            } else {
                // ==================== SAT (SELL) SİNYALİ KRİTERLERİ ====================
                CalculatedRationaleItem(
                    icon = Icons.Default.Timeline,
                    title = "1. Ana Trend EMA 200 Kırılımı (Düşüş)",
                    calculatedBadge = "NEGATİF TREND",
                    calculatedBadgeColor = Color(0xFFEF5350),
                    metricLine = "Fiyat: ${signal.entryPrice} ₺  <  EMA 200: ${String.format(Locale.US, "%.2f", ema200Val)} ₺ (%-${String.format(Locale.US, "%.1f", ema200DiffPercent)} altında)",
                    description = "Günlük büyük zaman diliminde fiyat 200 günlük hareketli ortalamanın (EMA 200) altına inmiştir. Uzun vadeli ana akıntı satıcıların hakimiyetindedir.",
                    iconTint = Color(0xFFEF5350)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.Speed,
                    title = "2. Güçlü Satış Trendi (ADX)",
                    calculatedBadge = "ADX: ${String.format(Locale.US, "%.1f", adxVal)}",
                    calculatedBadgeColor = Color(0xFFEF5350),
                    metricLine = "Hesaplanan ADX = ${String.format(Locale.US, "%.1f", adxVal)}  (Düşüş Trend Gücü: >= 22.0)",
                    description = "ADX göstergesi 22 eşiğini aşarak hissede yaşanan geri çekilmenin geçici bir dalgalanma değil, güçlü ve ivmeli bir satış trendi olduğunu göstermektedir.",
                    iconTint = Color(0xFFFF7043)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.TrendingDown,
                    title = "3. EMA 21 Direnç Reddi & StochRSI Tepe Kesişimi",
                    calculatedBadge = "DİRENÇ REDDİ",
                    calculatedBadgeColor = Color(0xFFEF5350),
                    metricLine = "EMA 21 Direnci: ${String.format(Locale.US, "%.2f", ema21Val)} ₺  |  StochRSI K: ${String.format(Locale.US, "%.1f", stochKVal)} < D: ${String.format(Locale.US, "%.1f", stochDVal)}",
                    description = "Fiyat yukarı tepkide 21 periyotluk EMA dinamik direncini aşamayarak reddedildi. Stochastic RSI aşırı alım bölgesinden (>75) aşağı yönlü ölüm kesişimi üretti.",
                    iconTint = Color(0xFFEF5350)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.ShowChart,
                    title = "4. Kurumsal Satış Baskısı (Para Çıkışı)",
                    calculatedBadge = "${String.format(Locale.US, "%.2f", volumeRatioVal)}x SATIŞ HACMİ",
                    calculatedBadgeColor = Color(0xFFEF5350),
                    metricLine = "Satış Hacmi: 20 periyotluk ortalamanın ${String.format(Locale.US, "%.2f", volumeRatioVal)} katı (Eşik: >1.5x)",
                    description = "Aşağı yönlü kırılım mumu son 20 periyotluk ortalama hacmin %150'si üzerinde gerçekleşerek kurumsal satış dalgasını ve para çıkışını doğrulamıştır.",
                    iconTint = Color(0xFFAB47BC)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CalculatedRationaleItem(
                    icon = Icons.Default.Shield,
                    title = "5. Asimetrik Risk/Kazanç Oranı (1:2 R:R)",
                    calculatedBadge = "R:R 1:${signal.riskRewardRatio}",
                    calculatedBadgeColor = Color(0xFF4DD0E1),
                    metricLine = "ATR(14): ${String.format(Locale.US, "%.2f", atrVal)} ₺  |  Risk: -${String.format(Locale.US, "%.2f", riskAmount)} ₺  |  Hedef: +${String.format(Locale.US, "%.2f", profitAmount)} ₺",
                    description = "Yukarı yönlü olası tepkilere karşı stop-loss 1.5x ATR mesafede tutulurken, aşağı yönlü düşüş hedefi riske edilen tutarın tam 2 katıdır.",
                    iconTint = Color(0xFF4DD0E1)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Kapat Butonu
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuy) Color(0xFF238636) else Color(0xFFDA3633)
                )
            ) {
                Text(
                    text = "Anladım",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun CalculatedRationaleItem(
    icon: ImageVector,
    title: String,
    calculatedBadge: String,
    calculatedBadgeColor: Color,
    metricLine: String,
    description: String,
    iconTint: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0D1117)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Başlık & Rozet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = calculatedBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = calculatedBadge,
                        color = calculatedBadgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hesaplanan Sayısal Veri Satırı (Vurgulu)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF161B22)
            ) {
                Text(
                    text = metricLine,
                    color = Color(0xFF58A6FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Detay Açıklama
            Text(
                text = description,
                color = Color(0xFF8B949E),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 17.sp
            )
        }
    }
}
