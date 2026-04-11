package com.example.fuelrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fuelrecord.viewmodel.FuelRecordViewModel
import com.example.fuelrecord.viewmodel.MonthlyStats
import com.example.fuelrecord.viewmodel.OverallStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FuelRecordViewModel,
    onNavigateBack: () -> Unit
) {
    val overallStats by viewModel.overallStats.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OverallStatsCard(overallStats)
            }

            if (monthlyStats.isNotEmpty()) {
                item {
                    Text(
                        "月度统计",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    ConsumptionTrendCard(monthlyStats)
                }

                items(monthlyStats) { stats ->
                    MonthlyStatsCard(stats)
                }
            }
        }
    }
}

@Composable
private fun OverallStatsCard(stats: OverallStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "总体统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (stats.recordCount == 0) {
                Text(
                    "暂无数据",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatsGridItem(
                        "加油次数",
                        "${stats.recordCount} 次",
                        Modifier.weight(1f)
                    )
                    StatsGridItem(
                        "总里程",
                        String.format("%.0f km", stats.totalDistance),
                        Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatsGridItem(
                        "总加油",
                        String.format("%.1f L", stats.totalFuel),
                        Modifier.weight(1f)
                    )
                    StatsGridItem(
                        "总花费",
                        String.format("¥%.2f", stats.totalCost),
                        Modifier.weight(1f)
                    )
                }
                stats.avgConsumption?.let { avg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatsGridItem(
                            "平均油耗",
                            String.format("%.1f L/100km", avg),
                            Modifier.weight(1f)
                        )
                        val avgCostPerKm = if (stats.totalDistance > 0)
                            stats.totalCost / stats.totalDistance else null
                        StatsGridItem(
                            "平均成本",
                            avgCostPerKm?.let { String.format("¥%.2f/km", it) } ?: "--",
                            Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGridItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConsumptionTrendCard(monthlyStats: List<MonthlyStats>) {
    val statsWithConsumption = monthlyStats
        .filter { it.avgConsumption != null }
        .takeLast(12)
        .reversed()

    if (statsWithConsumption.isEmpty()) return

    val maxConsumption = statsWithConsumption.maxOf { it.avgConsumption!! }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "油耗趋势 (L/100km)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            statsWithConsumption.forEach { stats ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stats.yearMonth.substring(2),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(48.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                    ) {
                        val fraction = (stats.avgConsumption!! / maxConsumption)
                            .toFloat()
                            .coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        String.format("%.1f", stats.avgConsumption),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatsCard(stats: MonthlyStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stats.yearMonth,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    String.format("加油 %.1fL", stats.totalFuel),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    String.format("花费 ¥%.2f", stats.totalCost),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stats.avgConsumption?.let {
                    Text(
                        String.format("油耗 %.1f L/100km", it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } ?: Text(
                    "油耗 --",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${stats.recordCount} 次加油",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
