package com.efrei.nanoorbit.ui.planning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.*
import com.efrei.nanoorbit.ui.components.FenetreCard
import com.efrei.nanoorbit.viewmodel.NanoOrbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(viewModel: NanoOrbitViewModel) {

    var stationSelectionnee by remember { mutableStateOf<StationSol?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val fenetresFiltrees = remember(stationSelectionnee) {
        if (stationSelectionnee == null) MockData.fenetres
        else MockData.getFenetresByStation(stationSelectionnee!!.codeStation)
    }.sortedBy { it.datetimeDebut }

    val dureeTotale = fenetresFiltrees.sumOf { it.dureeSecondes }
    val volumeTotal = fenetresFiltrees.mapNotNull { it.volumeDonnees }.sum()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planning Communications", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Sélecteur de station
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Filtrer par station", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = stationSelectionnee?.nomStation ?: "Toutes les stations",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Toutes les stations") },
                            onClick = { stationSelectionnee = null; dropdownExpanded = false }
                        )
                        MockData.stations.forEach { station ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(station.nomStation)
                                        if (station.statut == StatutStation.MAINTENANCE) {
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.errorContainer
                                            ) {
                                                Text(
                                                    "Maintenance",
                                                    modifier = Modifier.padding(
                                                        horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = { stationSelectionnee = station; dropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Résumé
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${fenetresFiltrees.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("fenêtres", fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${dureeTotale / 60}min", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("durée totale", fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (volumeTotal > 0) "${volumeTotal.toInt()} Mo" else "—",
                            fontWeight = FontWeight.Bold, fontSize = 20.sp
                        )
                        Text("volume total", fontSize = 11.sp)
                    }
                }
            }

            // Warning Maintenance — miroir trigger T1 Oracle RG-G03
            if (stationSelectionnee?.statut == StatutStation.MAINTENANCE) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠ Station en Maintenance — aucune nouvelle fenêtre planifiable (RG-G03)",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Liste fenêtres
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = fenetresFiltrees, key = { it.idFenetre }) { fenetre ->
                    val station = MockData.getStationById(fenetre.codeStation)
                    val satellite = MockData.getSatelliteById(fenetre.idSatellite)
                    Column {
                        Text(
                            text = satellite.nomSatellite,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                        FenetreCard(fenetre = fenetre, nomStation = station.nomStation)
                    }
                }
            }
        }
    }
}