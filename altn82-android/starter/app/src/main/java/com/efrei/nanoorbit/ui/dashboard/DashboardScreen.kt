package com.efrei.nanoorbit.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.StatutSatellite
import com.efrei.nanoorbit.ui.components.SatelliteCard
import com.efrei.nanoorbit.viewmodel.NanoOrbitViewModel

// Q1 : LazyColumn ne compose que les items visibles a l'ecran.
// Avec Column, Android composerait tous les satellites simultanement,
// meme hors ecran -> probleme de memoire et de rendu a grande echelle.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NanoOrbitViewModel = viewModel(),
    onSatelliteClick: (String) -> Unit = {}
) {
    // Observation des StateFlow: se recompose automatiquement quand
    // les valeurs changent dans le ViewModel
    val satellites by viewModel.satellites.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatut by viewModel.selectedStatut.collectAsStateWithLifecycle()

    val all = MockData.satellites
    val nbOperationnels = all.count { it.statut == StatutSatellite.OPERATIONNEL }
    val nbEnVeille = all.count { it.statut == StatutSatellite.EN_VEILLE }
    val nbDefaillants = all.count { it.statut == StatutSatellite.DEFAILLANT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🛰 NanoOrbit Ground Control",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Bouton refresh
                    IconButton(onClick = { viewModel.refreshSatellites() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraichir")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // Stats 
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatColumn(nbOperationnels, "Operationnels", Color(0xFF2E7D32), Modifier.weight(1f))
                        VerticalDivider(Modifier.height(36.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        StatColumn(nbEnVeille, "En veille", Color(0xFFF57C00), Modifier.weight(1f))
                        VerticalDivider(Modifier.height(36.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        StatColumn(nbDefaillants, "Defaillants", Color(0xFFC62828), Modifier.weight(1f))
                    }
                }
            }

            // Barre de recherche
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Rechercher un satellite ou orbite...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Filtres
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatut == null,
                            onClick = { viewModel.onStatutFilterChange(null) },
                            label = { Text("Tous") }
                        )
                    }
                    items(StatutSatellite.entries) { statut ->
                        FilterChip(
                            selected = selectedStatut == statut,
                            onClick = {
                                viewModel.onStatutFilterChange(
                                    if (selectedStatut == statut) null else statut
                                )
                            },
                            label = { Text(statut.label) }
                        )
                    }
                }
            }

            // En-tete de section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SATELLITES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${satellites.size} resultat(s)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            // État Chargement
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            }

            // Erreur
            errorMessage?.let { msg ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp
                            )
                            TextButton(onClick = { viewModel.refreshSatellites() }) {
                                Text("Reessayer")
                            }
                        }
                    }
                }
            }

            // Etat vide
            if (!isLoading && satellites.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Aucun satellite trouve", fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Essayez de modifier vos filtres",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Liste satellites
            if (!isLoading) {
                items(items = satellites, key = { it.idSatellite }) { satellite ->
                    val orbite = MockData.getOrbiteById(satellite.idOrbite)
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        SatelliteCard(
                            satellite = satellite,
                            orbite = orbite,
                            onClick = {
                                // Q3 : Meme logique que le trigger T1 Oracle qui interdit
                                // toute nouvelle fenetre de comm pour un satellite DESORBITE.
                                // L'app valide cote client ; la base garantit cote serveur.
                                if (satellite.statut != StatutSatellite.DESORBITE) {
                                    onSatelliteClick(satellite.idSatellite)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboardScreen() {
    MaterialTheme {
        DashboardScreen()
    }
}