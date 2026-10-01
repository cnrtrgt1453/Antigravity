package com.antigravity.mobile.presentation.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.mobile.domain.model.News

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(top = 40.dp)
    ) {
        // Üst Başlık & Yenile Butonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KAP Haberleri",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFFF6C90E),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.currentMonthName} Bildirimleri",
                        color = Color(0xFF8B949E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Yenile (Refresh) Butonu
            Surface(
                color = Color(0xFF21262D),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF30363D)),
                modifier = Modifier.clickable(enabled = !uiState.refreshing) {
                    viewModel.refreshNews()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (uiState.refreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF58A6FF),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = Color(0xFF58A6FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = if (uiState.refreshing) "Yenileniyor..." else "Yenile",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filtreler Bölümü
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Hisse Arama Çubuğu
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text("Hisse kodu ara (örn. THYAO, GARAN)...", color = Color(0xFF8B949E), fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B949E), modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.selectSymbol(null)
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = Color(0xFF8B949E), modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF161B22),
                    unfocusedContainerColor = Color(0xFF161B22),
                    focusedBorderColor = Color(0xFF58A6FF),
                    unfocusedBorderColor = Color(0xFF30363D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF58A6FF)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Popüler Hisse Çipleri
            val popularSymbols = listOf("Tümü", "THYAO", "GARAN", "ASELS", "EREGL", "KCHOL", "TUPRS", "BIMAS", "SISE", "SASA", "AKBNK", "YKBNK", "PETKM", "KOZAL")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(popularSymbols) { symbol ->
                    val isSelected = if (symbol == "Tümü") {
                        uiState.selectedSymbol == null
                    } else {
                        uiState.selectedSymbol.equals(symbol, ignoreCase = true)
                    }
                    SymbolChip(
                        text = symbol,
                        isSelected = isSelected,
                        onClick = {
                            if (symbol == "Tümü") {
                                viewModel.selectSymbol(null)
                            } else {
                                viewModel.selectSymbol(symbol)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sıralama Butonları (Yeniden Eskiye / Eskiden Yeniye)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SortButton(
                    text = "Yeniden Eskiye (En Yeni)",
                    icon = Icons.Default.ArrowDownward,
                    isSelected = uiState.sortOrder == "desc",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setSortOrder("desc") }
                )
                SortButton(
                    text = "Eskiden Yeniye (En Eski)",
                    icon = Icons.Default.ArrowUpward,
                    isSelected = uiState.sortOrder == "asc",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setSortOrder("asc") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Haber Listesi
        if (uiState.isLoading && uiState.news.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF58A6FF))
            }
        } else if (uiState.news.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    border = BorderStroke(1.dp, Color(0xFF30363D))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Feed,
                            contentDescription = null,
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.selectedSymbol != null) {
                                "${uiState.selectedSymbol} için bu ay açıklanan KAP haberi bulunamadı."
                            } else {
                                "Bu ay için henüz kayıtlı bir KAP haberi bulunmuyor."
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Yeni açıklamaları kontrol etmek için Yenile butonuna dokunabilirsiniz.",
                            color = Color(0xFF8B949E),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshNews() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF238636),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Şimdi Yenile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.news) { item ->
                    NewsItemCard(
                        news = item,
                        onOpenUrl = { url ->
                            if (url.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF58A6FF))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) Color(0xFF58A6FF).copy(alpha = 0.15f) else Color(0xFF161B22),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF58A6FF) else Color(0xFF30363D)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF58A6FF) else Color(0xFF8B949E),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isSelected) Color(0xFF58A6FF) else Color(0xFF8B949E),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SymbolChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF238636).copy(alpha = 0.15f) else Color(0xFF161B22),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
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
fun NewsItemCard(news: News, onOpenUrl: (String) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = news.sourceUrl.isNotBlank()) { onOpenUrl(news.sourceUrl) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (news.stockSymbol.isNotBlank()) {
                    Surface(
                        color = Color(0xFFF6C90E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFF6C90E).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = news.stockSymbol,
                            color = Color(0xFFF6C90E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFF58A6FF).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "KAP",
                            color = Color(0xFF58A6FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Tarih formatlama
                val dateDisplay = remember(news.publishedAt) {
                    try {
                        val clean = news.publishedAt.replace("T", " ").replace("Z", "")
                        if (clean.length >= 16) {
                            val parts = clean.substring(0, 10).split("-")
                            if (parts.size == 3) {
                                "${parts[2]}.${parts[1]}.${parts[0]} ${clean.substring(11, 16)}"
                            } else clean.take(16)
                        } else news.publishedAt.take(10)
                    } catch (_: Exception) {
                        news.publishedAt.take(10)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateDisplay,
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = news.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 21.sp
            )
            
            if (news.content.isNotBlank() && news.content != news.title) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = news.content,
                    color = Color(0xFFC9D1D9),
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kamuyu Aydınlatma Platformu",
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "KAP'ta Aç",
                        color = Color(0xFF58A6FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color(0xFF58A6FF),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
