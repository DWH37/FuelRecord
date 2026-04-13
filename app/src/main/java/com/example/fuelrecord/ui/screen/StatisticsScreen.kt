package com.example.fuelrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fuelrecord.viewmodel.FuelRecordViewModel
import com.example.fuelrecord.viewmodel.MonthlyStats
import com.example.fuelrecord.viewmodel.OverallStats
import com.example.fuelrecord.viewmodel.YearlyStats

private val YearColors = listOf(
    Color(0xFF1976D2),
    Color(0xFFF57C00),
    Color(0xFF388E3C),
    Color(0xFFD32F2F),
    Color(0xFF7B1FA2),
    Color(0xFF00796B),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FuelRecordViewModel,
    onNavigateBack: () -> Unit
) {
    val overallStats by viewModel.overallStats.collectAsState()
    val yearlyStats by viewModel.yearlyStats.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("总览", "年度对比", "月度明细")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab(overallStats, yearlyStats, monthlyStats)
                1 -> YearCompareTab(yearlyStats, monthlyStats, availableYears)
                2 -> MonthDetailTab(monthlyStats, availableYears)
            }
        }
    }
}

// ==================== Tab 0: 总览 ====================

@Composable
private fun OverviewTab(
    overallStats: OverallStats,
    yearlyStats: List<YearlyStats>,
    monthlyStats: List<MonthlyStats>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { OverallStatsCard(overallStats) }

        if (monthlyStats.any { it.avgConsumption != null }) {
            item { ConsumptionTrendCard(monthlyStats) }
        }

        if (yearlyStats.size > 1) {
            item { YearlyCostCompareCard(yearlyStats) }
        }
    }
}

// ==================== Tab 1: 年度对比 ====================

@Composable
private fun YearCompareTab(
    yearlyStats: List<YearlyStats>,
    monthlyStats: List<MonthlyStats>,
    availableYears: List<Int>
) {
    if (yearlyStats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(yearlyStats) { stats -> YearlyStatsCard(stats) }

        if (availableYears.size > 1) {
            item {
                Text(
                    "各年同月油耗对比 (L/100km)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item { CrossYearMonthChart(monthlyStats, availableYears) }

            item {
                Text(
                    "各年同月花费对比 (¥)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item { CrossYearMonthCostChart(monthlyStats, availableYears) }
        }
    }
}

// ==================== Tab 2: 月度明细 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDetailTab(
    monthlyStats: List<MonthlyStats>,
    availableYears: List<Int>
) {
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    val filtered = if (selectedYear == null) monthlyStats
    else monthlyStats.filter { it.year == selectedYear }

    Column {
        // 年份筛选条
        if (availableYears.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedYear == null,
                    onClick = { selectedYear = null },
                    label = { Text("全部") }
                )
                availableYears.forEach { year ->
                    FilterChip(
                        selected = selectedYear == year,
                        onClick = { selectedYear = if (selectedYear == year) null else year },
                        label = { Text("${year}年") }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.yearMonth }) { stats ->
                    MonthlyStatsCard(stats)
                }
            }
        }
    }
}

// ==================== 共享卡片组件 ====================

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
                Text("暂无数据", color = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatsGridItem("加油次数", "${stats.recordCount} 次", Modifier.weight(1f))
                    StatsGridItem("总里程", String.format("%.0f km", stats.totalDistance), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatsGridItem("总加油", String.format("%.1f L", stats.totalFuel), Modifier.weight(1f))
                    StatsGridItem("总花费", String.format("¥%.2f", stats.totalCost), Modifier.weight(1f))
                }
                stats.avgConsumption?.let { avg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatsGridItem("平均油耗", String.format("%.1f L/100km", avg), Modifier.weight(1f))
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
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ConsumptionTrendCard(monthlyStats: List<MonthlyStats>) {
    val statsWithConsumption = monthlyStats
        .filter { it.avgConsumption != null }
        .sortedByDescending { it.yearMonth }

    if (statsWithConsumption.isEmpty()) return

    val maxConsumption = statsWithConsumption.maxOf { it.avgConsumption!! }
    val scrollState = rememberScrollState()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("油耗趋势 (L/100km)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(scrollState)
            ) {
                statsWithConsumption.forEach { stats ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stats.yearMonth.substring(2),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(48.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(20.dp)) {
                            val fraction = (stats.avgConsumption!! / maxConsumption).toFloat().coerceIn(0.05f, 1f)
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
}

// ==================== 年度花费对比卡片 ====================

@Composable
private fun YearlyCostCompareCard(yearlyStats: List<YearlyStats>) {
    val maxCost = yearlyStats.maxOf { it.totalCost }
    if (maxCost <= 0) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("年度花费对比", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            yearlyStats.forEachIndexed { index, stats ->
                val color = YearColors[index % YearColors.size]
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${stats.year}年",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(48.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(22.dp)) {
                        val fraction = (stats.totalCost / maxCost).toFloat().coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        String.format("¥%.0f", stats.totalCost),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(56.dp)
                    )
                }
            }
        }
    }
}

// ==================== 跨年同月对比图 ====================

@Composable
private fun CrossYearMonthChart(
    monthlyStats: List<MonthlyStats>,
    availableYears: List<Int>
) {
    val years = availableYears.sortedDescending()
    val dataByYearMonth = monthlyStats.associateBy { Pair(it.year, it.month) }
    val allValues = (1..12).flatMap { m ->
        years.mapNotNull { y -> dataByYearMonth[Pair(y, m)]?.avgConsumption }
    }
    val maxVal = allValues.maxOrNull() ?: return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                years.forEachIndexed { index, year ->
                    val color = YearColors[index % YearColors.size]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(12.dp).height(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${year}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 1-12月分组柱状图
            (1..12).forEach { month ->
                val hasData = years.any { y -> dataByYearMonth[Pair(y, month)]?.avgConsumption != null }
                if (!hasData) return@forEach

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${month}月",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        years.forEachIndexed { index, year ->
                            val value = dataByYearMonth[Pair(year, month)]?.avgConsumption
                            if (value != null) {
                                val color = YearColors[index % YearColors.size]
                                val fraction = (value / maxVal).toFloat().coerceIn(0.03f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(14.dp)
                                        .padding(vertical = 1.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.width(80.dp)) {
                        years.forEachIndexed { index, year ->
                            val value = dataByYearMonth[Pair(year, month)]?.avgConsumption
                            Text(
                                value?.let { String.format("%.1f", it) } ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = YearColors[index % YearColors.size],
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CrossYearMonthCostChart(
    monthlyStats: List<MonthlyStats>,
    availableYears: List<Int>
) {
    val years = availableYears.sortedDescending()
    val dataByYearMonth = monthlyStats.associateBy { Pair(it.year, it.month) }
    val allValues = (1..12).flatMap { m ->
        years.mapNotNull { y -> dataByYearMonth[Pair(y, m)]?.totalCost }
    }
    val maxVal = allValues.maxOrNull() ?: return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                years.forEachIndexed { index, year ->
                    val color = YearColors[index % YearColors.size]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(12.dp).height(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${year}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            (1..12).forEach { month ->
                val hasData = years.any { y -> dataByYearMonth[Pair(y, month)]?.totalCost != null && dataByYearMonth[Pair(y, month)]!!.totalCost > 0 }
                if (!hasData) return@forEach

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${month}月",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        years.forEachIndexed { index, year ->
                            val value = dataByYearMonth[Pair(year, month)]?.totalCost
                            if (value != null && value > 0) {
                                val color = YearColors[index % YearColors.size]
                                val fraction = (value / maxVal).toFloat().coerceIn(0.03f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(14.dp)
                                        .padding(vertical = 1.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.width(80.dp)) {
                        years.forEachIndexed { index, year ->
                            val value = dataByYearMonth[Pair(year, month)]?.totalCost
                            Text(
                                value?.let { String.format("¥%.0f", it) } ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = YearColors[index % YearColors.size],
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ==================== 年度卡片 & 月度卡片 ====================

@Composable
private fun YearlyStatsCard(stats: YearlyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${stats.year} 年",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatsGridItem("加油次数", "${stats.recordCount} 次")
                StatsGridItem("总里程", String.format("%.0f km", stats.totalDistance))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatsGridItem("加油量", String.format("%.1f L", stats.totalFuel))
                StatsGridItem("花费", String.format("¥%.2f", stats.totalCost))
            }
            stats.avgConsumption?.let { avg ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatsGridItem("平均油耗", String.format("%.1f L/100km", avg))
                    val avgCostPerKm = if (stats.totalDistance > 0)
                        stats.totalCost / stats.totalDistance else null
                    StatsGridItem("平均成本", avgCostPerKm?.let { String.format("¥%.2f/km", it) } ?: "--")
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatsCard(stats: MonthlyStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stats.yearMonth, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(String.format("加油 %.1fL", stats.totalFuel), style = MaterialTheme.typography.bodyMedium)
                Text(String.format("花费 ¥%.2f", stats.totalCost), style = MaterialTheme.typography.bodyMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                stats.avgConsumption?.let {
                    Text(
                        String.format("油耗 %.1f L/100km", it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } ?: Text("油耗 --", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${stats.recordCount} 次加油",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
