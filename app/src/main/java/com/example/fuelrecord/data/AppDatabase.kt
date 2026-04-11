package com.example.fuelrecord.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// exportSchema = true 会将 schema 保存到 schemas/ 目录，用于生成迁移 SQL。
// 升级数据库版本时，必须提供对应的 Migration，绝对不要使用
// fallbackToDestructiveMigration()，否则升级时数据会被清空。
//
// 版本升级示例（假设新增一列 fuelType）：
// 1. 将 version 改为 2
// 2. 添加迁移：
//    val MIGRATION_1_2 = object : Migration(1, 2) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("ALTER TABLE fuel_records ADD COLUMN fuelType TEXT NOT NULL DEFAULT '92#'")
//        }
//    }
// 3. 在 databaseBuilder 中调用 .addMigrations(MIGRATION_1_2)
@Database(entities = [FuelRecord::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fuelRecordDao(): FuelRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuel_record_database"
                )
                // 升级版本时在此添加 Migration，例如：
                // .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
