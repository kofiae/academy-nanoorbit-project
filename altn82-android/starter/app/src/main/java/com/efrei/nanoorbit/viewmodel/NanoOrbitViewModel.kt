package com.efrei.nanoorbit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efrei.nanoorbit.data.mock.MockData
import com.efrei.nanoorbit.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class NanoOrbitViewModel : ViewModel() {

    // ── États internes (modifiables uniquement dans le ViewModel) ─────────────
    private val _allSatellites = MutableStateFlow<List<Satellite>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatut = MutableStateFlow<StatutSatellite?>(null)

    // ── États publics (lecture seule pour l'UI) ───────────────────────────────
    val isLoading: StateFlow<Boolean> = _isLoading
    val errorMessage: StateFlow<String?> = _errorMessage
    val searchQuery: StateFlow<String> = _searchQuery
    val selectedStatut: StateFlow<StatutSatellite?> = _selectedStatut

    // ── Liste filtrée — calculée automatiquement quand searchQuery
    //    ou selectedStatut changent (combine = écoute les 2 flows) ─────────────
    val satellites: StateFlow<List<Satellite>> = combine(
        _allSatellites,
        _searchQuery,
        _selectedStatut
    ) { all, query, statut ->
        all.filter { sat ->
            val orbite = MockData.getOrbiteById(sat.idOrbite)
            val matchSearch = query.isBlank() ||
                    sat.nomSatellite.contains(query, ignoreCase = true) ||
                    orbite.typeOrbite.label.contains(query, ignoreCase = true)
            val matchStatut = statut == null || sat.statut == statut
            matchSearch && matchStatut
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ── Init : chargement automatique au démarrage ────────────────────────────
    init {
        loadSatellites()
    }

    // ── Fonctions publiques appelées par l'UI ─────────────────────────────────

    fun loadSatellites() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                // Simulation latence réseau (sera remplacé par Retrofit en Phase 3)
                delay(500)
                _allSatellites.value = MockData.satellites
            } catch (e: Exception) {
                _errorMessage.value = "Erreur de chargement : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatutFilterChange(statut: StatutSatellite?) {
        _selectedStatut.value = statut
    }

    fun refreshSatellites() {
        _allSatellites.value = emptyList()
        loadSatellites()
    }
}