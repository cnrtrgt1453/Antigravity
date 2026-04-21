package com.antigravity.mobile.presentation.signals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.mobile.domain.model.MarketSignal
import com.antigravity.mobile.ui.theme.SuccessGreen
import com.antigravity.mobile.ui.theme.ErrorRed

@Composable
fun SignalsScreen(
    viewModel: SignalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Sinyaller",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Teknik analiz al-sat sinyalleri",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            item {
                FilterChip(
                    selected = uiState.activeFilter == SignalFilter.ALL,
                    onClick = { viewModel.applyFilter(SignalFilter.ALL) },
                    label = { Text("Hepsi") },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            item {
                FilterChip(
                    selected = uiState.activeFilter == SignalFilter.GOLDEN,
                    onClick = { viewModel.applyFilter(SignalFilter.GOLDEN) },
                    label = { Text("Golden Cross") },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SuccessGreen)
                )
            }
            item {
                FilterChip(
                    selected = uiState.activeFilter == SignalFilter.DEAD,
                    onClick = { viewModel.applyFilter(SignalFilter.DEAD) },
                    label = { Text("Dead Cross") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ErrorRed)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(uiState.filteredResults) { signal ->
                    SignalCard(signal = signal)
                }
            }
        }
    }
}

@Composable
fun SignalCard(signal: MarketSignal) {
    val isGolden = signal.signal == "GOLDEN_CROSS"
    val color = if (isGolden) SuccessGreen else ErrorRed

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = signal.ticker,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isGolden) "GOLDEN" else "DEAD",
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = signal.message ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Mevcut Fiyat", style = MaterialTheme.typography.labelSmall)
                    Text(signal.current_price.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Kesişim Fiyatı", style = MaterialTheme.typography.labelSmall)
                    Text(signal.cross_price?.toString() ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Tarih", style = MaterialTheme.typography.labelSmall)
                    Text(signal.cross_date ?: "-", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
