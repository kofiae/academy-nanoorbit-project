package com.efrei.nanoorbit.data.models

import java.time.LocalDate
import java.time.LocalDateTime

// ══════════════════════════════════════════════════════════
// ENUMS — Contraintes CHECK Oracle
// ══════════════════════════════════════════════════════════

/**
 * Table SATELLITE — colonne statut
 * CHECK IN ('Opérationnel','En veille','Défaillant','Désorbité')
 */
enum class StatutSatellite(val label: String) {
    OPERATIONNEL("Opérationnel"),
    EN_VEILLE("En veille"),
    DEFAILLANT("Défaillant"),
    DESORBITE("Désorbité")
}

/**
 * Table SATELLITE — colonne format_cubesat
 * CHECK IN ('1U','3U','6U','12U')
 */
enum class FormatCubeSat(val label: String) {
    U1("1U"),
    U3("3U"),
    U6("6U"),
    U12("12U")
}

/**
 * Table ORBITE — colonne type_orbite
 * CHECK IN ('SSO','LEO','MEO','GEO')
 */
enum class TypeOrbite(val label: String) {
    SSO("SSO"),
    LEO("LEO"),
    MEO("MEO"),
    GEO("GEO")
}

/**
 * Table FENETRE_COM — colonne statut
 * CHECK IN ('Planifiée','Réalisée')
 */
enum class StatutFenetre(val label: String) {
    PLANIFIEE("Planifiée"),
    REALISEE("Réalisée")
}

/**
 * Table EMBARQUEMENT — colonne etat_fonctionnement
 * CHECK IN ('Nominal','Dégradé','Hors service')
 */
enum class EtatFonctionnement(val label: String) {
    NOMINAL("Nominal"),
    DEGRADE("Dégradé"),
    HORS_SERVICE("Hors service")
}

/**
 * Table MISSION — colonne statut_mission
 * CHECK IN ('Active','Terminée')
 */
enum class StatutMission(val label: String) {
    ACTIVE("Active"),
    TERMINEE("Terminée")
}

/**
 * Table STATION_SOL — colonne statut
 * CHECK IN ('Active','Maintenance','Inactive')
 */
enum class StatutStation(val label: String) {
    ACTIVE("Active"),
    MAINTENANCE("Maintenance"),
    INACTIVE("Inactive")
}

/**
 * Table CENTRE_CONTROLE — colonne statut
 * CHECK IN ('Actif','Inactif')
 */
enum class StatutCentre(val label: String) {
    ACTIF("Actif"),
    INACTIF("Inactif")
}

// ══════════════════════════════════════════════════════════
// DATA CLASSES — Tables Oracle MLD
// ══════════════════════════════════════════════════════════

/**
 * Table ORBITE
 *
 * Oracle             | Kotlin          | Type Oracle
 * -------------------|-----------------|---------------------------
 * id_orbite          | idOrbite        | NUMBER (AI)
 * type_orbite        | typeOrbite      | VARCHAR2(10)
 * altitude           | altitude        | NUMBER(5)       — en km
 * inclinaison        | inclinaison     | NUMBER(5,2)     — en degrés
 * periode_orbitale   | periodeOrbitale | NUMBER(6,2)     — en minutes
 * excentricite       | excentricite    | NUMBER(6,4)
 * zone_couverture    | zoneCouverture  | VARCHAR2(200)
 *
 * RG-O02 : UNIQUE(altitude, inclinaison)
 */
data class Orbite(
    val idOrbite: Int,
    val typeOrbite: TypeOrbite,
    val altitude: Int,
    val inclinaison: Double,
    val periodeOrbitale: Double,
    val excentricite: Double,
    val zoneCouverture: String? = null
)

/**
 * Table SATELLITE
 *
 * Oracle             | Kotlin           | Type Oracle
 * -------------------|------------------|---------------------------
 * id_satellite       | idSatellite      | VARCHAR2(20)
 * nom_satellite      | nomSatellite     | VARCHAR2(100)
 * date_lancement     | dateLancement    | DATE             — nullable
 * masse              | masse            | NUMBER(5,2)      — en kg, nullable
 * format_cubesat     | formatCubesat    | VARCHAR2(5)
 * statut             | statut           | VARCHAR2(30)
 * duree_vie_prevue   | dureeViePrevue   | NUMBER(4)        — en mois
 * capacite_batterie  | capaciteBatterie | NUMBER(6,1)      — en Wh
 * #id_orbite         | idOrbite         | NUMBER           — FK → ORBITE
 *
 * RG-S01 : idSatellite immuable après mise en orbite
 * RG-S06 : DESORBITE bloque FENETRE_COM et PARTICIPATION
 */
data class Satellite(
    val idSatellite: String,
    val nomSatellite: String,
    val statut: StatutSatellite,
    val formatCubesat: FormatCubeSat,
    val idOrbite: Int,
    val dureeViePrevue: Int,              // En mois
    val capaciteBatterie: Double,         // En Wh
    val dateLancement: LocalDate? = null,
    val masse: Double? = null
)

/**
 * Table INSTRUMENT
 *
 * Oracle             | Kotlin          | Type Oracle
 * -------------------|-----------------|---------------------------
 * ref_instrument     | refInstrument   | VARCHAR2(20)
 * type_instrument    | typeInstrument  | VARCHAR2(50)
 * modele             | modele          | VARCHAR2(100)
 * resolution         | resolution      | NUMBER(6,1)     — nullable (ex: AIS)
 * consommation       | consommation    | NUMBER(5,2)     — en watts, nullable
 * masse              | masse           | NUMBER(5,3)     — en kg, nullable
 *
 * RG-I01 : catalogue global indépendant
 * RG-I03 : instrument non simultané sur deux satellites (trigger)
 */
data class Instrument(
    val refInstrument: String,
    val typeInstrument: String,
    val modele: String,
    val consommation: Double,             // En watts
    val masse: Double,                    // En kg
    val resolution: Double? = null
)

/**
 * Table EMBARQUEMENT — entité-association SATELLITE × INSTRUMENT
 *
 * Oracle               | Kotlin               | Type Oracle
 * ---------------------|----------------------|---------------------------
 * #id_satellite        | idSatellite          | VARCHAR2(20)  — PK composite
 * #ref_instrument      | refInstrument        | VARCHAR2(20)  — PK composite
 * date_integration     | dateIntegration      | DATE
 * etat_fonctionnement  | etatFonctionnement   | VARCHAR2(20)
 *
 * RG-S04 : attributs propres à chaque couple (satellite, instrument)
 * RG-I04 : HORS_SERVICE > 30j → signalement (procédure PL/SQL Phase 3)
 */
data class Embarquement(
    val idSatellite: String,
    val refInstrument: String,
    val dateIntegration: LocalDate,
    val etatFonctionnement: EtatFonctionnement
)

/**
 * Table CENTRE_CONTROLE
 *
 * Oracle             | Kotlin        | Type Oracle
 * -------------------|---------------|---------------------------
 * id_centre          | idCentre      | NUMBER (AI)
 * nom_centre         | nomCentre     | VARCHAR2(100)  — UNIQUE
 * ville              | ville         | VARCHAR2(50)
 * region_geo         | regionGeo     | VARCHAR2(50)   — CHECK IN ('Europe','Amériques','Asie-Pacifique')
 * fuseau_horaire     | fuseauHoraire | VARCHAR2(50)   — identifiant IANA
 * statut             | statut        | VARCHAR2(20)
 */
data class CentreControle(
    val idCentre: Int,
    val nomCentre: String,
    val ville: String,
    val regionGeo: String,
    val fuseauHoraire: String,
    val statut: StatutCentre
)

/**
 * Table STATION_SOL
 *
 * Oracle             | Kotlin           | Type Oracle
 * -------------------|------------------|---------------------------
 * code_station       | codeStation      | VARCHAR2(20)
 * nom_station        | nomStation       | VARCHAR2(100)
 * latitude           | latitude         | NUMBER(9,6)   — osmdroid
 * longitude          | longitude        | NUMBER(9,6)   — osmdroid
 * diametre_antenne   | diametreAntenne  | NUMBER(4,1)   — nullable, en mètres
 * bande_frequence    | bandeFrequence   | VARCHAR2(10)  — nullable
 * debit_max          | debitMax         | NUMBER(6,1)   — nullable, en Mbps
 * statut             | statut           | VARCHAR2(20)
 * #id_centre         | idCentre         | NUMBER        — FK → CENTRE_CONTROLE
 *
 * RG-G03 : MAINTENANCE bloque toute nouvelle fenêtre (trigger T1)
 * RG-G04 : exactement un centre par station
 */
data class StationSol(
    val codeStation: String,
    val nomStation: String,
    val latitude: Double,
    val longitude: Double,
    val statut: StatutStation,
    val idCentre: Int,
    val diametreAntenne: Double? = null,
    val bandeFrequence: String? = null,
    val debitMax: Double? = null
)

/**
 * Table MISSION
 *
 * Oracle             | Kotlin        | Type Oracle
 * -------------------|---------------|---------------------------
 * id_mission         | idMission     | VARCHAR2(20)
 * nom_mission        | nomMission    | VARCHAR2(100)
 * objectif           | objectif      | VARCHAR2(500)
 * zone_geo_cible     | zoneGeoCible  | VARCHAR2(200)  — nullable
 * date_debut         | dateDebut     | DATE           — NOT NULL
 * date_fin           | dateFin       | DATE           — nullable
 * statut_mission     | statutMission | VARCHAR2(20)
 *
 * RG-M01 : dateDebut NOT NULL, dateFin nullable si mission en cours
 * RG-M04 : TERMINEE bloque tout nouvel ajout satellite (trigger T4)
 */
data class Mission(
    val idMission: String,
    val nomMission: String,
    val objectif: String,
    val dateDebut: LocalDate,
    val statutMission: StatutMission,
    val zoneGeoCible: String? = null,
    val dateFin: LocalDate? = null
)

/**
 * Table FENETRE_COM
 *
 * Oracle             | Kotlin         | Type Oracle
 * -------------------|----------------|---------------------------
 * id_fenetre         | idFenetre      | NUMBER (AI)
 * datetime_debut     | datetimeDebut  | TIMESTAMP
 * duree              | dureeSecondes  | NUMBER(4)     — [1-900] RG-F04
 * elevation_max      | elevationMax   | NUMBER(5,2)   — en degrés
 * volume_donnees     | volumeDonnees  | NUMBER(8,1)   — nullable (RG-F05)
 * statut             | statut         | VARCHAR2(20)
 * #id_satellite      | idSatellite    | VARCHAR2(20)  — FK → SATELLITE
 * #code_station      | codeStation    | VARCHAR2(20)  — FK → STATION_SOL
 *
 * RG-F04 : dureeSecondes [1-900] — validation côté client OBLIGATOIRE avant envoi
 * RG-F05 : volumeDonnees NULL si statut != REALISEE
 * RG-F02/F03 : chevauchements gérés par trigger T2 Oracle
 */
data class FenetreCom(
    val idFenetre: Int,
    val datetimeDebut: LocalDateTime,
    val dureeSecondes: Int,
    val elevationMax: Double,
    val statut: StatutFenetre,
    val idSatellite: String,
    val codeStation: String,
    val volumeDonnees: Double? = null
) {
    /**
     * Validation côté client — miroir du trigger Oracle T1 et RG-F04.
     */
    fun valider(): Result<Unit> {
        if (dureeSecondes < 1 || dureeSecondes > 900) {
            return Result.failure(
                IllegalArgumentException(
                    "La durée doit être comprise entre 1 et 900 secondes (RG-F04). " +
                            "Valeur saisie : $dureeSecondes s."
                )
            )
        }
        if (statut != StatutFenetre.REALISEE && volumeDonnees != null) {
            return Result.failure(
                IllegalArgumentException(
                    "Le volume de données ne peut être renseigné que pour une fenêtre " +
                            "au statut Réalisée (RG-F05)."
                )
            )
        }
        return Result.success(Unit)
    }
}

/**
 * Table PARTICIPATION — entité-association SATELLITE × MISSION
 *
 * Oracle             | Kotlin        | Type Oracle
 * -------------------|---------------|---------------------------
 * #id_satellite      | idSatellite   | VARCHAR2(20)  — PK composite
 * #id_mission        | idMission     | VARCHAR2(20)  — PK composite
 * role_satellite     | roleSatellite | VARCHAR2(100) — NOT NULL
 *
 * RG-M03 : rôle spécifique à chaque participation
 * RG-M04 : impossible d'ajouter à une mission Terminée (trigger T4)
 */
data class Participation(
    val idSatellite: String,
    val idMission: String,
    val roleSatellite: String
)

/**
 * Table HISTORIQUE_STATUT — alimentée par trigger T5 Oracle uniquement
 *
 * Oracle             | Kotlin           | Type Oracle
 * -------------------|------------------|---------------------------
 * id_historique      | idHistorique     | NUMBER (AI)
 * #id_satellite      | idSatellite      | VARCHAR2(20)  — FK → SATELLITE
 * ancien_statut      | ancienStatut     | VARCHAR2(30)
 * nouveau_statut     | nouveauStatut    | VARCHAR2(30)
 * date_changement    | dateChangement   | TIMESTAMP
 * motif              | motif            | VARCHAR2(255) — nullable
 */
data class HistoriqueStatut(
    val idHistorique: Int,
    val idSatellite: String,
    val ancienStatut: StatutSatellite,
    val nouveauStatut: StatutSatellite,
    val dateChangement: LocalDateTime,
    val motif: String? = null
)