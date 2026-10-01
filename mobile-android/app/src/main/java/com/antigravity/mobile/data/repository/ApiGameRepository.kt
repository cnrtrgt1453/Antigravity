package com.antigravity.mobile.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.domain.model.Portfolio
import com.antigravity.mobile.domain.model.PortfolioItem
import com.antigravity.mobile.domain.model.TradeHistory
import com.antigravity.mobile.domain.repository.GameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@Serializable
internal data class SupabasePortfolioItemDto(
    val id: Long = 0,
    @SerialName("portfolio_id") val portfolioId: Long = 0,
    @SerialName("stock_symbol") val stockSymbol: String,
    val quantity: Long,
    @SerialName("average_cost") val averageCost: Double
)

@Serializable
internal data class SupabasePortfolioDto(
    val id: Long = 0,
    val balance: Double = 500000.0,
    val items: List<SupabasePortfolioItemDto> = emptyList(),
    @SerialName("portfolio_items") val portfolioItems: List<SupabasePortfolioItemDto> = emptyList()
)

@Serializable
internal data class SupabasePortfolioInsertDto(
    @SerialName("user_id") val userId: String,
    val balance: Double = 500000.0
)

@Serializable
internal data class SupabaseBalancePatchDto(
    val balance: Double
)

@Serializable
internal data class SupabaseQuantityPatchDto(
    val quantity: Long
)

@Serializable
internal data class SupabasePortfolioItemInsertDto(
    @SerialName("portfolio_id") val portfolioId: Long,
    @SerialName("stock_symbol") val stockSymbol: String,
    val quantity: Long,
    @SerialName("average_cost") val averageCost: Double
)

@Serializable
internal data class SupabasePortfolioItemUpdateDto(
    val quantity: Long,
    @SerialName("average_cost") val averageCost: Double
)

@Serializable
internal data class SupabaseTradeHistoryDto(
    val id: Long = 0,
    @SerialName("stock_symbol") val stockSymbol: String,
    val type: String,
    val quantity: Long,
    val price: Double,
    val commission: Double,
    @SerialName("total_amount") val totalAmount: Double,
    val timestamp: String
)

@Serializable
internal data class SupabaseTradeHistoryInsertDto(
    @SerialName("user_id") val userId: String,
    @SerialName("stock_symbol") val stockSymbol: String,
    val type: String,
    val quantity: Long,
    val price: Double,
    val commission: Double,
    @SerialName("total_amount") val totalAmount: Double,
    val timestamp: String
)

@Serializable
internal data class RpcTradeRequest(
    val p_symbol: String,
    val p_quantity: Long,
    val p_price: Double
)

@Serializable
internal data class LocalPortfolioItem(
    val id: Long,
    val stockSymbol: String,
    val quantity: Long,
    val averageCost: Double
)

@Serializable
internal data class LocalPortfolio(
    val id: Long = 1,
    val balance: Double = 500000.0,
    val items: List<LocalPortfolioItem> = emptyList()
)

@Serializable
internal data class LocalTradeHistoryItem(
    val id: Long,
    val stockSymbol: String,
    val type: String,
    val quantity: Long,
    val price: Double,
    val commission: Double,
    val totalAmount: Double,
    val timestamp: String
)

class ApiGameRepository @Inject constructor(
    private val client: HttpClient,
    private val config: SupabaseConfig,
    @ApplicationContext private val context: Context
) : GameRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("financeup_game_prefs", Context.MODE_PRIVATE)
    }

    private val restUrl: String
        get() = "${SupabaseConfig.SUPABASE_URL}/rest/v1"

    private fun getUserKey(): String {
        return config.getUserId() ?: "demo_user"
    }

    private fun getLocalPortfolio(userKey: String): Portfolio {
        val raw = prefs.getString("portfolio_$userKey", null)
        if (raw != null) {
            try {
                val local = json.decodeFromString<LocalPortfolio>(raw)
                val actualBalance = if (local.balance == 750000.0) 500000.0 else local.balance
                val portfolio = Portfolio(
                    id = local.id,
                    balance = actualBalance,
                    items = local.items.map {
                        PortfolioItem(
                            id = it.id,
                            stockSymbol = it.stockSymbol,
                            quantity = it.quantity,
                            averageCost = it.averageCost
                        )
                    }
                )
                if (local.balance == 750000.0) {
                    saveLocalPortfolio(userKey, portfolio)
                }
                return portfolio
            } catch (e: Exception) {
                // Hata durumunda varsayılan dön
            }
        }
        val defaultPortfolio = Portfolio(id = 1, balance = 500000.0, items = emptyList())
        saveLocalPortfolio(userKey, defaultPortfolio)
        return defaultPortfolio
    }

    private fun saveLocalPortfolio(userKey: String, portfolio: Portfolio) {
        val local = LocalPortfolio(
            id = portfolio.id,
            balance = portfolio.balance,
            items = portfolio.items.map {
                LocalPortfolioItem(
                    id = it.id,
                    stockSymbol = it.stockSymbol,
                    quantity = it.quantity,
                    averageCost = it.averageCost
                )
            }
        )
        prefs.edit().putString("portfolio_$userKey", json.encodeToString(local)).apply()
    }

    private fun getLocalHistory(userKey: String): List<TradeHistory> {
        val raw = prefs.getString("history_$userKey", null) ?: return emptyList()
        return try {
            val list = json.decodeFromString<List<LocalTradeHistoryItem>>(raw)
            list.map {
                TradeHistory(
                    id = it.id,
                    stockSymbol = it.stockSymbol,
                    type = it.type,
                    quantity = it.quantity,
                    price = it.price,
                    commission = it.commission,
                    totalAmount = it.totalAmount,
                    timestamp = it.timestamp
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveLocalHistory(userKey: String, list: List<TradeHistory>) {
        val dtoList = list.map {
            LocalTradeHistoryItem(
                id = it.id,
                stockSymbol = it.stockSymbol,
                type = it.type,
                quantity = it.quantity,
                price = it.price,
                commission = it.commission,
                totalAmount = it.totalAmount,
                timestamp = it.timestamp
            )
        }
        prefs.edit().putString("history_$userKey", json.encodeToString(dtoList)).apply()
    }

    private fun saveLocalHistoryItem(userKey: String, item: LocalTradeHistoryItem) {
        val existing = getLocalHistory(userKey).toMutableList()
        existing.add(
            0,
            TradeHistory(
                id = item.id,
                stockSymbol = item.stockSymbol,
                type = item.type,
                quantity = item.quantity,
                price = item.price,
                commission = item.commission,
                totalAmount = item.totalAmount,
                timestamp = item.timestamp
            )
        )
        val dtoList = existing.map {
            LocalTradeHistoryItem(
                id = it.id,
                stockSymbol = it.stockSymbol,
                type = it.type,
                quantity = it.quantity,
                price = it.price,
                commission = it.commission,
                totalAmount = it.totalAmount,
                timestamp = it.timestamp
            )
        }
        prefs.edit().putString("history_$userKey", json.encodeToString(dtoList)).apply()
    }

    override suspend fun getPortfolio(): Result<Portfolio> {
        val userKey = getUserKey()
        return try {
            var domainPortfolio: Portfolio? = null

            // 1. Supabase'den kullanıcıya özel portföyü çek
            if (config.isLoggedIn()) {
                val userId = config.getUserId()
                config.refreshTokenIfNeeded(client)
                try {
                    // Portföy başlığını çek
                    val response = client.get("$restUrl/portfolios?select=*") {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header("Authorization", config.getAuthHeader())
                    }
                    if (response.status.value in 200..299) {
                        val dtoList = response.body<List<SupabasePortfolioDto>>()
                        var targetDto = dtoList.firstOrNull()

                        // Kullanıcının veritabanında henüz portföy satırı yoksa otomatik oluştur
                        if (targetDto == null && !userId.isNullOrEmpty()) {
                            val createResp = client.post("$restUrl/portfolios") {
                                contentType(ContentType.Application.Json)
                                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                header("Authorization", config.getAuthHeader())
                                header("Prefer", "return=representation")
                                setBody(SupabasePortfolioInsertDto(userId = userId, balance = 500000.0))
                            }
                            if (createResp.status.value in 200..299) {
                                val createdList = createResp.body<List<SupabasePortfolioDto>>()
                                targetDto = createdList.firstOrNull()
                            } else {
                                Log.e("ApiGameRepository", "Failed to create remote portfolio: ${createResp.status} - ${createResp.bodyAsText()}")
                            }
                        }

                        if (targetDto != null) {
                            var currentBalance = targetDto.balance
                            if (currentBalance == 750000.0) {
                                currentBalance = 500000.0
                                try {
                                    client.patch("$restUrl/portfolios?id=eq.${targetDto.id}") {
                                        contentType(ContentType.Application.Json)
                                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                        header("Authorization", config.getAuthHeader())
                                        setBody(SupabaseBalancePatchDto(balance = 500000.0))
                                    }
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }

                            // Portföydeki hisse kalemlerini DOĞRUDAN portfolio_items tablosundan çek
                            var fetchedItems: List<PortfolioItem> = emptyList()
                            try {
                                val itemsResp = client.get("$restUrl/portfolio_items?portfolio_id=eq.${targetDto.id}&select=*") {
                                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                    header("Authorization", config.getAuthHeader())
                                }
                                if (itemsResp.status.value in 200..299) {
                                    val itemsDtoList = itemsResp.body<List<SupabasePortfolioItemDto>>()
                                    fetchedItems = itemsDtoList
                                        .filter { it.quantity > 0 }
                                        .map {
                                            PortfolioItem(
                                                id = it.id,
                                                stockSymbol = it.stockSymbol,
                                                quantity = it.quantity,
                                                averageCost = it.averageCost
                                            )
                                        }
                                } else {
                                    Log.e("ApiGameRepository", "portfolio_items fetch error: ${itemsResp.status} - ${itemsResp.bodyAsText()}")
                                }
                            } catch (itemEx: Exception) {
                                Log.e("ApiGameRepository", "portfolio_items fetch exception: ${itemEx.message}", itemEx)
                            }

                            domainPortfolio = Portfolio(
                                id = targetDto.id,
                                balance = currentBalance,
                                items = fetchedItems
                            )
                            saveLocalPortfolio(userKey, domainPortfolio)
                        }
                    } else {
                        Log.e("ApiGameRepository", "getPortfolio remote error: ${response.status} - ${response.bodyAsText()}")
                    }
                } catch (e: Exception) {
                    Log.e("ApiGameRepository", "getPortfolio remote exception: ${e.message}", e)
                }
            }

            if (domainPortfolio == null) {
                domainPortfolio = getLocalPortfolio(userKey)
            }

            Result.success(domainPortfolio)
        } catch (e: Exception) {
            Result.success(getLocalPortfolio(userKey))
        }
    }

    override suspend fun getTradeHistory(): Result<List<TradeHistory>> {
        val userKey = getUserKey()
        return try {
            if (config.isLoggedIn()) {
                config.refreshTokenIfNeeded(client)
                try {
                    val response = client.get("$restUrl/trade_history?select=*&order=timestamp.desc") {
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header("Authorization", config.getAuthHeader())
                    }
                    if (response.status.value in 200..299) {
                        val dtoList = response.body<List<SupabaseTradeHistoryDto>>()
                        val remoteList = dtoList.map {
                            TradeHistory(
                                id = it.id,
                                stockSymbol = it.stockSymbol.substringBefore("."),
                                type = it.type,
                                quantity = it.quantity,
                                price = it.price,
                                commission = it.commission,
                                totalAmount = it.totalAmount,
                                timestamp = it.timestamp
                            )
                        }
                        saveLocalHistory(userKey, remoteList)
                        return Result.success(remoteList)
                    } else {
                        Log.e("ApiGameRepository", "getTradeHistory error: ${response.status} - ${response.bodyAsText()}")
                    }
                } catch (e: Exception) {
                    Log.e("ApiGameRepository", "getTradeHistory exception: ${e.message}", e)
                }
            }
            Result.success(getLocalHistory(userKey))
        } catch (e: Exception) {
            Result.success(getLocalHistory(userKey))
        }
    }

    override suspend fun buyStock(symbol: String, quantity: Long, price: Double): Result<Portfolio> {
        val userKey = getUserKey()
        val cleanSymbol = symbol.substringBefore(".").trim().uppercase()
        val currentPortfolio = getLocalPortfolio(userKey)

        val totalStockCost = quantity * price
        val commission = totalStockCost * 0.01 // %1 komisyon
        val totalDeduction = totalStockCost + commission

        val symbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,##0.00", symbols)

        if (currentPortfolio.balance < totalDeduction) {
            return Result.failure(
                Exception("Yetersiz bakiye! İşlem için ${df.format(totalDeduction)} TL gerekiyor. Mevcut bakiye: ${df.format(currentPortfolio.balance)} TL")
            )
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        if (!config.isLoggedIn()) {
            return Result.failure(Exception("Alım işlemi yapabilmek için lütfen giriş yapınız."))
        }

        // Token geçerliliğini kontrol et ve gerekiyorsa otomatik yenile
        config.refreshTokenIfNeeded(client)

        val userId = config.getUserId()
        if (userId.isNullOrEmpty()) {
            return Result.failure(Exception("Kullanıcı oturumu bulunamadı. Lütfen tekrar giriş yapınız."))
        }

        var databaseSaved = false
        var lastDbError: String? = null

        // 1.A. Supabase RPC çağrısını dene (Atomik Fonksiyon)
        try {
            val rpcResponse = client.post("$restUrl/rpc/buy_stock") {
                contentType(ContentType.Application.Json)
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
                setBody(RpcTradeRequest(cleanSymbol, quantity, price))
            }
            if (rpcResponse.status.value in 200..299) {
                Log.d("ApiGameRepository", "buyStock RPC succeeded for $cleanSymbol")
                databaseSaved = true
            } else {
                lastDbError = rpcResponse.bodyAsText()
                Log.w("ApiGameRepository", "buy_stock RPC failed (${rpcResponse.status.value}): $lastDbError. Direct REST fallback will be used.")
            }
        } catch (e: Exception) {
            lastDbError = e.message
            Log.w("ApiGameRepository", "buy_stock RPC exception: ${e.message}. Direct REST fallback will be used.")
        }

        // 1.B. RPC başarısız olduysa doğrudan PostgREST veritabanı kayıt fallback'i
        if (!databaseSaved) {
            try {
                val portResp = client.get("$restUrl/portfolios?select=*") {
                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    header("Authorization", config.getAuthHeader())
                }
                var remotePortfolio: SupabasePortfolioDto? = null
                if (portResp.status.value in 200..299) {
                    val list = portResp.body<List<SupabasePortfolioDto>>()
                    if (list.isNotEmpty()) {
                        remotePortfolio = list.first()
                    } else {
                        val createResp = client.post("$restUrl/portfolios") {
                            contentType(ContentType.Application.Json)
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header("Authorization", config.getAuthHeader())
                            header("Prefer", "return=representation")
                            setBody(SupabasePortfolioInsertDto(userId = userId, balance = 500000.0))
                        }
                        if (createResp.status.value in 200..299) {
                            remotePortfolio = createResp.body<List<SupabasePortfolioDto>>().firstOrNull()
                        } else {
                            val createErr = createResp.bodyAsText()
                            lastDbError = "Portföy oluşturulamadı: $createErr"
                            Log.e("ApiGameRepository", "Direct create portfolio error: ${createResp.status} - $createErr")
                        }
                    }
                } else {
                    val portErr = portResp.bodyAsText()
                    lastDbError = "Portföy bilgisi alınamadı: $portErr"
                    Log.e("ApiGameRepository", "Direct get portfolio error: ${portResp.status} - $portErr")
                }

                if (remotePortfolio != null) {
                    if (remotePortfolio.balance < totalDeduction) {
                        return Result.failure(
                            Exception("Yetersiz bakiye! İşlem için ${df.format(totalDeduction)} TL gerekiyor. Mevcut bakiye: ${df.format(remotePortfolio.balance)} TL")
                        )
                    }

                    val newBalance = remotePortfolio.balance - totalDeduction

                    // 1. Bakiyeyi güncelle (SupabaseBalancePatchDto ile güvenli serileştirme)
                    val patchPortResp = client.patch("$restUrl/portfolios?id=eq.${remotePortfolio.id}") {
                        contentType(ContentType.Application.Json)
                        header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                        header("Authorization", config.getAuthHeader())
                        setBody(SupabaseBalancePatchDto(balance = newBalance))
                    }
                    if (patchPortResp.status.value !in 200..299) {
                        Log.e("ApiGameRepository", "Portfolio balance patch failed: ${patchPortResp.status} - ${patchPortResp.bodyAsText()}")
                    }

                    // 2. Mevcut kalemleri doğrudan portfolio_items tablosundan çek
                    var existingItems: List<SupabasePortfolioItemDto> = emptyList()
                    try {
                        val curItemsResp = client.get("$restUrl/portfolio_items?portfolio_id=eq.${remotePortfolio.id}&select=*") {
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header("Authorization", config.getAuthHeader())
                        }
                        if (curItemsResp.status.value in 200..299) {
                            existingItems = curItemsResp.body<List<SupabasePortfolioItemDto>>()
                        }
                    } catch (e: Exception) {
                        Log.e("ApiGameRepository", "Error fetching existing items in fallback: ${e.message}")
                    }

                    val existingItem = existingItems.firstOrNull {
                        it.stockSymbol.equals(cleanSymbol, ignoreCase = true) ||
                        it.stockSymbol.equals("$cleanSymbol.IS", ignoreCase = true)
                    }

                    var itemSaved = false
                    if (existingItem != null) {
                        val newQty = existingItem.quantity + quantity
                        val newAvg = ((existingItem.quantity * existingItem.averageCost) + totalStockCost) / newQty
                        val patchItemResp = client.patch("$restUrl/portfolio_items?id=eq.${existingItem.id}") {
                            contentType(ContentType.Application.Json)
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header("Authorization", config.getAuthHeader())
                            setBody(SupabasePortfolioItemUpdateDto(quantity = newQty, averageCost = newAvg))
                        }
                        if (patchItemResp.status.value in 200..299) {
                            itemSaved = true
                        } else {
                            val err = patchItemResp.bodyAsText()
                            Log.e("ApiGameRepository", "portfolio_item patch failed: ${patchItemResp.status} - $err")
                            lastDbError = "Portföy kalemi güncellenemedi (${patchItemResp.status.value}): $err"
                        }
                    } else {
                        val postItemResp = client.post("$restUrl/portfolio_items") {
                            contentType(ContentType.Application.Json)
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header("Authorization", config.getAuthHeader())
                            header("Prefer", "return=representation")
                            setBody(SupabasePortfolioItemInsertDto(
                                portfolioId = remotePortfolio.id,
                                stockSymbol = cleanSymbol,
                                quantity = quantity,
                                averageCost = price
                            ))
                        }
                        if (postItemResp.status.value in 200..299) {
                            itemSaved = true
                        } else {
                            val err = postItemResp.bodyAsText()
                            Log.e("ApiGameRepository", "portfolio_item post failed: ${postItemResp.status} - $err")
                            lastDbError = "Hisse portföye eklenemedi (${postItemResp.status.value}): $err"
                        }
                    }

                    if (!itemSaved) {
                        // Bakiye değişikliğini geri al (Rollback)
                        try {
                            client.patch("$restUrl/portfolios?id=eq.${remotePortfolio.id}") {
                                contentType(ContentType.Application.Json)
                                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                header("Authorization", config.getAuthHeader())
                                setBody(SupabaseBalancePatchDto(balance = remotePortfolio.balance))
                            }
                        } catch (revertEx: Exception) {
                            Log.e("ApiGameRepository", "Rollback failed: ${revertEx.message}")
                        }
                        return Result.failure(Exception("Hisse portföye kaydedilemedi! $lastDbError"))
                    }

                    // 3. Kullanıcıya özel işlem geçmişine ekle (trade_history)
                    try {
                        val histResp = client.post("$restUrl/trade_history") {
                            contentType(ContentType.Application.Json)
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header("Authorization", config.getAuthHeader())
                            setBody(SupabaseTradeHistoryInsertDto(
                                userId = userId,
                                stockSymbol = cleanSymbol,
                                type = "BUY",
                                quantity = quantity,
                                price = price,
                                commission = commission,
                                totalAmount = totalDeduction,
                                timestamp = timestamp
                            ))
                        }
                        if (histResp.status.value !in 200..299) {
                            val histErr = histResp.bodyAsText()
                            Log.w("ApiGameRepository", "trade_history insert error for $cleanSymbol: $histErr. Retrying with .IS")
                            val retryResp = client.post("$restUrl/trade_history") {
                                contentType(ContentType.Application.Json)
                                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                header("Authorization", config.getAuthHeader())
                                setBody(SupabaseTradeHistoryInsertDto(
                                    userId = userId,
                                    stockSymbol = "$cleanSymbol.IS",
                                    type = "BUY",
                                    quantity = quantity,
                                    price = price,
                                    commission = commission,
                                    totalAmount = totalDeduction,
                                    timestamp = timestamp
                                ))
                            }
                            if (retryResp.status.value !in 200..299) {
                                Log.e("ApiGameRepository", "trade_history retry insert failed: ${retryResp.status} - ${retryResp.bodyAsText()}")
                            }
                        }
                    } catch (histEx: Exception) {
                        Log.e("ApiGameRepository", "Error inserting trade_history: ${histEx.message}")
                    }

                    databaseSaved = true
                    Log.d("ApiGameRepository", "buyStock direct database sync succeeded for user $userId, symbol $cleanSymbol")
                }
            } catch (e: Exception) {
                lastDbError = e.message
                Log.e("ApiGameRepository", "buyStock direct database fallback error: ${e.message}", e)
            }
        }

        if (!databaseSaved) {
            return Result.failure(
                Exception("Alım işlemi veritabanına kaydedilemedi! Lütfen Supabase SQL ayarlarını ve internet bağlantınızı kontrol ediniz. (Hata: ${lastDbError ?: "Bağlantı hatası"})")
            )
        }

        // 2. Yerel Atomik Portföy ve Geçmiş Güncellemesi (Cache & Anlık UI Yansıması)
        val newBalance = currentPortfolio.balance - totalDeduction
        val currentItems = currentPortfolio.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.stockSymbol.equals(cleanSymbol, ignoreCase = true) ||
            it.stockSymbol.equals("$cleanSymbol.IS", ignoreCase = true)
        }

        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            val newQty = existing.quantity + quantity
            val newAvgCost = ((existing.quantity * existing.averageCost) + totalStockCost) / newQty
            currentItems[existingIndex] = existing.copy(
                quantity = newQty,
                averageCost = newAvgCost
            )
        } else {
            currentItems.add(
                PortfolioItem(
                    id = System.currentTimeMillis(),
                    stockSymbol = cleanSymbol,
                    quantity = quantity,
                    averageCost = price
                )
            )
        }

        val updatedPortfolio = Portfolio(
            id = currentPortfolio.id,
            balance = newBalance,
            items = currentItems
        )
        saveLocalPortfolio(userKey, updatedPortfolio)
        saveLocalHistoryItem(
            userKey,
            LocalTradeHistoryItem(
                id = System.currentTimeMillis(),
                stockSymbol = cleanSymbol,
                type = "BUY",
                quantity = quantity,
                price = price,
                commission = commission,
                totalAmount = totalDeduction,
                timestamp = timestamp
            )
        )

        // 3. Veritabanından en güncel portföyü çekip yansıt
        val freshRes = getPortfolio()
        if (freshRes.isSuccess) {
            val freshPort = freshRes.getOrNull()
            if (freshPort != null) {
                return freshRes
            }
        }

        return Result.success(updatedPortfolio)
    }

    override suspend fun sellStock(symbol: String, quantity: Long, price: Double): Result<Portfolio> {
        val userKey = getUserKey()
        val cleanSymbol = symbol.substringBefore(".").trim().uppercase()
        val currentPortfolio = getLocalPortfolio(userKey)

        val currentItems = currentPortfolio.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst {
            it.stockSymbol.equals(cleanSymbol, ignoreCase = true) ||
            it.stockSymbol.equals("$cleanSymbol.IS", ignoreCase = true)
        }

        val totalStockRevenue = quantity * price
        val commission = totalStockRevenue * 0.01 // %1 komisyon
        val netGain = totalStockRevenue - commission

        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        if (!config.isLoggedIn()) {
            return Result.failure(Exception("Satış işlemi yapabilmek için lütfen giriş yapınız."))
        }

        // Token geçerliliğini kontrol et ve gerekiyorsa otomatik yenile
        config.refreshTokenIfNeeded(client)

        val userId = config.getUserId()
        if (userId.isNullOrEmpty()) {
            return Result.failure(Exception("Kullanıcı oturumu bulunamadı. Lütfen tekrar giriş yapınız."))
        }

        var databaseSaved = false
        var lastDbError: String? = null

        // 1.A. Supabase RPC çağrısını dene (Atomik Fonksiyon)
        try {
            val rpcResponse = client.post("$restUrl/rpc/sell_stock") {
                contentType(ContentType.Application.Json)
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", config.getAuthHeader())
                setBody(RpcTradeRequest(cleanSymbol, quantity, price))
            }
            if (rpcResponse.status.value in 200..299) {
                Log.d("ApiGameRepository", "sellStock RPC succeeded for $cleanSymbol")
                databaseSaved = true
            } else {
                lastDbError = rpcResponse.bodyAsText()
                Log.w("ApiGameRepository", "sell_stock RPC failed (${rpcResponse.status.value}): $lastDbError. Direct REST fallback will be used.")
            }
        } catch (e: Exception) {
            lastDbError = e.message
            Log.w("ApiGameRepository", "sell_stock RPC exception: ${e.message}. Direct REST fallback will be used.")
        }

        // 1.B. RPC başarısız olduysa doğrudan PostgREST veritabanı kayıt fallback'i
        if (!databaseSaved) {
            try {
                val portResp = client.get("$restUrl/portfolios?select=*") {
                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    header("Authorization", config.getAuthHeader())
                }
                if (portResp.status.value in 200..299) {
                    val list = portResp.body<List<SupabasePortfolioDto>>()
                    val remotePortfolio = list.firstOrNull()

                    if (remotePortfolio != null) {
                        var remoteItems: List<SupabasePortfolioItemDto> = emptyList()
                        try {
                            val itemsResp = client.get("$restUrl/portfolio_items?portfolio_id=eq.${remotePortfolio.id}&select=*") {
                                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                header("Authorization", config.getAuthHeader())
                            }
                            if (itemsResp.status.value in 200..299) {
                                remoteItems = itemsResp.body<List<SupabasePortfolioItemDto>>()
                            }
                        } catch (e: Exception) {
                            Log.e("ApiGameRepository", "Error fetching remote items for sell: ${e.message}")
                        }

                        val existingItem = remoteItems.firstOrNull {
                            it.stockSymbol.equals(cleanSymbol, ignoreCase = true) ||
                            it.stockSymbol.equals("$cleanSymbol.IS", ignoreCase = true)
                        }

                        if (existingItem == null || existingItem.quantity < quantity) {
                            val owned = existingItem?.quantity ?: 0L
                            return Result.failure(Exception("Yetersiz hisse adedi! Portföyünüzde $owned lot $cleanSymbol bulunmaktadır."))
                        }

                        val newBalance = remotePortfolio.balance + netGain

                        // 1. Bakiyeyi güncelle (SupabaseBalancePatchDto ile güvenli serileştirme)
                        val patchPortResp = client.patch("$restUrl/portfolios?id=eq.${remotePortfolio.id}") {
                            contentType(ContentType.Application.Json)
                            header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                            header("Authorization", config.getAuthHeader())
                            setBody(SupabaseBalancePatchDto(balance = newBalance))
                        }
                        if (patchPortResp.status.value !in 200..299) {
                            Log.e("ApiGameRepository", "sell balance patch failed: ${patchPortResp.status} - ${patchPortResp.bodyAsText()}")
                        }

                        // 2. Portföy kalemini güncelle ya da sil (SupabaseQuantityPatchDto ile güvenli serileştirme)
                        var itemUpdated = false
                        val remaining = existingItem.quantity - quantity
                        if (remaining <= 0) {
                            val delResp = client.delete("$restUrl/portfolio_items?id=eq.${existingItem.id}") {
                                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                header("Authorization", config.getAuthHeader())
                            }
                            if (delResp.status.value in 200..299) {
                                itemUpdated = true
                            } else {
                                val err = delResp.bodyAsText()
                                Log.e("ApiGameRepository", "portfolio_item delete failed: ${delResp.status} - $err")
                                lastDbError = "Portföy kalemi silinemedi (${delResp.status.value}): $err"
                            }
                        } else {
                            val patchItemResp = client.patch("$restUrl/portfolio_items?id=eq.${existingItem.id}") {
                                contentType(ContentType.Application.Json)
                                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                header("Authorization", config.getAuthHeader())
                                setBody(SupabaseQuantityPatchDto(quantity = remaining))
                            }
                            if (patchItemResp.status.value in 200..299) {
                                itemUpdated = true
                            } else {
                                val err = patchItemResp.bodyAsText()
                                Log.e("ApiGameRepository", "portfolio_item quantity patch failed: ${patchItemResp.status} - $err")
                                lastDbError = "Portföy kalemi güncellenemedi (${patchItemResp.status.value}): $err"
                            }
                        }

                        if (!itemUpdated) {
                            // Bakiye artışını geri al (Rollback)
                            try {
                                client.patch("$restUrl/portfolios?id=eq.${remotePortfolio.id}") {
                                    contentType(ContentType.Application.Json)
                                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                    header("Authorization", config.getAuthHeader())
                                    setBody(SupabaseBalancePatchDto(balance = remotePortfolio.balance))
                                }
                            } catch (revertEx: Exception) {
                                Log.e("ApiGameRepository", "Sell rollback failed: ${revertEx.message}")
                            }
                            return Result.failure(Exception("Satış işlemi portföye uygulanamadı! $lastDbError"))
                        }

                        // 3. Kullanıcıya özel işlem geçmişine ekle (trade_history)
                        try {
                            val histResp = client.post("$restUrl/trade_history") {
                                contentType(ContentType.Application.Json)
                                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                header("Authorization", config.getAuthHeader())
                                setBody(SupabaseTradeHistoryInsertDto(
                                    userId = userId,
                                    stockSymbol = cleanSymbol,
                                    type = "SELL",
                                    quantity = quantity,
                                    price = price,
                                    commission = commission,
                                    totalAmount = netGain,
                                    timestamp = timestamp
                                ))
                            }
                            if (histResp.status.value !in 200..299) {
                                client.post("$restUrl/trade_history") {
                                    contentType(ContentType.Application.Json)
                                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                    header("Authorization", config.getAuthHeader())
                                    setBody(SupabaseTradeHistoryInsertDto(
                                        userId = userId,
                                        stockSymbol = "$cleanSymbol.IS",
                                        type = "SELL",
                                        quantity = quantity,
                                        price = price,
                                        commission = commission,
                                        totalAmount = netGain,
                                        timestamp = timestamp
                                    ))
                                }
                            }
                        } catch (histEx: Exception) {
                            Log.e("ApiGameRepository", "Error inserting sell trade_history: ${histEx.message}")
                        }

                        databaseSaved = true
                        Log.d("ApiGameRepository", "sellStock direct database sync succeeded for user $userId, symbol $cleanSymbol")
                    } else {
                        lastDbError = "Portföy kaydı bulunamadı."
                    }
                } else {
                    lastDbError = portResp.bodyAsText()
                }
            } catch (e: Exception) {
                lastDbError = e.message
                Log.e("ApiGameRepository", "sellStock direct database fallback error: ${e.message}", e)
            }
        }

        if (!databaseSaved) {
            return Result.failure(
                Exception("Satış işlemi veritabanına kaydedilemedi! Lütfen Supabase SQL ayarlarını ve internet bağlantınızı kontrol ediniz. (Hata: ${lastDbError ?: "Bağlantı hatası"})")
            )
        }

        // 2. Yerel Kontrol ve Atomik Güncelleme (Cache & Anlık UI Yansıması)
        if (existingIndex < 0 || currentItems[existingIndex].quantity < quantity) {
            val owned = if (existingIndex >= 0) currentItems[existingIndex].quantity else 0L
            return Result.failure(Exception("Yetersiz hisse adedi! Portföyünüzde $owned lot $cleanSymbol bulunmaktadır."))
        }

        val existing = currentItems[existingIndex]
        val remainingQty = existing.quantity - quantity
        if (remainingQty <= 0) {
            currentItems.removeAt(existingIndex)
        } else {
            currentItems[existingIndex] = existing.copy(quantity = remainingQty)
        }

        val newBalance = currentPortfolio.balance + netGain
        val updatedPortfolio = Portfolio(
            id = currentPortfolio.id,
            balance = newBalance,
            items = currentItems
        )
        saveLocalPortfolio(userKey, updatedPortfolio)
        saveLocalHistoryItem(
            userKey,
            LocalTradeHistoryItem(
                id = System.currentTimeMillis(),
                stockSymbol = cleanSymbol,
                type = "SELL",
                quantity = quantity,
                price = price,
                commission = commission,
                totalAmount = netGain,
                timestamp = timestamp
            )
        )

        // 3. Veritabanı senkronize edildiyse güncel portföyü çek
        val freshRes = getPortfolio()
        if (freshRes.isSuccess) {
            return freshRes
        }

        return Result.success(updatedPortfolio)
    }
}
