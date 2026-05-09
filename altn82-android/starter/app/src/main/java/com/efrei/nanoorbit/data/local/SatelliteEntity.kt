package com.efrei.nanoorbit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.efrei.nanoorbit.data.models.*
import java.time.LocalDate

@Entity(tableName = "satellites")
data class SatelliteEntity(
    @PrimaryKey
    val idSatellite: String,
    val nomSatellite: String,
    val statut: String,
    val formatCubesat: String,
    val idOrbite: Int,
    val dureeViePrevue: Int,
    val capaciteBatterie: Double,
    val dateLancement: String?,
    val masse: Double?,
    val derniereMiseAJour: Long = System.currentTimeMillis()
) {
    fun toSatellite(): Satellite = Satellite(
        idSatellite = idSatellite,
        nomSatellite = nomSatellite,
        statut = StatutSatellite.entries.first { it.label == statut },
        formatCubesat = FormatCubeSat.entries.first { it.label == formatCubesat },
        idOrbite = idOrbite,
        dureeViePrevue = dureeViePrevue,
        capaciteBatterie = capaciteBatterie,
        dateLancement = dateLancement?.let { LocalDate.parse(it) },
        masse = masse
    )

    companion object {
        fun fromSatellite(satellite: Satellite): SatelliteEntity = SatelliteEntity(
            idSatellite = satellite.idSatellite,
            nomSatellite = satellite.nomSatellite,
            statut = satellite.statut.label,
            formatCubesat = satellite.formatCubesat.label,
            idOrbite = satellite.idOrbite,
            dureeViePrevue = satellite.dureeViePrevue,
            capaciteBatterie = satellite.capaciteBatterie,
            dateLancement = satellite.dateLancement?.toString(),
            masse = satellite.masse
        )
    }
}