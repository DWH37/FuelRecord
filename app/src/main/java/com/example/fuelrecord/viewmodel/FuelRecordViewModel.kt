package com.example.fuelrecord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fuelrecord.data.AppDatabase
import com.example.fuelrecord.data.FuelRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class RecordWithConsumption(
    val record: FuelRecord,
    val distance: Double?,
    val consumption: Double?,
    val costPerKm: Double?
)

data class MonthlyStats(
    val yearMonth: String,
    val totalFuel: Double,
    val totalCost: Double,
    val avgConsumption: Double?,
    val recordCount: Int
)

data class OverallStats(
    val totalFuel: Double,
    val totalCost: Double,
    val avgConsumption: Double?,
    val totalDistance: Double,
    val recordCount: Int
)

class FuelRecordViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).fuelRecordDao()

    private val recordsByMileage: StateFlow<List<FuelRecord>> = dao.getAllRecordsByMileage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recordsWithConsumption: StateFlow<List<RecordWithConsumption>> =
        recordsByMileage.map { records ->
            records.mapIndexed { index, record ->
                if (index == 0) {
                    RecordWithConsumption(record, null, null, null)
                } else {
                    val prevRecord = records[index - 1]
                    val distance = record.totalMileage - prevRecord.totalMileage
                    if (distance > 0 && record.isFull) {
                        val consumption = record.fuelAmount / distance * 100
                        val costPerKm = record.cost / distance
                        RecordWithConsumption(record, distance, consumption, costPerKm)
                    } else {
                        RecordWithConsumption(
                            record,
                            if (distance > 0) distance else null,
                            null,
                            null
                        )
                    }
                }
            }.sortedByDescending { it.record.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallStats: StateFlow<OverallStats> = recordsByMileage.map { records ->
        if (records.isEmpty()) {
            OverallStats(0.0, 0.0, null, 0.0, 0)
        } else {
            val totalFuel = records.sumOf { it.fuelAmount }
            val totalCost = records.sumOf { it.cost }
            val totalDistance = if (records.size > 1)
                records.last().totalMileage - records.first().totalMileage else 0.0
            val avgConsumption =
                if (totalDistance > 0) totalFuel / totalDistance * 100 else null
            OverallStats(totalFuel, totalCost, avgConsumption, totalDistance, records.size)
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        OverallStats(0.0, 0.0, null, 0.0, 0)
    )

    val monthlyStats: StateFlow<List<MonthlyStats>> =
        recordsWithConsumption.map { records ->
            records.groupBy { rc ->
                val cal = Calendar.getInstance().apply { timeInMillis = rc.record.date }
                String.format(
                    "%d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1
                )
            }.map { (yearMonth, monthRecords) ->
                val totalFuel = monthRecords.sumOf { it.record.fuelAmount }
                val totalCost = monthRecords.sumOf { it.record.cost }
                val consumptions = monthRecords.mapNotNull { it.consumption }
                val avgConsumption =
                    if (consumptions.isNotEmpty()) consumptions.average() else null
                MonthlyStats(yearMonth, totalFuel, totalCost, avgConsumption, monthRecords.size)
            }.sortedByDescending { it.yearMonth }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getRecordById(id: Long): FuelRecord? = dao.getRecordById(id)

    fun addRecord(record: FuelRecord) {
        viewModelScope.launch { dao.insert(record) }
    }

    fun updateRecord(record: FuelRecord) {
        viewModelScope.launch { dao.update(record) }
    }

    fun deleteRecord(record: FuelRecord) {
        viewModelScope.launch { dao.delete(record) }
    }
}
