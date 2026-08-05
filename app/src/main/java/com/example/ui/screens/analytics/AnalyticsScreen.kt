package com.example.ui.screens.analytics

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ExpiringMembershipInfo
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

@Composable
fun AnalyticsScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val expiringList by viewModel.expiringMemberships.collectAsState()
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize().background(GymBgLight)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Аналитика и сводка зала",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GymTextPrimary
                )
            }

            // 1. Dashboard Stat Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Выручка за сегодня",
                            value = "${stats.todayRevenue.toInt()} ₽",
                            icon = Icons.Default.Payments,
                            tint = GymPrimaryIndigo,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Визиты сегодня",
                            value = "${stats.todayVisits}",
                            icon = Icons.Default.CheckCircle,
                            tint = GymGreenSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Продажи товаров",
                            value = "${stats.todaySalesCount} шт.",
                            icon = Icons.Default.ShoppingBag,
                            tint = GymAmberAlert,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Активные абонементы",
                            value = "${stats.activeMemberships}",
                            icon = Icons.Default.CardMembership,
                            tint = GymPrimaryIndigo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Revenue Payment Method Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "РАСПРЕДЕЛЕНИЕ ВЫРУЧКИ ПО ОПЛАТЕ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = GymTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val total = (stats.cashRevenue + stats.cardRevenue).coerceAtLeast(1.0)
                        val cashPct = (stats.cashRevenue / total).toFloat()
                        val cardPct = (stats.cardRevenue / total).toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymSurfaceVariant)
                        ) {
                            if (cashPct > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(cashPct.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(GymGreenSuccess)
                                )
                            }
                            if (cardPct > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(cardPct.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(GymPrimaryIndigo)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(GymGreenSuccess))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Наличные: ${stats.cashRevenue.toInt()} ₽", style = MaterialTheme.typography.bodyMedium, color = GymTextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(GymPrimaryIndigo))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Карта: ${stats.cardRevenue.toInt()} ₽", style = MaterialTheme.typography.bodyMedium, color = GymTextPrimary)
                            }
                        }
                    }
                }
            }

            // 3. Expiring Memberships Section ("Истекающие абонементы (7 дней)")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Истекающие абонементы (${expiringList.size})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = GymTextPrimary
                    )

                    Surface(color = GymAmberAlert.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "Обзвон клиентов",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymAmberAlert,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (expiringList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("Нет клиентов с заканчивающимся абонементом", color = GymTextSecondary)
                        }
                    }
                }
            } else {
                items(expiringList) { expiring ->
                    ExpiringClientCard(
                        info = expiring,
                        onCallClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${expiring.phone}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = GymTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GymTextPrimary)
        }
    }
}

@Composable
fun ExpiringClientCard(
    info: ExpiringMembershipInfo,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymSurfaceWhite),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = GymAmberAlert, modifier = Modifier.size(24.dp))

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(info.clientName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GymTextPrimary)
                Text(info.tariffName, style = MaterialTheme.typography.bodySmall, color = GymPrimaryIndigo, fontWeight = FontWeight.Bold)

                val reason = if (info.visitsLeft != null) {
                    "Осталось визитов: ${info.visitsLeft}"
                } else {
                    "Дней осталось: ${info.daysLeft ?: 0} (до ${info.expiresAt})"
                }
                Text(reason, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = GymAmberAlert)
            }

            Button(
                onClick = onCallClick,
                colors = ButtonDefaults.buttonColors(containerColor = GymPrimaryIndigo),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Позвонить", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
