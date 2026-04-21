package com.antigravity.mobile.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.mobile.domain.model.TradeHistory
import com.antigravity.mobile.ui.theme.SuccessGreen
import com.antigravity.mobile.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeHistoryScreen(
    viewModel: TradeHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İşlem Geçmişi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Chips
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                FilterChip(
                    selected = uiState.activeFilter == HistoryFilter.ALL,
                    onClick = { viewModel.applyFilter(HistoryFilter.ALL) },
                    label = { Text("Hepsi") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = uiState.activeFilter == HistoryFilter.BUY,
                    onClick = { viewModel.applyFilter(HistoryFilter.BUY) },
                    label = { Text("Alımlar") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = uiState.activeFilter == HistoryFilter.SELL,
                    onClick = { viewModel.applyFilter(HistoryFilter.SELL) },
                    label = { Text("Satımlar") }
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.filteredResults) { item ->
                        TransactionListItem(item = item)
                    }
                    
                    if (uiState.filteredResults.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                                Text("Henüz bir işlem kaydı bulunmuyor.", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(item: TradeHistory) {
    val isBuy = item.type == "BUY"
    val color = if (isBuy) SuccessGreen else ErrorRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.stockSymbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isBuy) "ALIM" else "SATIM",
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Miktar: ${item.quantity} Adet", style = MaterialTheme.typography.bodySmall)
                Text("Birim: ${String.format("%.2f TL", item.price)}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = String.format("%.2f TL", item.totalAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
