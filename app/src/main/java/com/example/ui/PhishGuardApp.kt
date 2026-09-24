package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuthDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.viewmodel.PhishGuardViewModel

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    SCANNER("Scanner", Icons.Default.Security, "tab_scanner"),
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    HISTORY("History", Icons.Default.History, "tab_history"),
    ADMIN("Admin", Icons.Default.AdminPanelSettings, "tab_admin"),
    PROFILE("Profile", Icons.Default.Person, "tab_profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhishGuardApp(
    viewModel: PhishGuardViewModel
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isAuthOpen by viewModel.isAuthDialogOpen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    if (isAuthOpen) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setAuthDialogOpen(false) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CyberPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PHISHGUARD",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Cloud status indicator
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Connected",
                            tint = CyberSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cloud",
                            fontSize = 10.sp,
                            color = CyberSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // User avatar button
                    IconButton(
                        onClick = { viewModel.setAuthDialogOpen(true) },
                        modifier = Modifier.testTag("top_user_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentUser?.role == "ADMIN") CyberPrimary.copy(alpha = 0.2f)
                                    else CyberSurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (currentUser?.role == "ADMIN") CyberPrimary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile",
                                tint = if (currentUser?.role == "ADMIN") CyberPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(imageVector = tab.icon, contentDescription = tab.label)
                        },
                        label = {
                            Text(text = tab.label, fontSize = 10.sp)
                        },
                        modifier = Modifier.testTag(tab.tag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPrimary,
                            selectedTextColor = CyberPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = CyberPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> ScannerScreen(viewModel = viewModel, modifier = contentModifier)
            1 -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToScan = { selectedTab = 0 },
                modifier = contentModifier
            )
            2 -> HistoryScreen(
                viewModel = viewModel,
                onSelectScan = { url ->
                    viewModel.onUrlChanged(url)
                    viewModel.scanUrl(url)
                    selectedTab = 0
                },
                modifier = contentModifier
            )
            3 -> AdminScreen(viewModel = viewModel, modifier = contentModifier)
            4 -> ProfileScreen(viewModel = viewModel, modifier = contentModifier)
        }
    }
}
