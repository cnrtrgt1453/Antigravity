package com.antigravity.mobile.presentation.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.antigravity.mobile.presentation.components.StockDetailsBottomSheet
import com.antigravity.mobile.presentation.components.StockListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel = hiltViewModel()
) {
    val stocks = viewModel.stocksPagingData.collectAsLazyPagingItems()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val isChartLoading by viewModel.isChartLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Piyasalar",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(stocks) { stock ->
                stock?.let {
                    StockListItem(
                        stock = it,
                        onItemClick = { viewModel.selectStock(it) },
                        onWatchlistToggle = { s -> viewModel.toggleWatchlist(s) }
                    )
                }
            }

            when (val state = stocks.loadState.append) {
                is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        Text(
                            text = "Daha fazla yüklenemedi: ${state.error.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                else -> {}
            }
        }
    }

    if (selectedStock != null) {
        StockDetailsBottomSheet(
            symbol = selectedStock?.symbol,
            name = selectedStock?.name,
            ohlcData = chartData,
            isLoading = isChartLoading,
            onDismiss = { viewModel.selectStock(null) }
        )
    }
}
