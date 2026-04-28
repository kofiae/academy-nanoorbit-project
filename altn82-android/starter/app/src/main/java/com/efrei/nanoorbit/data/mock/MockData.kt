package com.efrei.nanoorbit.data.mock

import com.efrei.nanoorbit.data.models.*
import java.time.LocalDate
import java.time.LocalDateTime

object MockData {

    val orbites = listOf(
        Orbite(
            idOrbite = 1,
            typeOrbite = TypeOrbite.SSO,
            altitude = 550,
            inclinaison = 97.6,
            periodeOrbitale = 95.5,
            excentricite = 0.0010,
            zoneCouverture = "Polaire globale — Europe / Arctique"
        ),
        Orbite(
            idOrbite = 2,
            typeOrbite = TypeOrbite.SSO,
            altitude = 700,
            inclinaison = 98.2,
            periodeOrbitale = 98.8,
            excentricite = 0.0008,
            zoneCouverture = "Polaire globale — haute latitude"
        ),
        Orbite(
            idOrbite = 3,
            typeOrbite = TypeOrbite.LEO,
            altitude = 400,
            inclinaison = 51.6,
            periodeOrbitale = 92.6,
            excentricite = 0.0020,
            zoneCouverture = "Équatoriale — zone tropicale"
        )
    )

    val satellites = listOf(
        Satellite(
            idSatellite = "SAT-001",
            nomSatellite = "NanoOrbit-Alpha",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = FormatCubeSat.U3,
            idOrbite = 1,
            dureeViePrevue = 60,
            capaciteBatterie = 20.0,
            dateLancement = LocalDate.of(2022, 3, 15),
            masse = 1.30
        ),
        Satellite(
            idSatellite = "SAT-002",
            nomSatellite = "NanoOrbit-Beta",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = FormatCubeSat.U3,
            idOrbite = 1,
            dureeViePrevue = 60,
            capaciteBatterie = 20.0,
            dateLancement = LocalDate.of(2022, 3, 15),
            masse = 1.30
        ),
        Satellite(
            idSatellite = "SAT-003",
            nomSatellite = "NanoOrbit-Gamma",
            statut = StatutSatellite.OPERATIONNEL,
            formatCubesat = FormatCubeSat.U6,
            idOrbite = 2,
            dureeViePrevue = 84,
            capaciteBatterie = 40.0,
            dateLancement = LocalDate.of(2023, 6, 10),
            masse = 2.00
        ),
        Satellite(
            idSatellite = "SAT-004",
            nomSatellite = "NanoOrbit-Delta",
            statut = StatutSatellite.EN_VEILLE,
            formatCubesat = FormatCubeSat.U6,
            idOrbite = 2,
            dureeViePrevue = 84,
            capaciteBatterie = 40.0,
            dateLancement = LocalDate.of(2023, 6, 10),
            masse = 2.00
        ),
        // ⚠️ SAT-005 Désorbité — miroir trigger Oracle T1 (RG-S06)
        // L'app désactive toute interaction pour ce satellite
        Satellite(
            idSatellite = "SAT-005",
            nomSatellite = "NanoOrbit-Epsilon",
            statut = StatutSatellite.DESORBITE,
            formatCubesat = FormatCubeSat.U12,
            idOrbite = 3,
            dureeViePrevue = 36,
            capaciteBatterie = 80.0,
            dateLancement = LocalDate.of(2021, 11, 20),
            masse = 4.50
        )
    )

    val instruments = listOf(
        Instrument(
            refInstrument = "INS-CAM-01",
            typeInstrument = "Caméra optique",
            modele = "PlanetScope-Mini",
            resolution = 3.0,
            consommation = 2.5,
            masse = 0.40
        ),
        Instrument(
            refInstrument = "INS-IR-01",
            typeInstrument = "Infrarouge",
            modele = "FLIR-Lepton-3",
            resolution = 160.0,
            consommation = 1.2,
            masse = 0.15
        ),
        // resolution = null car AIS ne produit pas d'image — cas NVL Palier 2
        Instrument(
            refInstrument = "INS-AIS-01",
            typeInstrument = "Récepteur AIS",
            modele = "ShipTrack-V2",
            resolution = null,
            consommation = 0.8,
            masse = 0.12
        ),
        Instrument(
            refInstrument = "INS-SPEC-01",
            typeInstrument = "Spectromètre",
            modele = "HyperSpec-Nano",
            resolution = 30.0,
            consommation = 3.1,
            masse = 0.60
        )
    )

    val stations = listOf(
        StationSol(
            codeStation = "GS-TLS-01",
            nomStation = "Toulouse Ground Station",
            latitude = 43.6047,
            longitude = 1.4442,
            statut = StatutStation.ACTIVE,
            idCentre = 1,
            diametreAntenne = 3.5,
            bandeFrequence = "S",
            debitMax = 150.0
        ),
        StationSol(
            codeStation = "GS-KIR-01",
            nomStation = "Kiruna Arctic Station",
            latitude = 67.8557,
            longitude = 20.2253,
            statut = StatutStation.ACTIVE,
            idCentre = 1,
            diametreAntenne = 5.4,
            bandeFrequence = "X",
            debitMax = 400.0
        ),
        // ⚠️ GS-SGP-01 Maintenance — miroir trigger Oracle T1 (RG-G03)
        StationSol(
            codeStation = "GS-SGP-01",
            nomStation = "Singapore Station",
            latitude = 1.3521,
            longitude = 103.8198,
            statut = StatutStation.MAINTENANCE,
            idCentre = 3,
            diametreAntenne = 3.0,
            bandeFrequence = "S",
            debitMax = 120.0
        )
    )

    val fenetres = listOf(
        FenetreCom(
            idFenetre = 1,
            datetimeDebut = LocalDateTime.of(2024, 1, 15, 9, 14),
            dureeSecondes = 420,
            elevationMax = 82.3,
            statut = StatutFenetre.REALISEE,
            idSatellite = "SAT-001",
            codeStation = "GS-KIR-01",
            volumeDonnees = 1250.0
        ),
        FenetreCom(
            idFenetre = 2,
            datetimeDebut = LocalDateTime.of(2024, 1, 15, 11, 52),
            dureeSecondes = 310,
            elevationMax = 67.1,
            statut = StatutFenetre.REALISEE,
            idSatellite = "SAT-002",
            codeStation = "GS-TLS-01",
            volumeDonnees = 890.0
        ),
        FenetreCom(
            idFenetre = 3,
            datetimeDebut = LocalDateTime.of(2024, 1, 16, 8, 30),
            dureeSecondes = 540,
            elevationMax = 88.9,
            statut = StatutFenetre.REALISEE,
            idSatellite = "SAT-003",
            codeStation = "GS-KIR-01",
            volumeDonnees = 1680.0
        ),
        // volumeDonnees = null obligatoire si Planifiée (RG-F05 / trigger T3)
        FenetreCom(
            idFenetre = 4,
            datetimeDebut = LocalDateTime.of(2024, 1, 20, 14, 22),
            dureeSecondes = 380,
            elevationMax = 71.4,
            statut = StatutFenetre.PLANIFIEE,
            idSatellite = "SAT-001",
            codeStation = "GS-TLS-01",
            volumeDonnees = null
        ),
        FenetreCom(
            idFenetre = 5,
            datetimeDebut = LocalDateTime.of(2024, 1, 21, 7, 45),
            dureeSecondes = 290,
            elevationMax = 59.8,
            statut = StatutFenetre.PLANIFIEE,
            idSatellite = "SAT-003",
            codeStation = "GS-TLS-01",
            volumeDonnees = null
        )
    )

    val missions = listOf(
        Mission(
            idMission = "MSN-ARC-2023",
            nomMission = "ArcticWatch 2023",
            objectif = "Surveillance fonte des glaces et dynamique des banquises",
            dateDebut = LocalDate.of(2023, 1, 1),
            statutMission = StatutMission.ACTIVE,
            zoneGeoCible = "Arctique / Groenland",
            dateFin = null
        ),
        Mission(
            idMission = "MSN-DEF-2022",
            nomMission = "DeforestAlert",
            objectif = "Détection et cartographie de la déforestation",
            dateDebut = LocalDate.of(2022, 6, 1),
            statutMission = StatutMission.TERMINEE,
            zoneGeoCible = "Amazonie / Congo",
            dateFin = LocalDate.of(2023, 5, 31)
        ),
        Mission(
            idMission = "MSN-COAST-2024",
            nomMission = "CoastGuard 2024",
            objectif = "Surveillance évolution du trait de côte et détection d'érosion",
            dateDebut = LocalDate.of(2024, 3, 1),
            statutMission = StatutMission.ACTIVE,
            zoneGeoCible = "Méditerranée / Atlantique",
            dateFin = null
        )
    )

    // Helpers pour retrouver un élément par son ID
    fun getOrbiteById(id: Int) = orbites.first { it.idOrbite == id }
    fun getSatelliteById(id: String) = satellites.first { it.idSatellite == id }
    fun getStationById(code: String) = stations.first { it.codeStation == code }
    fun getFenetresBySatellite(id: String) = fenetres.filter { it.idSatellite == id }
    fun getFenetresByStation(code: String) = fenetres.filter { it.codeStation == code }
}