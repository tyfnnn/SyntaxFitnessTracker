package com.example.syntaxfitness.ui.running.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syntaxfitness.utils.CoordinateUtils
import com.example.syntaxfitness.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RunningViewModel : ViewModel() {
    data class RunningUiState(
        val isRunning: Boolean = false,
        val startLatitude: String = "--",
        val startLongitude: String = "--",
        val endLatitude: String = "--",
        val endLongitude: String = "--",
        val hasLocationPermission: Boolean = false,
        val showPermissionDeniedMessage: Boolean = false,
        val isGettingLocation: Boolean = false,
        val showPermissionRationaleDialog: Boolean = false,
        val statusMessage: String = "Standortberechtigung erforderlich"
    )

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    fun updateLocationPermission(
        fineLocationGranted: Boolean,
        coarseLocationGranted: Boolean
    ) {
        val hasPermission = fineLocationGranted || coarseLocationGranted

        _uiState.value = _uiState.value.copy(
            hasLocationPermission = hasPermission,
            showPermissionDeniedMessage = !hasPermission,
            statusMessage = if (hasPermission) "Bereit zum Starten" else "Standortberechtigung erforderlich"
        )
    }

    fun setPermissionRationaleDialogVisibility(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPermissionRationaleDialog = show)
    }

    fun toggleRunning(context: Context) {
        if (!_uiState.value.hasLocationPermission) {
            return
        }

        val currentState = _uiState.value

        if (!currentState.isRunning) {
            startRun(context)
        } else {
            stopRun(context)
        }
    }

    private fun stopRun(context: Context) {
        _uiState.value = _uiState.value.copy(
            isGettingLocation = true,
            statusMessage = "GPS-Position wird ermittelt..."
        )

        viewModelScope.launch {
            getLocationForEnd(context)
        }
    }

    private fun startRun(context: Context) {
        _uiState.value = _uiState.value.copy(
            isRunning = true,
            isGettingLocation = true,
            statusMessage = "GPS-Position wird ermittelt..."
        )

        viewModelScope.launch {
            getLocationForStart(context)
        }
    }

    private suspend fun getLocationForStart(context: Context) {
        LocationUtils.getLocation(context) { location ->
            val formattedLat = CoordinateUtils.formatCoordinate(location.latitude)
            val formattedLon = CoordinateUtils.formatCoordinate(location.longitude)

            _uiState.value = _uiState.value.copy(
                startLatitude = formattedLat,
                startLongitude = formattedLon,
                isGettingLocation = false,
                statusMessage = "Lauf läuft..."
            )
        }
    }

    private suspend fun getLocationForEnd(context: Context) {
        LocationUtils.getLocation(context) { location ->
            // Konsistente Verwendung der CoordinateUtils wie in getLocationForStart
            val formattedLat = CoordinateUtils.formatCoordinate(location.latitude)
            val formattedLon = CoordinateUtils.formatCoordinate(location.longitude)

            _uiState.value = _uiState.value.copy(
                endLatitude = formattedLat,
                endLongitude = formattedLon,
                isRunning = false,
                isGettingLocation = false,
                statusMessage = "Lauf beendet - Bereit für einen neuen Lauf"
            )
        }
    }

    fun calculateDistance(): Float? {
        val currentState = _uiState.value

        // Sichere Koordinaten-Parsing mit der Utility-Klasse
        val startLat = CoordinateUtils.parseCoordinate(currentState.startLatitude)
        val startLon = CoordinateUtils.parseCoordinate(currentState.startLongitude)
        val endLat = CoordinateUtils.parseCoordinate(currentState.endLatitude)
        val endLon = CoordinateUtils.parseCoordinate(currentState.endLongitude)

        // Defensive Programmierung
        return if (startLat != null && startLon != null && endLat != null && endLon != null) {
            CoordinateUtils.calculateDistance(startLat, startLon, endLat, endLon)
        } else {
            null
        }
    }
}