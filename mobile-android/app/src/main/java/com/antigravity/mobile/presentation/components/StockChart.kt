package com.antigravity.mobile.presentation.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StockChart(
    symbol: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val cleanSymbol = remember(symbol) {
        symbol.split(".")[0].uppercase()
    }

    if (isLoading) {
        Box(
            modifier = modifier
                .height(380.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFFF6C90E))
        }
    } else {
        val chartHtml = remember(cleanSymbol) {
            """
            <!DOCTYPE html>
            <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        html, body { width: 100%; height: 100%; background-color: #0D1117; overflow: hidden; }
                        .tradingview-widget-container { width: 100%; height: 100%; }
                        #tradingview_chart { width: 100%; height: 100%; }
                    </style>
                </head>
                <body>
                    <div class="tradingview-widget-container">
                        <div id="tradingview_chart"></div>
                        <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                        <script type="text/javascript">
                            new TradingView.widget({
                                "autosize": true,
                                "symbol": "BIST:$cleanSymbol",
                                "interval": "D",
                                "timezone": "Europe/Istanbul",
                                "theme": "dark",
                                "style": "1",
                                "locale": "tr",
                                "toolbar_bg": "#0D1117",
                                "enable_publishing": false,
                                "allow_symbol_change": false,
                                "hide_side_toolbar": true,
                                "hide_top_toolbar": false,
                                "save_image": false,
                                "container_id": "tradingview_chart"
                            });
                        </script>
                    </div>
                </body>
            </html>
            """.trimIndent()
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                    setBackgroundColor(0xFF0D1117.toInt())
                    tag = cleanSymbol
                    loadDataWithBaseURL("https://www.tradingview.com", chartHtml, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                if (webView.tag != cleanSymbol) {
                    webView.tag = cleanSymbol
                    webView.loadDataWithBaseURL("https://www.tradingview.com", chartHtml, "text/html", "UTF-8", null)
                }
            },
            modifier = modifier.height(380.dp).fillMaxSize()
        )
    }
}
