package com.efrei.nanoorbit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NanoOrbitDao {

    @Query("SELECT * FROM satellites ORDER BY idSatellite")
    fun getAllSatellites(): Flow<List<SatelliteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatellites(satellites: List<SatelliteEntity>)

    @Query("DELETE FROM satellites")
    suspend fun clearSatellites()

    @Query("SELECT * FROM fenetres_com ORDER BY datetimeDebut")
    fun getAllFenetres(): Flow<List<FenetreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFenetres(fenetres: List<FenetreEntity>)

    @Query("DELETE FROM fenetres_com")
    suspend fun clearFenetres()

    @Query("SELECT MIN(derniereMiseAJour) FROM satellites")
    suspend fun getLastUpdate(): Long?
}