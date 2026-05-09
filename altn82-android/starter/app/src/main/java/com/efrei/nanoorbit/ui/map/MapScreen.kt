package com.efrei.nanoorbit.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.StatutStation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var stationSelectionnee by remember {
        mutableStateOf<com.efrei.nanoorbit.data.models.StationSol?>(null)
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stations au sol", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            // Carte OSM
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(3.0)
                        controller.setCenter(GeoPoint(30.0, 20.0))

                        MockData.stations.forEach { station ->
                            val marker = Marker(this)
                            marker.position = GeoPoint(station.latitude, station.longitude)
                            marker.title = station.nomStation
                            marker.snippet = when (station.statut) {
                                StatutStation.ACTIVE ->
                                    "Active | ${station.bandeFrequence} | ${station.debitMax} Mbps"
                                StatutStation.MAINTENANCE ->
                                    "Maintenance | ${station.bandeFrequence} | ${station.debitMax} Mbps"
                                StatutStation.INACTIVE ->
                                    "Inactive | ${station.bandeFrequence} | ${station.debitMax} Mbps"
                            }
                            marker.setOnMarkerClickListener { m, _ ->
                                stationSelectionnee = station
                                m.showInfoWindow()
                                true
                            }
                            overlays.add(marker)
                        }
                        invalidate()
                    }
                }
            )

            // Légende
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Légende", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    LegendItem("Active", Color(0xFF2E7D32))
                    LegendItem("Maintenance", Color(0xFFF57C00))
                    LegendItem("Inactive", Color(0xFF757575))
                }
            }

            // Infobulle station
            stationSelectionnee?.let { station ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(station.nomStation, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            TextButton(onClick = { stationSelectionnee = null }) {
                                Text("✕")
                            }
                        }
                        InfoRow("Statut", station.statut.label)
                        InfoRow("Bande", station.bandeFrequence ?: "—")
                        InfoRow("Débit max", "${station.debitMax ?: "—"} Mbps")
                        InfoRow("Antenne", "${station.diametreAntenne ?: "—"} m")
                        InfoRow("Coordonnées", "${station.latitude}, ${station.longitude}")
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = color
        ) {}
        Text(label, fontSize = 11.sp, color = color)
    }
}

@Composable
fun InfoRow(label: String, valeur: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valeur, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}