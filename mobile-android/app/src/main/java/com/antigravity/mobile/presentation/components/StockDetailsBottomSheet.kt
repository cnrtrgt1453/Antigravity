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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.domain.model.OHLCData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsBottomSheet(
    symbol: String?,
    name: String?,
    ohlcData: OHLCData,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    if (symbol == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF30363D)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = symbol,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = name ?: "",
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color(0xFF30363D), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart
            StockChart(
                ohlcData = ohlcData,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer
            Text(
                text = "Son 1 Aylık Performans (Günlük)",
                color = Color(0xFF8B949E),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
