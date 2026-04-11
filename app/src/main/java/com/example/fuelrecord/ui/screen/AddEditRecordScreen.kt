package com.example.fuelrecord.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fuelrecord.data.FuelRecord
import com.example.fuelrecord.viewmodel.FuelRecordViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecordScreen(
    viewModel: FuelRecordViewModel,
    recordId: Long?,
    onNavigateBack: () -> Unit
) {
    val isEditing = recordId != null && recordId > 0
    var isLoaded by remember { mutableStateOf(!isEditing) }
    var editingRecordId by remember { mutableLongStateOf(0L) }

    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var mileageText by remember { mutableStateOf("") }
    var fuelAmountText by remember { mutableStateOf("") }
    var unitPriceText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var isFull by remember { mutableStateOf(true) }
    var noteText by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load record for editing
    LaunchedEffect(recordId) {
        if (isEditing) {
            viewModel.getRecordById(recordId!!)?.let { r ->
                dateMillis = r.date
                mileageText =
                    r.totalMileage.toBigDecimal().stripTrailingZeros().toPlainString()
                fuelAmountText =
                    r.fuelAmount.toBigDecimal().stripTrailingZeros().toPlainString()
                unitPriceText =
                    r.unitPrice.toBigDecimal().stripTrailingZeros().toPlainString()
                costText = r.cost.toBigDecimal().stripTrailingZeros().toPlainString()
                isFull = r.isFull
                noteText = r.note
                editingRecordId = r.id
                isLoaded = true
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    fun autoCalculate(
        newAmount: String? = null,
        newPrice: String? = null,
        newCost: String? = null
    ) {
        val amount = (newAmount ?: fuelAmountText).toDoubleOrNull()
        val price = (newPrice ?: unitPriceText).toDoubleOrNull()
        val cost = (newCost ?: costText).toDoubleOrNull()

        if (newCost != null) {
            // User editing cost → calculate price
            if (amount != null && cost != null && amount > 0) {
                unitPriceText = String.format("%.2f", cost / amount)
            }
        } else {
            // User editing amount or price → calculate cost
            if (amount != null && price != null && amount > 0 && price > 0) {
                costText = String.format("%.2f", amount * price)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑记录" else "添加记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (!isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Date field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = dateFormat.format(Date(dateMillis)),
                    onValueChange = {},
                    label = { Text("日期") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Mileage
            OutlinedTextField(
                value = mileageText,
                onValueChange = { mileageText = it },
                label = { Text("总里程 (km)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Fuel amount
            OutlinedTextField(
                value = fuelAmountText,
                onValueChange = {
                    fuelAmountText = it
                    autoCalculate(newAmount = it)
                },
                label = { Text("加油升数 (L)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Unit price
            OutlinedTextField(
                value = unitPriceText,
                onValueChange = {
                    unitPriceText = it
                    autoCalculate(newPrice = it)
                },
                label = { Text("油价 (元/L)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Cost (auto-calculated or manual)
            OutlinedTextField(
                value = costText,
                onValueChange = {
                    costText = it
                    autoCalculate(newCost = it)
                },
                label = { Text("金额 (元)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                supportingText = { Text("自动计算: 升数 × 油价") }
            )

            // Is full checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isFull,
                    onCheckedChange = { isFull = it }
                )
                Text(
                    "已加满",
                    modifier = Modifier.clickable { isFull = !isFull }
                )
            }

            // Note
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("备注 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Error message
            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save button
            Button(
                onClick = {
                    val mileage = mileageText.toDoubleOrNull()
                    val fuelAmount = fuelAmountText.toDoubleOrNull()
                    val cost = costText.toDoubleOrNull()
                    val unitPrice = unitPriceText.toDoubleOrNull()

                    when {
                        mileage == null || mileage <= 0 ->
                            errorMessage = "请输入有效的总里程"

                        fuelAmount == null || fuelAmount <= 0 ->
                            errorMessage = "请输入有效的加油升数"

                        unitPrice == null || unitPrice <= 0 ->
                            errorMessage = "请输入有效的油价"

                        cost == null || cost <= 0 ->
                            errorMessage = "请输入有效的金额"

                        else -> {
                            errorMessage = null
                            val record = FuelRecord(
                                id = if (isEditing) editingRecordId else 0,
                                date = dateMillis,
                                totalMileage = mileage,
                                fuelAmount = fuelAmount,
                                cost = cost,
                                unitPrice = unitPrice,
                                isFull = isFull,
                                note = noteText.trim()
                            )
                            if (isEditing) viewModel.updateRecord(record)
                            else viewModel.addRecord(record)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (isEditing) "保存修改" else "添加记录")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && isEditing) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条加油记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord(
                        FuelRecord(
                            id = editingRecordId,
                            date = dateMillis,
                            totalMileage = mileageText.toDoubleOrNull() ?: 0.0,
                            fuelAmount = fuelAmountText.toDoubleOrNull() ?: 0.0,
                            cost = costText.toDoubleOrNull() ?: 0.0,
                            unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0,
                            isFull = isFull,
                            note = noteText
                        )
                    )
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}
