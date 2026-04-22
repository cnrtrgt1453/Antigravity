package com.antigravity.mobile.presentation.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.mobile.domain.model.News

@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(top = 40.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Haberler & Analiz",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Surface(
                color = Color(0xFF238636),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clickable { /* Trigger Report */ }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("📊 Rapor", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filters
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161B22))
                    .padding(4.dp)
            ) {
                TabItem(
                    text = "Takiplerim",
                    isSelected = uiState.watchlistOnly,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.toggleWatchlistOnly(true) }
                )
                TabItem(
                    text = "Tümü",
                    isSelected = !uiState.watchlistOnly,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.toggleWatchlistOnly(false) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chips & Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SymbolChip(
                            text = "Hepsi",
                            isSelected = uiState.selectedSymbol == null,
                            onClick = { viewModel.selectSymbol(null) }
                        )
                    }
                    val dummySymbols = listOf("THYAO", "EREGL", "ASELS", "SISE", "SASA")
                    items(dummySymbols) { symbol ->
                        SymbolChip(
                            text = symbol,
                            isSelected = uiState.selectedSymbol == symbol,
                            onClick = { viewModel.selectSymbol(symbol) }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    color = Color(0xFF21262D),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { viewModel.toggleSortOrder() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFC9D1D9), modifier = Modifier.size(14.dp))
                        Text(
                            text = if (uiState.sortOrder.endsWith("desc")) "Yeni" else "Eski",
                            color = Color(0xFFC9D1D9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // News List
        if (uiState.isLoading && uiState.news.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF58A6FF))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.news) { item ->
                    NewsItemCard(news = item)
                }
                
                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF58A6FF))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) Color(0xFF21262D) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)) else null
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF8B949E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SymbolChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF238636).copy(alpha = 0.1f) else Color(0xFF161B22),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF238636) else Color(0xFF30363D)
        )
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF3FB950) else Color(0xFF8B949E),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun NewsItemCard(news: News) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF58A6FF).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = news.stockSymbol,
                        color = Color(0xFF58A6FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = news.publishedAt.take(10),
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = news.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = news.content,
                color = Color(0xFFC9D1D9),
                fontSize = 13.sp,
                maxLines = 3,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Devamını Oku →",
                color = Color(0xFF58A6FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
