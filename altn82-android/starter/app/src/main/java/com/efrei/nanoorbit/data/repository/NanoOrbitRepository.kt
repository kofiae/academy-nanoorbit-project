package com.efrei.nanoorbit.data.repository

import com.efrei.nanoorbit.data.api.NanoOrbitApi
import com.efrei.nanoorbit.data.api.RetrofitClient
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.*
import kotlinx.coroutines.delay

class NanoOrbitRepository(
    private val api: NanoOrbitApi = RetrofitClient.api
) {

    suspend fun getSatellites(): List<Satellite> {
        return try {
            api.getSatellites()
        } catch (e: Exception) {
            delay(500)
            MockData.satellites
        }
    }

    suspend fun getInstruments(idSatellite: String): List<Instrument> {
        return try {
            api.getInstruments(idSatellite)
        } catch (e: Exception) {
            delay(300)
            MockData.instruments
        }
    }

    suspend fun getFenetres(): List<FenetreCom> {
        return try {
            api.getFenetres()
        } catch (e: Exception) {
            delay(300)
            MockData.fenetres
        }
    }

    suspend fun getFenetresBySatellite(idSatellite: String): List<FenetreCom> {
        return try {
            api.getFenetres().filter { it.idSatellite == idSatellite }
        } catch (e: Exception) {
            delay(200)
            MockData.getFenetresBySatellite(idSatellite)
        }
    }

    suspend fun getStations(): List<StationSol> {
        delay(200)
        return MockData.stations
    }

    // Validation RG-F04 côté client — miroir du trigger Oracle T3
    // CHECK(duree BETWEEN 1 AND 900) : bloque avant tout envoi reseau
    fun validerFenetre(fenetre: FenetreCom): Result<Unit> = fenetre.valider()
}