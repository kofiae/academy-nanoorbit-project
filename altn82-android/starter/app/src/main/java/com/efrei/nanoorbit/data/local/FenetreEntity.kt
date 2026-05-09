package com.efrei.nanoorbit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.efrei.nanoorbit.data.models.*
import java.time.LocalDateTime

@Entity(tableName = "fenetres_com")
data class FenetreEntity(
    @PrimaryKey
    val idFenetre: Int,
    val datetimeDebut: String,
    val dureeSecondes: Int,
    val elevationMax: Double,
    val statut: String,
    val idSatellite: String,
    val codeStation: String,
    val volumeDonnees: Double?,
    val derniereMiseAJour: Long = System.currentTimeMillis()
) {
    fun toFenetreCom(): FenetreCom = FenetreCom(
        idFenetre = idFenetre,
        datetimeDebut = LocalDateTime.parse(datetimeDebut),
        dureeSecondes = dureeSecondes,
        elevationMax = elevationMax,
        statut = StatutFenetre.entries.first { it.label == statut },
        idSatellite = idSatellite,
        codeStation = codeStation,
        volumeDonnees = volumeDonnees
    )

    companion object {
        fun fromFenetreCom(f: FenetreCom): FenetreEntity = FenetreEntity(
            idFenetre = f.idFenetre,
            datetimeDebut = f.datetimeDebut.toString(),
            dureeSecondes = f.dureeSecondes,
            elevationMax = f.elevationMax,
            statut = f.statut.label,
            idSatellite = f.idSatellite,
            codeStation = f.codeStation,
            volumeDonnees = f.volumeDonnees
        )
    }
}