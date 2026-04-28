package com.efrei.nanoorbit.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.efrei.nanoorbit.data.models.StatutSatellite
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.ui.components.SatelliteCard
import com.efrei.nanoorbit.viewmodel.NanoOrbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    // viewModel() injecte automatiquement et survit aux rotations d'écran
    viewModel: NanoOrbitViewModel = viewModel(),
    onSatelliteClick: (String) -> Unit = {}
) {
    // Observation des StateFlow — se recompose automatiquement quand
    // les valeurs changent dans le ViewModel
    val satellites by viewModel.satellites.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatut by viewModel.selectedStatut.collectAsStateWithLifecycle()

    val nbOperationnels = satellites.count {
        it.statut == StatutSatellite.OPERATIONNEL
    }

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
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,

                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Rechercher un satellite ou orbite…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )


            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        onClick = { viewModel.onStatutFilterChange(
                            if (selectedStatut == statut) null else statut
                        )},
                        label = { Text(statut.label) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Compteur
            Text(
                text = "$nbOperationnels/${MockData.satellites.size} opérationnels" +
                        " · ${satellites.size} résultat(s)",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── État chargement ───────────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // État erreur
            errorMessage?.let { msg ->
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
                            Text("Réessayer")
                        }
                    }
                }
            }

            // ── Liste satellites ──────────────────────────────────────────────
            if (!isLoading) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = satellites,
                        key = { it.idSatellite }
                    ) { satellite ->
                        val orbite = MockData.getOrbiteById(satellite.idOrbite)
                        SatelliteCard(
                            satellite = satellite,
                            orbite = orbite,
                            onClick = {
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

@Preview(showBackground = true)
@Composable
fun PreviewDashboardScreen() {
    MaterialTheme {
        DashboardScreen()
    }
}