package com.antigravity.mobile.presentation.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gizlilik Politikası", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D1117)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Son Güncelleme: 3 Nisan 2024",
                color = Color(0xFF8B949E),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PrivacySection(title = "1. Giriş", content = "FinanceUp (\"biz\", \"tarafımız\" veya \"uygulama\"), kullanıcılarımızın gizliliğine önem vermektedir. Bu Gizlilik Politikası, uygulamamızı kullandığınızda bilgilerinizin nasıl toplandığını, kullanıldığını ve korunduğunu açıklar.")
            
            PrivacySection(title = "2. Toplanan Bilgiler", content = "Uygulamamızı kullandığınızda aşağıdaki bilgileri toplayabiliriz:\n• Hesap Bilgileri: Adınız, e-posta adresiniz ve profil resminiz (Sosyal giriş yapıldığında).\n• Uygulama Verileri: İzleme listeniz, portföy hareketleriniz ve işlem geçmişiniz.\n• Cihaz Bilgileri: İşletim sistemi sürümü ve cihaz modeli (Hata ayıklama ve performans analizi için).")
            
            PrivacySection(title = "3. Bilgilerin Kullanımı", content = "Topladığımız veriler şu amaçlarla kullanılır:\n• Uygulama özelliklerini ve yatırım simülasyonunu sürdürmek.\n• Size özel analiz raporları sunmak.\n• Güvenliği sağlamak ve dolandırıcılığı önlemek.\n• Kullanıcı deneyimini iyileştirmek.")
            
            PrivacySection(title = "4. Veri Paylaşımı", content = "Verileriniz üçüncü şahıslara satılmaz. Ancak yasal zorunluluklar veya bulut hizmet sağlayıcılarımız (Firebase, Google Cloud vb.) ile operasyonel amaçlarla sınırlı bilgi paylaşımı yapılabilir.")
            
            PrivacySection(title = "5. Veri Saklama ve Silme", content = "Verileriniz, hesabınız aktif olduğu sürece saklanır. İstediğiniz zaman uygulama içindeki \"Hesabımı Sil\" seçeneğini kullanarak tüm verilerinizin kalıcı olarak silinmesini talep edebilirsiniz.")
            
            PrivacySection(title = "6. Güvenlik", content = "Verilerinizi korumak için endüstri standardı güvenlik önlemleri almaktayız. Ancak internet üzerinden yapılan hiçbir iletimin %100 güvenli olmadığını belirtmek isteriz.")
            
            PrivacySection(title = "7. İletişim", content = "Bu politika ile ilgili sorularınız için bizimle iletişime geçebilirsiniz.")

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = content,
            color = Color(0xFFC9D1D9),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}
