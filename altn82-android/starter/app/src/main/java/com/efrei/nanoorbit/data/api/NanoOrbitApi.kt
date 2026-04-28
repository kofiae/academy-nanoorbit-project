package com.efrei.nanoorbit.data.api

import com.efrei.nanoorbit.data.models.FenetreCom
import com.efrei.nanoorbit.data.models.Instrument
import com.efrei.nanoorbit.data.models.Satellite
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface Retrofit - miroir des endpoints du serveur NanoOrbit.
 *
 * Correspondance avec le projet BDD :
 *   GET /satellites          → SELECT * FROM SATELLITE
 *   GET /satellites/{id}/instruments → SELECT i.* FROM INSTRUMENT i JOIN EMBARQUEMENT e ON ...
 *   GET /fenetres            → SELECT * FROM FENETRE_COM
 */
interface NanoOrbitApi {

    @GET("satellites")
    suspend fun getSatellites(): List<Satellite>

    @GET("satellites/{id}/instruments")
    suspend fun getInstruments(@Path("id") idSatellite: String): List<Instrument>

    @GET("fenetres")
    suspend fun getFenetres(): List<FenetreCom>
}
