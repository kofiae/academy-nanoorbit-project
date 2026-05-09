package com.efrei.nanoorbit.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.*
import com.efrei.nanoorbit.ui.components.InstrumentItem
import com.efrei.nanoorbit.ui.components.StatusBadge
import com.efrei.nanoorbit.viewmodel.NanoOrbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    satelliteId: String,
    viewModel: NanoOrbitViewModel,
    onBack: () -> Unit
) {
    val satellite = MockData.getSatelliteById(satelliteId)
    val orbite = MockData.getOrbiteById(satellite.idOrbite)

    val embarquements = listOf(
        Embarquement("SAT-001", "INS-CAM-01", java.time.LocalDate.of(2022,3,15), EtatFonctionnement.NOMINAL),
        Embarquement("SAT-001", "INS-IR-01",  java.time.LocalDate.of(2022,3,15), EtatFonctionnement.NOMINAL),
        Embarquement("SAT-002", "INS-CAM-01", java.time.LocalDate.of(2022,3,15), EtatFonctionnement.NOMINAL),
        Embarquement("SAT-003", "INS-CAM-01", java.time.LocalDate.of(2023,6,10), EtatFonctionnement.NOMINAL),
        Embarquement("SAT-003", "INS-SPEC-01",java.time.LocalDate.of(2023,6,10), EtatFonctionnement.NOMINAL),
        Embarquement("SAT-004", "INS-IR-01",  java.time.LocalDate.of(2023,6,10), EtatFonctionnement.DEGRADE),
        Embarquement("SAT-005", "INS-AIS-01", java.time.LocalDate.of(2021,11,20),EtatFonctionnement.HORS_SERVICE),
    ).filter { it.idSatellite == satelliteId }

    val participations = listOf(
        Participation("SAT-001", "MSN-ARC-2023",   "Imageur principal"),
        Participation("SAT-002", "MSN-ARC-2023",   "Imageur secondaire"),
        Participation("SAT-003", "MSN-ARC-2023",   "Satellite de relais"),
        Participation("SAT-001", "MSN-DEF-2022",   "Imageur principal"),
        Participation("SAT-005", "MSN-DEF-2022",   "Imageur secondaire"),
        Participation("SAT-003", "MSN-COAST-2024", "Imageur principal"),
        Participation("SAT-004", "MSN-COAST-2024", "Satellite de secours"),
    ).filter { it.idSatellite == satelliteId }

    var showDialog by remember { mutableStateOf(false) }
    var anomalieTexte by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Signaler une anomalie") },
            text = {
                Column {
                    Text(
                        text = "Satellite : ${satellite.nomSatellite}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = anomalieTexte,
                        onValueChange = { anomalieTexte = it },
                        placeholder = { Text("Décrivez l'anomalie…") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false; anomalieTexte = "" },
                    enabled = anomalieTexte.isNotBlank()
                ) { Text("Envoyer") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(satellite.nomSatellite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Statut", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(statut = satellite.statut)
                            Text(satellite.formatCubesat.label, fontSize = 13.sp)
                            Text("${orbite.typeOrbite.label} · ${orbite.altitude} km", fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Télémétrie", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        TelemetrieRow("Masse", "${satellite.masse ?: "—"} kg")
                        TelemetrieRow("Batterie", "${satellite.capaciteBatterie} Wh")
                        TelemetrieRow("Durée de vie", "${satellite.dureeViePrevue} mois")
                        TelemetrieRow("Lancement", satellite.dateLancement?.toString() ?: "—")
                        TelemetrieRow("Altitude", "${orbite.altitude} km")
                        TelemetrieRow("Période orbitale", "${orbite.periodeOrbitale} min")
                        Spacer(Modifier.height(8.dp))
                        Text("Niveau batterie", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        val ratio = (satellite.capaciteBatterie / 80.0).coerceIn(0.0, 1.0)
                        LinearProgressIndicator(
                            progress = { ratio.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Instruments (${embarquements.size})",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        embarquements.forEach { emb ->
                            val instrument = MockData.instruments
                                .firstOrNull { it.refInstrument == emb.refInstrument }
                            instrument?.let {
                                InstrumentItem(
                                    instrument = it,
                                    etatFonctionnement = emb.etatFonctionnement
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Missions (${participations.size})",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        participations.forEach { part ->
                            val mission = MockData.missions
                                .firstOrNull { it.idMission == part.idMission }
                            mission?.let {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(it.nomMission, fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium)
                                        Text(part.roleSatellite, fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (it.statutMission == StatutMission.ACTIVE)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = it.statutMission.label,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Signaler une anomalie")
                }
            }
        }
    }
}

@Composable
fun TelemetrieRow(label: String, valeur: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valeur, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}