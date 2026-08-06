package com.example.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.clients.ClientDetailScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.journal.JournalScreen
import com.example.ui.screens.products.ProductsScreen
import com.example.ui.screens.tariffs.TariffsScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel
import kotlinx.coroutines.flow.collectLatest

enum class NavDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CLIENTS("Клиенты", Icons.Default.People),
    PRODUCTS("Товары", Icons.Default.Storefront),
    JOURNAL("Журнал", Icons.Default.ReceiptLong),
    ANALYTICS("Аналитика", Icons.Default.Assessment),
    TARIFFS("Тарифы", Icons.Default.Settings)
}

@Composable
fun AppNavigation(viewModel: GymViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val context = LocalContext.current

    // Observe UI messages for Toast feedback
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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

    val selectedClient by viewModel.selectedClient.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavDestination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = currentDestination == dest,
                            onClick = {
                                currentDestination = dest
                                phoneShowDetailScreen = false
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
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
                .padding(bottom = innerPadding.calculateBottomPadding())
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
