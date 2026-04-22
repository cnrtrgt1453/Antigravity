package com.antigravity.mobile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut || uiState.isDeleted) {
        if (uiState.isLoggedOut || uiState.isDeleted) {
            onLogout()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Profile Header
        ProfileHeader(
            fullName = uiState.user?.fullName ?: "Kullanıcı",
            email = uiState.user?.email ?: ""
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Statistics
        Text(
            text = "İstatistikler",
            color = Color(0xFF8B949E),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), value = "0", label = "Takip")
            StatCard(modifier = Modifier.weight(1f), value = uiState.tradeCount.toString(), label = "İşlem")
            StatCard(modifier = Modifier.weight(1f), value = (uiState.portfolio?.items?.size ?: 0).toString(), label = "Portföy")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Balance Card
        BalanceCard(balance = uiState.portfolio?.balance?.toString() ?: "0.00")

        Spacer(modifier = Modifier.height(28.dp))

        // App Info Section
        Text(
            text = "Uygulama",
            color = Color(0xFF8B949E),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161B22))
                .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
        ) {
            InfoRow(icon = Icons.Outlined.Info, label = "FinanceUp", value = "v1.0.0")
            Divider(color = Color(0xFF30363D))
            InfoRow(icon = Icons.Outlined.TrendingUp, label = "Yöntem", value = "Golden / Dead Cross")
            Divider(color = Color(0xFF30363D))
            InfoRow(
                icon = Icons.Outlined.ErrorOutline,
                label = "Sinyaller yatırım tavsiyesi değildir.",
                value = "",
                labelColor = Color(0xFFC9D1D9)
            )
            Divider(color = Color(0xFF30363D))
            InfoRow(
                icon = Icons.Outlined.Description,
                label = "Gizlilik Politikası",
                value = "",
                labelColor = Color(0xFF58A6FF),
                showChevron = true,
                onClick = onNavigateToPrivacy
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Logout Button
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF85149).copy(alpha = 0.1f),
                contentColor = Color(0xFFF85149)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF85149).copy(alpha = 0.3f))
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Çıkış Yap", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Hesabımı Sil",
            color = Color(0xFF8B949E),
            fontSize = 14.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDeleteDialog = true }
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap") },
            text = { Text("Hesabınızdan çıkmak istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout() }) {
                    Text("Çıkış Yap", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("HESABI SİL") },
            text = { Text("Tüm verileriniz kalıcı olarak silinecektir. Bu işlem geri alınamaz! Emin misiniz?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAccount() }) {
                    Text("EVET, SİL", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun ProfileHeader(fullName: String, email: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val initials = if (fullName.contains(" ")) {
            fullName.split(" ").take(2).map { it.first() }.joinToString("").uppercase()
        } else {
            fullName.take(2).uppercase()
        }

        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color(0xFF58A6FF).copy(alpha = 0.1f))
                .border(2.dp, Color(0xFF58A6FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color(0xFF58A6FF),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(fullName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(email, color = Color(0xFF8B949E), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            color = Color(0xFFF6C90E).copy(alpha = 0.1f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF6C90E).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFF6C90E), modifier = Modifier.size(12.dp))
                Text("Üye", color = Color(0xFFF6C90E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .background(Color(0xFF161B22), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Color(0xFF8B949E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BalanceCard(balance: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161B22), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFF6C90E), modifier = Modifier.size(18.dp))
            Text("Oyun Bakiyesi", color = Color(0xFF8B949E), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("$balance TL", color = Color(0xFFF6C90E), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    labelColor: Color = Color(0xFFC9D1D9),
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF8B949E), modifier = Modifier.size(18.dp))
        Text(label, color = labelColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, color = Color(0xFF8B949E), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        if (showChevron) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8B949E), modifier = Modifier.size(16.dp))
        }
    }
}
