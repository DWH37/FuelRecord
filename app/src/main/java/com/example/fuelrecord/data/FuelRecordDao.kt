package com.example.fuelrecord.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRecordDao {

    @Query("SELECT * FROM fuel_records ORDER BY date DESC, totalMileage DESC")
    fun getAllRecords(): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records ORDER BY totalMileage ASC")
    fun getAllRecordsByMileage(): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records WHERE id = :id")
    suspend fun getRecordById(id: Long): FuelRecord?

    @Insert
    suspend fun insert(record: FuelRecord): Long

    @Update
    suspend fun update(record: FuelRecord)

    @Delete
    suspend fun delete(record: FuelRecord)
}
