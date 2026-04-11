package com.example.fuelrecord.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fuelrecord.viewmodel.FuelRecordViewModel
import com.example.fuelrecord.viewmodel.OverallStats
import com.example.fuelrecord.viewmodel.RecordWithConsumption
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    viewModel: FuelRecordViewModel,
    onAddClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onStatsClick: () -> Unit
) {
    val records by viewModel.recordsWithConsumption.collectAsState()
    val overallStats by viewModel.overallStats.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<RecordWithConsumption?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    // Uri of file chosen for import; triggers the import mode dialog
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Show snackbar whenever operationResult changes
    LaunchedEffect(operationResult) {
        operationResult?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearOperationResult()
        }
    }

    // SAF launcher: export CSV — user picks save location
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val csv = viewModel.exportToCsv()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(csv.toByteArray(Charsets.UTF_8))
                    }
                    snackbarHostState.showSnackbar("导出成功")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("导出失败: ${e.message}")
                }
            }
        }
    }

    // SAF launcher: import CSV — user picks file
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { pendingImportUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("油耗记录") },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.Info, contentDescription = "统计")
                    }
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("导出 CSV") },
                                onClick = {
                                    showMenu = false
                                    val fileName = "fuel_records_${
                                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                            .format(Date())
                                    }.csv"
                                    exportLauncher.launch(fileName)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("导入 CSV") },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("text/*", "*/*"))
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "暂无加油记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "点击右上角 + 添加第一条记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SummaryCard(overallStats = overallStats)
                }

                items(records, key = { it.record.id }) { recordWithConsumption ->
                    RecordCard(
                        recordWithConsumption = recordWithConsumption,
                        onClick = { onRecordClick(recordWithConsumption.record.id) },
                        onDelete = { showDeleteDialog = recordWithConsumption }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { rwc ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条加油记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord(rwc.record)
                    showDeleteDialog = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Import mode dialog: append or replace
    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("导入方式") },
            text = { Text("请选择导入方式：\n「追加」将新数据添加到现有记录中；\n「替换」将清空所有现有记录后导入。") },
            confirmButton = {
                TextButton(onClick = {
                    val csvContent = try {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""
                    } catch (e: Exception) {
                        scope.launch { snackbarHostState.showSnackbar("读取文件失败: ${e.message}") }
                        ""
                    }
                    pendingImportUri = null
                    if (csvContent.isNotBlank()) {
                        viewModel.importFromCsv(csvContent, replaceAll = true)
                    }
                }) {
                    Text("替换", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val csvContent = try {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""
                    } catch (e: Exception) {
                        scope.launch { snackbarHostState.showSnackbar("读取文件失败: ${e.message}") }
                        ""
                    }
                    pendingImportUri = null
                    if (csvContent.isNotBlank()) {
                        viewModel.importFromCsv(csvContent, replaceAll = false)
                    }
                }) {
                    Text("追加")
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(overallStats: OverallStats) {
    if (overallStats.recordCount == 0) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "总览",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("总加油", String.format("%.1fL", overallStats.totalFuel))
                StatItem("总花费", String.format("¥%.0f", overallStats.totalCost))
                overallStats.avgConsumption?.let {
                    StatItem("均耗", String.format("%.1fL/百km", it))
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun RecordCard(
    recordWithConsumption: RecordWithConsumption,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val record = recordWithConsumption.record
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dateFormat.format(Date(record.date)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.isFull) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text("已加满", style = MaterialTheme.typography.labelSmall)
                            },
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "里程 ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    String.format("%.0f km", record.totalMileage),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    String.format(
                        "%.2fL × ¥%.2f = ¥%.2f",
                        record.fuelAmount, record.unitPrice, record.cost
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            recordWithConsumption.consumption?.let { consumption ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        String.format("油耗 %.1f L/100km", consumption),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    recordWithConsumption.costPerKm?.let { costPerKm ->
                        Text(
                            String.format("¥%.2f/km", costPerKm),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (record.note.isNotBlank()) {
                Text(
                    record.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
