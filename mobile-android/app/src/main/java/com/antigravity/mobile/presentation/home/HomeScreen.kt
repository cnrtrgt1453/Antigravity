package com.antigravity.mobile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.antigravity.mobile.presentation.components.StockDetailsBottomSheet
import com.antigravity.mobile.presentation.components.StockListItem
import com.antigravity.mobile.presentation.components.TrendCard

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Üst Boşluk
        item {
            Spacer(modifier = Modifier.height(44.dp))
        }

        // 1. Dolar, Euro, Pound ve Gram Altın
        item {
            if (uiState.isLoading && uiState.summaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else if (uiState.summaries.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.summaries) { summary ->
                        TrendCard(summary = summary)
                    }
                }
            }
        }

        // Başlık: BIST 30 Hisseleri
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BIST 30 Hisseleri",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (uiState.bist30Stocks.isNotEmpty()) {
                    Text(
                        text = "${uiState.bist30Stocks.size} Hisse",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF8B949E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Yükleniyor Göstergesi
        if (uiState.isBist30Loading && uiState.bist30Stocks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // 2. BIST 30 Hisse Listesi (İsimleri ve Fiyatları)
        items(uiState.bist30Stocks, key = { it.symbol }) { stock ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                StockListItem(
                    stock = stock,
                    onItemClick = { clickedStock ->
                        if (viewModel.isLoggedIn()) {
                            viewModel.selectStock(clickedStock)
                        } else {
                            onNavigateToLogin()
                        }
                    },
                    onWatchlistToggle = { toggledStock ->
                        if (viewModel.isLoggedIn()) {
                            viewModel.toggleWatchlist(toggledStock)
                        } else {
                            onNavigateToLogin()
                        }
                    }
                )
            }
        }
    }

    if (viewModel.isLoggedIn() && uiState.selectedStock != null) {
        StockDetailsBottomSheet(
            symbol = uiState.selectedStock?.symbol,
            name = uiState.selectedStock?.name,
            stock = uiState.selectedStock,
            ohlcData = uiState.chartData,
            isLoading = uiState.isChartLoading,
            onDismiss = { viewModel.selectStock(null) }
        )
    }
}
