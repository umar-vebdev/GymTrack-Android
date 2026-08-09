package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.clients.ClientDetailScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.journal.JournalScreen
import com.example.ui.screens.products.ProductsScreen
import com.example.ui.screens.tariffs.TariffsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.components.LocalSettingsAction
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

enum class NavDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CLIENTS("Клиенты", Icons.Default.People),
    PRODUCTS("Товары", Icons.Default.Storefront),
    JOURNAL("Журнал", Icons.Default.ReceiptLong),
    TARIFFS("Тарифы", Icons.Default.CardMembership),
    ANALYTICS("Аналитика", Icons.Default.Assessment)
}

@Composable
fun AppNavigation(viewModel: GymViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    // Custom Top Snackbar feedback
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            toastMessage = msg
            delay(3000)
            toastMessage = null
        }
    }

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var currentDestination by remember { mutableStateOf(NavDestination.CLIENTS) }
    var phoneShowDetailScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    val selectedClient by viewModel.selectedClient.collectAsState()

    // 1. Intercept system back for Settings screen
    androidx.activity.compose.BackHandler(enabled = showSettingsScreen) {
        showSettingsScreen = false
    }

    // 2. Intercept system back for Phone Client Detail screen
    androidx.activity.compose.BackHandler(
        enabled = !showSettingsScreen && !isTablet && currentDestination == NavDestination.CLIENTS && phoneShowDetailScreen
    ) {
        phoneShowDetailScreen = false
    }

    // 3. Intercept system back to return to main tab (CLIENTS) if on another tab
    androidx.activity.compose.BackHandler(
        enabled = !showSettingsScreen && (!phoneShowDetailScreen || isTablet) && currentDestination != NavDestination.CLIENTS
    ) {
        currentDestination = NavDestination.CLIENTS
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // Navigation / Content System
        if (showSettingsScreen) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { showSettingsScreen = false }
            )
        } else {
            CompositionLocalProvider(LocalSettingsAction provides { showSettingsScreen = true }) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (!isTablet) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                NavDestination.entries.forEach { dest ->
                                    NavigationBarItem(
                                        selected = currentDestination == dest,
                                        onClick = {
                                            currentDestination = dest
                                            phoneShowDetailScreen = false
                                        },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { 
                                            Text(
                                                text = dest.label,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                                                style = MaterialTheme.typography.labelSmall
                                            ) 
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = GymPrimaryIndigo,
                                            selectedTextColor = GymPrimaryIndigo,
                                            indicatorColor = GymIndigoContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Tablet Left Navigation Rail
                        if (isTablet) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = GymPrimaryIndigo,
                                header = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = GymPrimaryIndigo,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "GymTrack",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                modifier = Modifier.width(88.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                NavDestination.entries.forEach { dest ->
                                    NavigationRailItem(
                                        selected = currentDestination == dest,
                                        onClick = { currentDestination = dest },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { Text(dest.label, fontSize = 11.sp) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = GymPrimaryIndigo,
                                            selectedTextColor = GymPrimaryIndigo,
                                            indicatorColor = GymIndigoContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
            
                        // Main Content Area
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            when (currentDestination) {
                                NavDestination.CLIENTS -> {
                                    if (isTablet) {
                                        // Split Master-Detail Layout for Tablet
                                        Row(modifier = Modifier.fillMaxSize()) {
                                            ClientsScreen(
                                                viewModel = viewModel,
                                                onClientSelected = { /* Already handled by ViewModel selectedClientId */ },
                                                modifier = Modifier.weight(0.42f)
                                            )
            
                                            VerticalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            
                                            ClientDetailScreen(
                                                viewModel = viewModel,
                                                client = selectedClient,
                                                onBackClick = null,
                                                modifier = Modifier.weight(0.58f)
                                            )
                                        }
                                    } else {
                                        // Phone Stack View
                                        if (phoneShowDetailScreen) {
                                            ClientDetailScreen(
                                                viewModel = viewModel,
                                                client = selectedClient,
                                                onBackClick = { phoneShowDetailScreen = false }
                                            )
                                        } else {
                                            ClientsScreen(
                                                viewModel = viewModel,
                                                onClientSelected = {
                                                    phoneShowDetailScreen = true
                                                }
                                            )
                                        }
                                    }
                                }
                                NavDestination.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                                NavDestination.JOURNAL -> JournalScreen(viewModel = viewModel)
                                NavDestination.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                                NavDestination.TARIFFS -> TariffsScreen(
                                    viewModel = viewModel,
                                    onBackClick = { currentDestination = NavDestination.CLIENTS }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Custom Top Snackbar Overlay
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp)
                .padding(horizontal = 16.dp)
                .zIndex(100f)
        ) {
            toastMessage?.let { msg ->
                val lowerMsg = msg.lowercase()
                val isError = lowerMsg.contains("ошибка") || lowerMsg.contains("failed") || lowerMsg.contains("заполните") || lowerMsg.contains("введите") || lowerMsg.contains("истек") || lowerMsg.contains("закончились")
                val isSuccess = lowerMsg.contains("успешн") || lowerMsg.contains("создан") || lowerMsg.contains("удален") || lowerMsg.contains("оформлен") || lowerMsg.contains("добавлен") || lowerMsg.contains("зафиксирована") || lowerMsg.contains("отменен") || lowerMsg.contains("добро")
                
                val bgColor = when {
                    isError -> GymRoseAlert
                    isSuccess -> GymGreenSuccess
                    else -> MaterialTheme.colorScheme.inverseSurface
                }
                val textColor = when {
                    isError || isSuccess -> Color.White
                    else -> MaterialTheme.colorScheme.inverseOnSurface
                }
                val icon = when {
                    isError -> Icons.Default.ErrorOutline
                    isSuccess -> Icons.Default.CheckCircle
                    else -> Icons.Default.Info
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = msg.replace("SUCCESS|", "").replace("ERROR|", "").replace("INFO|", ""), color = textColor, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
        
    }
}
