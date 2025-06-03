package com.example.syntaxfitness.ui.running.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syntaxfitness.data.local.entity.RunEntity
import com.example.syntaxfitness.data.local.repository.RunRepository
import com.example.syntaxfitness.service.RunNotificationService
import com.example.syntaxfitness.utils.CoordinateUtils
import com.example.syntaxfitness.utils.LocationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RunningViewModel(
    private val runRepository: RunRepository
) : ViewModel() {

    data class RunningUiState(
        val isRunning: Boolean = false,
        val startLatitude: String = "--",
        val startLongitude: String = "--",
        val endLatitude: String = "--",
        val endLongitude: String = "--",
        val hasLocationPermission: Boolean = false,
        val hasNotificationPermission: Boolean = false,
        val showPermissionDeniedMessage: Boolean = false,
        val isGettingLocation: Boolean = false,
        val showPermissionRationaleDialog: Boolean = false,
        val showNotificationRationaleDialog: Boolean = false,
        val statusMessage: String = "Berechtigungen erforderlich",
        val runHistory: List<RunEntity> = emptyList(),
        val totalDistance: Float = 0f,
        val totalRuns: Int = 0,
        val averageDistance: Float = 0f
    )

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    // Temporäre Variablen für laufende Läufe
    private var currentRunStartTime: LocalDateTime? = null
    private var startLat: Double? = null
    private var startLon: Double? = null

    init {
        // Lade Laufhistorie und Statistiken
        loadRunHistory()
        loadStatistics()
    }

    private fun loadRunHistory() {
        viewModelScope.launch {
            runRepository.getAllRuns().collect { runs ->
                _uiState.update { it.copy(runHistory = runs) }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            combine(
                runRepository.getTotalDistance(),
                runRepository.getTotalRunCount(),
                runRepository.getAverageDistance()
            ) { totalDist, totalCount, avgDist ->
                Triple(totalDist ?: 0f, totalCount, avgDist ?: 0f)
            }.collect { (totalDist, totalCount, avgDist) ->
                _uiState.update {
                    it.copy(
                        totalDistance = totalDist,
                        totalRuns = totalCount,
                        averageDistance = avgDist
                    )
                }
            }
        }
    }

    fun checkAllPermissions(context: Context) {
        val hasLocation = checkLocationPermissions(context)
        val hasNotification = checkNotificationPermission(context)
        updatePermissionStates(hasLocation, hasNotification)
    }

    private fun checkLocationPermissions(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    private fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun updatePermissionStates(hasLocation: Boolean, hasNotification: Boolean) {
        val statusMessage = when {
            !hasLocation && !hasNotification -> "Standort- und Benachrichtigungsberechtigungen erforderlich"
            !hasLocation -> "Standortberechtigung erforderlich"
            !hasNotification -> "Benachrichtigungsberechtigung empfohlen"
            else -> "Bereit zum Starten"
        }

        _uiState.update {
            it.copy(
                hasLocationPermission = hasLocation,
                hasNotificationPermission = hasNotification,
                showPermissionDeniedMessage = !hasLocation || !hasNotification,
                statusMessage = statusMessage
            )
        }
    }

    fun updateLocationPermission(fineLocationGranted: Boolean, coarseLocationGranted: Boolean) {
        val hasLocation = fineLocationGranted || coarseLocationGranted
        updatePermissionStates(hasLocation, _uiState.value.hasNotificationPermission)
    }

    fun updateNotificationPermission(granted: Boolean) {
        updatePermissionStates(_uiState.value.hasLocationPermission, granted)
    }

    fun setPermissionRationaleDialogVisibility(show: Boolean) {
        _uiState.update { it.copy(showPermissionRationaleDialog = show) }
    }

    fun setNotificationRationaleDialogVisibility(show: Boolean) {
        _uiState.update { it.copy(showNotificationRationaleDialog = show) }
    }

    fun toggleRunning(context: Context) {
        val currentState = _uiState.value

        if (!currentState.hasLocationPermission) {
            return
        }

        if (!currentState.isRunning) {
            startRun(context)
        } else {
            stopRun(context)
        }
    }

    private fun startRun(context: Context) {
        currentRunStartTime = LocalDateTime.now()

        _uiState.update {
            it.copy(
                isRunning = true,
                isGettingLocation = true,
                statusMessage = "GPS-Position wird ermittelt...",
                // Reset display values
                endLatitude = "--",
                endLongitude = "--"
            )
        }

        if (_uiState.value.hasNotificationPermission) {
            RunNotificationService(context).showRunInfoNotification("Lauf gestartet!")
        }

        viewModelScope.launch {
            getLocationForStart(context)
        }
    }

    private fun stopRun(context: Context) {
        _uiState.update {
            it.copy(
                isGettingLocation = true,
                statusMessage = "GPS-Position wird ermittelt..."
            )
        }

        viewModelScope.launch {
            getLocationForEnd(context)
        }
    }

    private fun getLocationForStart(context: Context) {
        LocationUtils.getLocation(context) { location ->
            startLat = location.latitude
            startLon = location.longitude

            val formattedLat = CoordinateUtils.formatCoordinate(location.latitude)
            val formattedLon = CoordinateUtils.formatCoordinate(location.longitude)

            _uiState.update {
                it.copy(
                    startLatitude = formattedLat,
                    startLongitude = formattedLon,
                    isGettingLocation = false,
                    statusMessage = "Lauf läuft..."
                )
            }
        }
    }

    private fun getLocationForEnd(context: Context) {
        LocationUtils.getLocation(context) { location ->
            val endLat = location.latitude
            val endLon = location.longitude
            val formattedLat = CoordinateUtils.formatCoordinate(endLat)
            val formattedLon = CoordinateUtils.formatCoordinate(endLon)

            _uiState.update {
                it.copy(
                    endLatitude = formattedLat,
                    endLongitude = formattedLon,
                    isRunning = false,
                    isGettingLocation = false,
                    statusMessage = "Lauf beendet - Bereit für einen neuen Lauf"
                )
            }

            // Speichere den Lauf in der Datenbank
            startLat?.let { sLat ->
                startLon?.let { sLon ->
                    val distance = CoordinateUtils.calculateDistance(sLat, sLon, endLat, endLon)
                    val endTime = LocalDateTime.now()
                    val duration = java.time.Duration.between(currentRunStartTime, endTime).toMillis()

                    viewModelScope.launch {
                        val newRun = RunEntity(
                            startLatitude = sLat,
                            startLongitude = sLon,
                            endLatitude = endLat,
                            endLongitude = endLon,
                            distance = distance,
                            startTime = currentRunStartTime ?: LocalDateTime.now(),
                            endTime = endTime,
                            duration = duration
                        )
                        runRepository.insertRun(newRun)
                    }
                }
            }

            if (_uiState.value.hasNotificationPermission) {
                RunNotificationService(context).showRunInfoNotification("Lauf beendet!")
            }
        }
    }

    fun calculateDistance(): Float? {
        val currentState = _uiState.value

        val startLatitude = CoordinateUtils.parseCoordinate(currentState.startLatitude)
        val startLongitude = CoordinateUtils.parseCoordinate(currentState.startLongitude)
        val endLatitude = CoordinateUtils.parseCoordinate(currentState.endLatitude)
        val endLongitude = CoordinateUtils.parseCoordinate(currentState.endLongitude)

        return if (startLatitude != null && startLongitude != null &&
            endLatitude != null && endLongitude != null) {
            CoordinateUtils.calculateDistance(startLatitude, startLongitude, endLatitude, endLongitude)
        } else {
            null
        }
    }

    fun deleteRun(run: RunEntity) {
        viewModelScope.launch {
            runRepository.deleteRun(run)
        }
    }

    fun getMissingPermissions(): List<String> {
        val missing = mutableListOf<String>()

        if (!_uiState.value.hasLocationPermission) {
            missing.add(Manifest.permission.ACCESS_FINE_LOCATION)
            missing.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (!_uiState.value.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            missing.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return missing
    }
}