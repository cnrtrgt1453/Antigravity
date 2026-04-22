package com.antigravity.mobile.presentation.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.antigravity.mobile.domain.model.OHLCData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun StockChart(
    ohlcData: OHLCData,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .height(300.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFFF6C90E))
        }
    } else {
        val jsonOhlc = remember(ohlcData) { Json.encodeToString(ohlcData.ohlc) }
        val jsonSma50 = remember(ohlcData) { Json.encodeToString(ohlcData.sma50) }
        val jsonSma200 = remember(ohlcData) { Json.encodeToString(ohlcData.sma200) }
        val jsonMarkers = remember(ohlcData) { Json.encodeToString(ohlcData.markers) }

        val chartHtml = """
            <!DOCTYPE html>
            <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <script src="https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js"></script>
                    <style>
                        body { margin: 0; padding: 0; background-color: #0D1117; overflow: hidden; }
                        #chart { width: 100vw; height: 100vh; }
                    </style>
                </head>
                <body>
                    <div id="chart"></div>
                    <script>
                        const chartOptions = { 
                            layout: { 
                                background: { color: '#0D1117' }, 
                                textColor: '#D1D4DC',
                            },
                            grid: {
                                vertLines: { color: '#1f2937' },
                                horzLines: { color: '#1f2937' },
                            },
                            crosshair: {
                                mode: LightweightCharts.CrosshairMode.Normal,
                            },
                            rightPriceScale: {
                                borderColor: '#1f2937',
                            },
                            timeScale: {
                                borderColor: '#1f2937',
                                timeVisible: true,
                            },
                        };
                        
                        const chart = LightweightCharts.createChart(document.getElementById('chart'), chartOptions);
                        const candlestickSeries = chart.addCandlestickSeries({
                            upColor: '#26a69a', downColor: '#ef5350', borderVisible: false,
                            wickUpColor: '#26a69a', wickDownColor: '#ef5350',
                        });
                        
                        const sma50Series = chart.addLineSeries({ color: '#4ade80', lineWidth: 2 });
                        const sma200Series = chart.addLineSeries({ color: '#ef4444', lineWidth: 2 });
                        
                        candlestickSeries.setData($jsonOhlc);
                        sma50Series.setData($jsonSma50);
                        sma200Series.setData($jsonSma200);
                        candlestickSeries.setMarkers($jsonMarkers);
                        
                        chart.timeScale().fitContent();
                        
                        window.onresize = () => {
                            chart.applyOptions({ width: window.innerWidth, height: window.innerHeight });
                        };
                    </script>
                </body>
            </html>
        """.trimIndent()

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    setBackgroundColor(0xFF0D1117.toInt())
                    loadDataWithBaseURL(null, chartHtml, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, chartHtml, "text/html", "UTF-8", null)
            },
            modifier = modifier.height(300.dp).fillMaxSize()
        )
    }
}
