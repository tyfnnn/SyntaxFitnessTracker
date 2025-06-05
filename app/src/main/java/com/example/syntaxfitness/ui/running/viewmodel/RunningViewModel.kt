package com.example.syntaxfitness.ui.running.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import java.util.Date

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
        val averageDistance: Float = 0f,
        val currentRunDuration: Long = 0L,
        val currentRunId: Long? = null
    )

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    private val _selectedRun = MutableStateFlow<RunEntity?>(null)
    val selectedRun: StateFlow<RunEntity?> = _selectedRun.asStateFlow()

    // Erweiterte Variablen für bessere Persistierung
    private var currentRunStartTime: Date? = null
    private var startLat: Double? = null
    private var startLon: Double? = null
    private var currentRunEntity: RunEntity? = null

    init {
        // Lade alle persistenten Daten beim Start
        loadRunHistory()
        loadStatistics()
        checkForIncompleteRun()
    }

    /**
     * Überprüft beim App-Start, ob ein unvollständiger Lauf existiert
     * und stellt diesen wieder her
     */
    private fun checkForIncompleteRun() {
        viewModelScope.launch {
            try {
                val lastRun = runRepository.getLastRun()
                if (lastRun != null && lastRun.endLatitude == 0.0 && lastRun.endLongitude == 0.0) {
                    // Unvollständiger Lauf gefunden - wiederherstellen
                    Log.i("RunningViewModel", "Unvollständiger Lauf gefunden, stelle wieder her")
                    restoreIncompleteRun(lastRun)
                }
            } catch (e: Exception) {
                Log.e("RunningViewModel", "Fehler beim Überprüfen unvollständiger Läufe", e)
            }
        }
    }

    /**
     * Stellt einen unvollständigen Lauf wieder her
     */
    private fun restoreIncompleteRun(run: RunEntity) {
        currentRunEntity = run
        currentRunStartTime = run.startTime
        startLat = run.startLatitude
        startLon = run.startLongitude

        _uiState.update {
            it.copy(
                isRunning = true,
                startLatitude = CoordinateUtils.formatCoordinate(run.startLatitude),
                startLongitude = CoordinateUtils.formatCoordinate(run.startLongitude),
                endLatitude = "--",
                endLongitude = "--",
                statusMessage = "Lauf läuft... (wiederhergestellt)",
                currentRunId = run.id
            )
        }
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
            _uiState.value.isRunning -> _uiState.value.statusMessage
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
        currentRunStartTime = Date()

        _uiState.update {
            it.copy(
                isRunning = true,
                isGettingLocation = true,
                statusMessage = "GPS-Position wird ermittelt...",
                endLatitude = "--",
                endLongitude = "--",
                currentRunDuration = 0L
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

            // Speichere den Start des Laufs sofort in der Datenbank
            viewModelScope.launch {
                saveRunStart(location.latitude, location.longitude)
            }
        }
    }

    /**
     * Speichert den Start eines Laufs sofort in der Datenbank
     * Dies stellt sicher, dass bei App-Abstürzen oder Unterbrechungen
     * der Lauf nicht verloren geht
     */
    private suspend fun saveRunStart(latitude: Double, longitude: Double) {
        try {
            val currentTime = Date()
            val incompleteRun = RunEntity(
                startLatitude = latitude,
                startLongitude = longitude,
                endLatitude = 0.0, // Markiert als unvollständig
                endLongitude = 0.0, // Markiert als unvollständig
                distance = 0f,
                startTime = currentTime,
                endTime = currentTime, // Temporär
                duration = 0L
            )

            val runId = runRepository.insertRun(incompleteRun)
            currentRunEntity = incompleteRun.copy(id = runId)

            _uiState.update { it.copy(currentRunId = runId) }

            Log.i("RunningViewModel", "Lauf-Start gespeichert mit ID: $runId")
        } catch (e: Exception) {
            Log.e("RunningViewModel", "Fehler beim Speichern des Lauf-Starts", e)
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

            // Vervollständige den Lauf in der Datenbank
            startLat?.let { sLat ->
                startLon?.let { sLon ->
                    viewModelScope.launch {
                        completeRun(sLat, sLon, endLat, endLon)
                    }
                }
            }

            if (_uiState.value.hasNotificationPermission) {
                RunNotificationService(context).showRunInfoNotification("Lauf beendet!")
            }
        }
    }

    /**
     * Vervollständigt einen Lauf in der Datenbank
     */
    private suspend fun completeRun(startLat: Double, startLon: Double, endLat: Double, endLon: Double) {
        try {
            val distance = CoordinateUtils.calculateDistance(startLat, startLon, endLat, endLon)
            val endTime = Date()
            val duration = currentRunStartTime?.let { startTime ->
                endTime.time - startTime.time
            } ?: 0L

            currentRunEntity?.let { existingRun ->
                // Update den bestehenden Lauf
                val completedRun = existingRun.copy(
                    endLatitude = endLat,
                    endLongitude = endLon,
                    distance = distance,
                    endTime = endTime,
                    duration = duration
                )

                runRepository.updateRun(completedRun)
                Log.i("RunningViewModel", "Lauf vervollständigt: ${distance}m in ${duration}ms")

                // Reset current run data
                currentRunEntity = null
                _uiState.update { it.copy(currentRunId = null) }
            } ?: run {
                // Fallback: Erstelle neuen Lauf falls kein bestehender gefunden wurde
                val newRun = RunEntity(
                    startLatitude = startLat,
                    startLongitude = startLon,
                    endLatitude = endLat,
                    endLongitude = endLon,
                    distance = distance,
                    startTime = currentRunStartTime ?: Date(),
                    endTime = endTime,
                    duration = duration
                )
                runRepository.insertRun(newRun)
                Log.i("RunningViewModel", "Neuer Lauf erstellt: ${distance}m")
            }
        } catch (e: Exception) {
            Log.e("RunningViewModel", "Fehler beim Vervollständigen des Laufs", e)
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

    fun deleteRun(run: RunEntity) {
        viewModelScope.launch {
            try {
                runRepository.deleteRun(run)
                Log.i("RunningViewModel", "Lauf gelöscht: ${run.id}")
            } catch (e: Exception) {
                Log.e("RunningViewModel", "Fehler beim Löschen des Laufs", e)
            }
        }
    }

    fun deleteAllRuns() {
        viewModelScope.launch {
            try {
                runRepository.deleteAllRuns()
                Log.i("RunningViewModel", "Alle Läufe gelöscht")
            } catch (e: Exception) {
                Log.e("RunningViewModel", "Fehler beim Löschen aller Läufe", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("RunningViewModel", "ViewModel wird zerstört")
    }

    fun loadRunDetails(runId: Long) {
        viewModelScope.launch {
            try {
                val run = runRepository.getRunById(runId)
                _selectedRun.value = run
            } catch (e: Exception) {
                Log.e("RunningViewModel", "Fehler beim Laden des Laufs mit ID $runId", e)
                _selectedRun.value = null
            }
        }
    }
}