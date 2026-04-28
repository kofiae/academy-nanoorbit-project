package com.efrei.nanoorbit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.*

// ── Couleur selon le statut du satellite ──────────────────────────────────────
fun StatutSatellite.couleur(): Color = when (this) {
    StatutSatellite.OPERATIONNEL -> Color(0xFF2E7D32)  // vert
    StatutSatellite.EN_VEILLE    -> Color(0xFFF57C00)  // orange
    StatutSatellite.DEFAILLANT   -> Color(0xFFC62828)  // rouge
    StatutSatellite.DESORBITE    -> Color(0xFF757575)  // gris
}

// ── StatusBadge ───────────────────────────────────────────────────────────────
// Q2 : On utilise une enum class et pas une String libre car une String
// permettrait des valeurs invalides ("operationnel", "Opérationnel ").
// L'enum garantit exactement les mêmes valeurs que le CHECK Oracle.
@Composable
fun StatusBadge(
    statut: StatutSatellite,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = statut.couleur().copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = RoundedCornerShape(50),
                color = statut.couleur()
            ) {}
            Text(
                text = statut.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = statut.couleur()
            )
        }
    }
}

// ── SatelliteCard ─────────────────────────────────────────────────────────────
@Composable
fun SatelliteCard(
    satellite: Satellite,
    orbite: Orbite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDesorbite = satellite.statut == StatutSatellite.DESORBITE

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDesorbite)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDesorbite) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Carré coloré avec le format CubeSat
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = satellite.statut.couleur().copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = satellite.formatCubesat.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = satellite.statut.couleur()
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = satellite.nomSatellite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDesorbite)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${orbite.typeOrbite.label} · ${orbite.altitude} km",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Bandeau DÉSORBITÉ visible sous le nom
                if (isDesorbite) {
                    Text(
                        text = "DÉSORBITÉ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575),
                        letterSpacing = 1.sp
                    )
                }
            }

            StatusBadge(statut = satellite.statut)
        }
    }
}

// ── FenetreCard ───────────────────────────────────────────────────────────────
@Composable
fun FenetreCard(
    fenetre: FenetreCom,
    nomStation: String,
    modifier: Modifier = Modifier
) {
    val couleur = when (fenetre.statut) {
        StatutFenetre.PLANIFIEE -> Color(0xFF1565C0)  // bleu
        StatutFenetre.REALISEE  -> Color(0xFF2E7D32)  // vert
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barre colorée à gauche
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(2.dp),
                color = couleur
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nomStation,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val dt = fenetre.datetimeDebut
                Text(
                    text = "%02d/%02d/%d à %02dh%02d".format(
                        dt.dayOfMonth, dt.monthValue, dt.year,
                        dt.hour, dt.minute
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val min = fenetre.dureeSecondes / 60
                val sec = fenetre.dureeSecondes % 60
                Text(
                    text = "Durée : ${min}min ${sec}s",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = couleur.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = fenetre.statut.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = couleur
                    )
                }
                // Volume affiché seulement si Réalisée (RG-F05)
                fenetre.volumeDonnees?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${it.toInt()} Mo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── InstrumentItem ────────────────────────────────────────────────────────────
@Composable
fun InstrumentItem(
    instrument: Instrument,
    etatFonctionnement: EtatFonctionnement,
    modifier: Modifier = Modifier
) {
    val couleur = when (etatFonctionnement) {
        EtatFonctionnement.NOMINAL      -> Color(0xFF2E7D32)
        EtatFonctionnement.DEGRADE      -> Color(0xFFF57C00)
        EtatFonctionnement.HORS_SERVICE -> Color(0xFFC62828)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = RoundedCornerShape(50),
            color = couleur
        ) {}
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instrument.modele,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = instrument.typeInstrument,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Résolution N/A si null — cas INS-AIS-01 (Palier 2 NVL Oracle)
            Text(
                text = "Résolution : ${instrument.resolution?.let { "${it.toInt()} m" } ?: "N/A"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = etatFonctionnement.label,
            fontSize = 12.sp,
            color = couleur,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "StatusBadge — tous les statuts")
@Composable
fun PreviewStatusBadge() {
    Column(
        Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatutSatellite.entries.forEach { StatusBadge(statut = it) }
    }
}

@Preview(showBackground = true, name = "SatelliteCard — Opérationnel")
@Composable
fun PreviewSatelliteCard() {
    SatelliteCard(
        satellite = MockData.satellites[0],
        orbite = MockData.getOrbiteById(1),
        onClick = {}
    )
}

@Preview(showBackground = true, name = "SatelliteCard — Désorbité")
@Composable
fun PreviewSatelliteCardDesorbite() {
    SatelliteCard(
        satellite = MockData.satellites[4],
        orbite = MockData.getOrbiteById(3),
        onClick = {}
    )
}

@Preview(showBackground = true, name = "FenetreCard — Réalisée")
@Composable
fun PreviewFenetreCard() {
    FenetreCard(
        fenetre = MockData.fenetres[0],
        nomStation = "Kiruna Arctic Station"
    )
}