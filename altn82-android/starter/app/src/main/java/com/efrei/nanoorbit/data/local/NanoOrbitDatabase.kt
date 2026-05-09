package com.efrei.nanoorbit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SatelliteEntity::class, FenetreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NanoOrbitDatabase : RoomDatabase() {

    abstract fun nanoOrbitDao(): NanoOrbitDao

    companion object {
        @Volatile
        private var INSTANCE: NanoOrbitDatabase? = null

        fun getDatabase(context: Context): NanoOrbitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NanoOrbitDatabase::class.java,
                    "nanoorbit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}