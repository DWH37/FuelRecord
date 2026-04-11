package com.example.fuelrecord.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_records")
data class FuelRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,            // 日期时间戳(毫秒)
    val totalMileage: Double,  // 总里程(km)
    val fuelAmount: Double,    // 加油升数(L)
    val cost: Double,          // 金额(元)
    val unitPrice: Double,     // 油价(元/L)
    val isFull: Boolean,       // 是否加满
    val note: String = ""      // 备注
)
