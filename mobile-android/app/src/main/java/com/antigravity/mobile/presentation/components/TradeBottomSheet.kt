package com.antigravity.mobile.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.mobile.ui.theme.SuccessGreen
import com.antigravity.mobile.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeBottomSheet(
    symbol: String,
    price: Double,
    tradeType: String, // BUY, SELL
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    val isBuy = tradeType == "BUY"
    val totalAmount = (quantity.toLongOrNull() ?: 0L) * price

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (isBuy) "Hisse Alımı" else "Hisse Satımı",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "$symbol - ${String.format("%.2f TL", price)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                label = { Text("Adet") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Toplam Tutar", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = String.format("%.2f TL", totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { quantity.toLongOrNull()?.let { onConfirm(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuy) SuccessGreen else ErrorRed
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (isBuy) "Yatırımı Onayla" else "Satışı Onayla",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
