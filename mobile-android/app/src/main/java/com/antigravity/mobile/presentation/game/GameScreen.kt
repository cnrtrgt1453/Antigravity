package com.antigravity.mobile.presentation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.antigravity.mobile.domain.model.MarketFilter
import com.antigravity.mobile.presentation.components.AssetListItem
import com.antigravity.mobile.presentation.components.TradeBottomSheet
import com.antigravity.mobile.presentation.components.TradeStockCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val stocks = viewModel.stocksPagingData.collectAsLazyPagingItems()

    var selectedTab by remember { mutableStateOf(0) } // 0: Varlıklarım, 1: Hisse Al / Piyasalar
    var sheetData by remember { mutableStateOf<TradeSheetState?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.checkLoginStatus()
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearMessages()
        }
    }

    // Trade Bottom Sheet
    sheetData?.let { data ->
        val balance = uiState.portfolio?.balance ?: 500000.0
        val maxQty = if (data.type == "SELL") {
            uiState.portfolio?.items?.find { it.stockSymbol == data.symbol }?.quantity ?: 0L
        } else null

        TradeBottomSheet(
            symbol = data.symbol,
            price = data.price,
            tradeType = data.type,
            availableBalance = balance,
            maxQuantity = maxQty,
            onDismiss = { sheetData = null },
            onConfirm = { qty ->
                viewModel.trade(data.symbol, qty, data.price, data.type) { success, _ ->
                    if (success && data.type == "BUY") {
                        // Alım yapıldıktan sonra varlıklarım sekmesine geçiş
                        selectedTab = 0
                    }
                }
                sheetData = null
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0D1117),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Üst Başlık & Geçmiş İkonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portföyüm",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                if (isLoggedIn) {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.background(Color(0xFF161B22), CircleShape)
                    ) {
                        Icon(Icons.Default.History, contentDescription = "İşlem Geçmişi", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoggedIn) {
                // Giriş yapılmadıysa KİLİTLİ EKRAN
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        border = BorderStroke(1.dp, Color(0xFF30363D))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF6C90E).copy(alpha = 0.15f),
                                modifier = Modifier.size(68.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFF6C90E),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Borsa alım/satım simülasyonunu test etmek için lütfen giriş yapınız.",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "500.000 TL sanal bakiye ile BIST hisselerinde risksiz alım-satım simülasyonu yapmak ve kendi portföyünüzü oluşturmak için giriş yapınız.",
                                color = Color(0xFF8B949E),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onNavigateToLogin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GOOGLE İLE GİRİŞ YAP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else {
                // Giriş yapılmış: Bakiye Kartı & İkiye Bölünmüş Portföy
                val balance = uiState.portfolio?.balance ?: 500000.0
                
                // Portföydeki hisselerin toplam piyasa değeri
                val totalStockValue = uiState.portfolio?.items?.sumOf { item ->
                    val clean = item.stockSymbol.substringBefore(".")
                    val curPrice = uiState.stockPrices[clean] ?: item.averageCost
                    curPrice * item.quantity
                } ?: 0.0

                val totalNetWorth = balance + totalStockValue

                // Mor Bakiye Kartı (Kullanıcının görseldeki tasarımı)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF51388E)) // Görseldeki mor ton
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Toplam Bakiye (Demo)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatTurkishLira(balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                        if (totalStockValue > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Hisse Değeri: ${formatTurkishLira(totalStockValue)}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Toplam Varlık: ${formatTurkishLira(totalNetWorth)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF6C90E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // İkiye Bölünmüş Segmented Sekmeler (Tabs): Varlıklarım | Hisse Al
                val assetCount = uiState.portfolio?.items?.size ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF161B22))
                        .padding(4.dp)
                ) {
                    PortfolioTabButton(
                        text = "Varlıklarım ($assetCount)",
                        icon = Icons.Default.PieChart,
                        isSelected = selectedTab == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    PortfolioTabButton(
                        text = "Hisse Al / Piyasalar",
                        icon = Icons.Default.AddShoppingCart,
                        isSelected = selectedTab == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SEKME İÇERİKLERİ
                if (selectedTab == 0) {
                    // ==========================================
                    // SEKME 0: VARLIKLARIM (HER KULLANICIYA ÖZEL)
                    // ==========================================
                    val items = uiState.portfolio?.items ?: emptyList()

                    if (uiState.isLoading && uiState.portfolio == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF58A6FF))
                        }
                    } else if (items.isEmpty()) {
                        // Varlık Yoksa Boş Durum & Hisse Almaya Yönlendirme
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF21262D),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFF8B949E),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Henüz bir yatırımınız bulunmuyor.",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mevcut 500.000 TL sanal bakiyenizle 'Hisse Al / Piyasalar' sekmesinden dilediğiniz hisseye hemen yatırım yapabilirsiniz.",
                                    color = Color(0xFF8B949E),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { selectedTab = 1 },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF238636),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Hisse Almaya Başla", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = "Portföyümdeki Hisseler",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(items) { item ->
                                val clean = item.stockSymbol.substringBefore(".")
                                val currentPrice = uiState.stockPrices[clean]
                                    ?: uiState.marketData.find { it.ticker == item.stockSymbol || it.ticker.startsWith(clean) }?.current_price
                                    ?: item.averageCost

                                AssetListItem(
                                    item = item,
                                    currentPrice = currentPrice,
                                    onSellClick = { asset ->
                                        sheetData = TradeSheetState(
                                            symbol = asset.stockSymbol,
                                            price = currentPrice,
                                            type = "SELL"
                                        )
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // SEKME 1: HİSSE AL / PİYASALAR
                    // ==========================================
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Hisse Arama Çubuğu
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text("Hisse kodu veya adı ara (örn. THYAO)...", color = Color(0xFF8B949E), fontSize = 13.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B949E), modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = Color(0xFF8B949E), modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
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

                        // Hızlı Filtre Çipleri
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MarketFilterChip(
                                text = "Tüm Hisseler",
                                isSelected = selectedFilter == MarketFilter.ALL,
                                onClick = { viewModel.setFilter(MarketFilter.ALL) }
                            )
                            MarketFilterChip(
                                text = "BIST 30",
                                isSelected = selectedFilter == MarketFilter.BIST30,
                                onClick = { viewModel.setFilter(MarketFilter.BIST30) }
                            )
                            MarketFilterChip(
                                text = "BIST 100",
                                isSelected = selectedFilter == MarketFilter.BIST100,
                                onClick = { viewModel.setFilter(MarketFilter.BIST100) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tüm Hisselerin Listesi
                        if (stocks.loadState.refresh is LoadState.Loading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF58A6FF))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(
                                    count = stocks.itemCount,
                                    key = stocks.itemKey { it.symbol },
                                    contentType = stocks.itemContentType { "StockTradeItem" }
                                ) { index ->
                                    val stock = stocks[index]
                                    if (stock != null) {
                                        TradeStockCard(
                                            stock = stock,
                                            onBuyClick = { selectedStock ->
                                                val stockPrice = selectedStock.currentPrice ?: 0.0
                                                sheetData = TradeSheetState(
                                                    symbol = selectedStock.symbol.substringBefore("."),
                                                    price = if (stockPrice > 0.0) stockPrice else 100.0,
                                                    type = "BUY"
                                                )
                                            }
                                        )
                                    }
                                }

                                if (stocks.loadState.append is LoadState.Loading) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF58A6FF))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioTabButton(
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
        color = if (isSelected) Color(0xFF21262D) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF30363D)) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF58A6FF) else Color(0xFF8B949E),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFF8B949E),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun MarketFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF238636).copy(alpha = 0.15f) else Color(0xFF161B22),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF238636) else Color(0xFF30363D))
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF3FB950) else Color(0xFF8B949E),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

data class TradeSheetState(val symbol: String, val price: Double, val type: String)

fun formatTurkishLira(amount: Double): String {
    val symbols = java.text.DecimalFormatSymbols(java.util.Locale("tr", "TR")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = if (amount % 1.0 == 0.0) {
        java.text.DecimalFormat("#,##0", symbols)
    } else {
        java.text.DecimalFormat("#,##0.00", symbols)
    }
    return "${formatter.format(amount)} TL"
}
