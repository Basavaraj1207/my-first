package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanEntity
import com.example.ui.components.StatCard
import com.example.ui.components.VerdictBadge
import com.example.ui.theme.CyberAlert
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberWarning
import com.example.ui.viewmodel.PhishGuardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: PhishGuardViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scans by viewModel.scans.collectAsState()

    val totalScans = scans.size
    val phishingCount = scans.count { it.verdict.equals("PHISHING", ignoreCase = true) }
    val suspiciousCount = scans.count { it.verdict.equals("SUSPICIOUS", ignoreCase = true) }
    val legitimateCount = scans.count { it.verdict.equals("LEGITIMATE", ignoreCase = true) }

    val avgRisk = if (totalScans > 0) scans.sumOf { it.riskScore } / totalScans else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DataExploration,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Security Telemetry Dashboard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time AI Risk Analysis & Threat Distribution",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4 KPI Cards in 2x2 grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Scans",
                        value = totalScans.toString(),
                        icon = Icons.Default.Shield,
                        accentColor = CyberPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Phishing Detected",
                        value = phishingCount.toString(),
                        icon = Icons.Default.Dangerous,
                        accentColor = CyberAlert,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Suspicious URLs",
                        value = suspiciousCount.toString(),
                        icon = Icons.Default.Warning,
                        accentColor = CyberWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Legitimate URLs",
                        value = legitimateCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = CyberSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Risk Ratio / Threat Spectrum Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "THREAT SPECTRUM BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Average Risk Index", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "$avgRisk / 100",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (avgRisk > 50) CyberAlert else CyberSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (avgRisk / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (avgRisk > 50) CyberAlert else CyberSecondary,
                        trackColor = CyberSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Proportions
                    if (totalScans > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Safe: ${(legitimateCount * 100) / totalScans}%",
                                fontSize = 11.sp,
                                color = CyberSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Suspicious: ${(suspiciousCount * 100) / totalScans}%",
                                fontSize = 11.sp,
                                color = CyberWarning,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Phishing: ${(phishingCount * 100) / totalScans}%",
                                fontSize = 11.sp,
                                color = CyberAlert,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Recent Scans Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT SCANS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tap to Rescan",
                    fontSize = 11.sp,
                    color = CyberPrimary,
                    modifier = Modifier.clickable { onNavigateToScan() }
                )
            }
        }

        if (scans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No scans performed yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(scans.take(5)) { scan ->
                RecentScanItem(
                    scan = scan,
                    onClick = {
                        viewModel.onUrlChanged(scan.url)
                        viewModel.scanUrl(scan.url)
                        onNavigateToScan()
                    }
                )
            }
        }
    }
}

@Composable
fun RecentScanItem(
    scan: ScanEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        .format(Date(scan.scannedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.url,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr • Risk: ${scan.riskScore}%",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            VerdictBadge(verdict = scan.verdict)
        }
    }
}
