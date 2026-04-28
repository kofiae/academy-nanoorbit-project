package com.efrei.nanoorbit.data.repository

import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.*
import kotlinx.coroutines.delay

class NanoOrbitRepository {

    suspend fun getSatellites(): List<Satellite> {
        delay(500)
        return MockData.satellites
    }

    suspend fun getInstruments(idSatellite: String): List<Instrument> {
        delay(300)
        return MockData.instruments
    }

    suspend fun getFenetres(): List<FenetreCom> {
        delay(300)
        return MockData.fenetres
    }

    suspend fun getFenetresBySatellite(idSatellite: String): List<FenetreCom> {
        delay(200)
        return MockData.getFenetresBySatellite(idSatellite)
    }

    suspend fun getStations(): List<StationSol> {
        delay(200)
        return MockData.stations
    }

    // Validation RG-F04 côté client — miroir trigger Oracle T1
    // CHECK(duree BETWEEN 1 AND 900)
    fun validerFenetre(fenetre: FenetreCom): Result<Unit> = fenetre.valider()
}