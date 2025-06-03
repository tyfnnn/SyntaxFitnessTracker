package com.example.syntaxfitness.ui.running.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syntaxfitness.service.RunNotificationService
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
        val hasNotificationPermission: Boolean = false,
        val showPermissionDeniedMessage: Boolean = false,
        val isGettingLocation: Boolean = false,
        val showPermissionRationaleDialog: Boolean = false,
        val showNotificationRationaleDialog: Boolean = false,
        val statusMessage: String = "Berechtigungen erforderlich"
    )

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    // Notification Service - wird für jede Benachrichtigung neu erstellt
    private fun getNotificationService(context: Context) = RunNotificationService(context)

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
            // für ältere System
            true
        }
    }

    private fun updatePermissionStates(hasLocation: Boolean, hasNotification: Boolean) {
        val statusMessage = when {
            !hasLocation && !hasNotification -> "Standort- und Benachrichtigungsberechtigungen erforderlich"
            !hasLocation -> "Standortberechtigung erforderlich"
            !hasNotification -> "Benachrichtigungsberechtigung erforderlich"
            else -> "Bereit zum Starten"
        }

        _uiState.value = _uiState.value.copy(
            hasLocationPermission = hasLocation,
            hasNotificationPermission = hasNotification,
            showPermissionDeniedMessage = !hasLocation || !hasNotification,
            statusMessage = statusMessage
        )
    }

    /**
     * Wird von der UI aufgerufen, wenn Standortberechtigungen erteilt/verweigert werden
     */
    fun updateLocationPermission(fineLocationGranted: Boolean, coarseLocationGranted: Boolean) {
        val hasLocation = fineLocationGranted || coarseLocationGranted
        updatePermissionStates(hasLocation, _uiState.value.hasNotificationPermission)
    }

    /**
     * Wird von der UI aufgerufen, wenn Notification-Berechtigung erteilt/verweigert wird
     */
    fun updateNotificationPermission(granted: Boolean) {
        updatePermissionStates(_uiState.value.hasLocationPermission, granted)
    }

    /**
     * Steuert die Sichtbarkeit des Standort-Rationale-Dialogs
     */
    fun setPermissionRationaleDialogVisibility(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPermissionRationaleDialog = show)
    }

    /**
     * Steuert die Sichtbarkeit des Notification-Rationale-Dialogs
     */
    fun setNotificationRationaleDialogVisibility(show: Boolean) {
        _uiState.value = _uiState.value.copy(showNotificationRationaleDialog = show)
    }

    /**
     * Hauptfunktion zum Starten/Stoppen des Laufs
     * Prüft vorher alle erforderlichen Berechtigungen
     */
    fun toggleRunning(context: Context) {
        val currentState = _uiState.value

        // Mindestens Standortberechtigung ist erforderlich
        if (!currentState.hasLocationPermission) {
            return
        }

        if (!currentState.isRunning) {
            startRun(context)
        } else {
            stopRun(context)
        }
    }

    /**
     * Startet einen neuen Lauf
     * Benachrichtigungen werden nur gesendet, wenn die Berechtigung vorhanden ist
     */
    private fun startRun(context: Context) {
        _uiState.value = _uiState.value.copy(
            isRunning = true,
            isGettingLocation = true,
            statusMessage = "GPS-Position wird ermittelt..."
        )

        // Notification nur senden, wenn Berechtigung vorhanden ist
        if (_uiState.value.hasNotificationPermission) {
            getNotificationService(context).showRunInfoNotification("Lauf gestartet!")
        }

        viewModelScope.launch {
            getLocationForStart(context)
        }
    }

    /**
     * Beendet den aktuellen Lauf
     */
    private fun stopRun(context: Context) {
        _uiState.value = _uiState.value.copy(
            isGettingLocation = true,
            statusMessage = "GPS-Position wird ermittelt..."
        )

        viewModelScope.launch {
            getLocationForEnd(context)
        }
    }

    /**
     * Ermittelt die Startposition für den Lauf
     */
    private fun getLocationForStart(context: Context) {
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

    /**
     * Ermittelt die Endposition und beendet den Lauf
     */
    private fun getLocationForEnd(context: Context) {
        LocationUtils.getLocation(context) { location ->
            val formattedLat = CoordinateUtils.formatCoordinate(location.latitude)
            val formattedLon = CoordinateUtils.formatCoordinate(location.longitude)

            _uiState.value = _uiState.value.copy(
                endLatitude = formattedLat,
                endLongitude = formattedLon,
                isRunning = false,
                isGettingLocation = false,
                statusMessage = "Lauf beendet - Bereit für einen neuen Lauf"
            )

            // Notification nur senden, wenn Berechtigung vorhanden ist
            if (_uiState.value.hasNotificationPermission) {
                getNotificationService(context).showRunInfoNotification("Lauf beendet!")
            }
        }
    }

    /**
     * Berechnet die Distanz zwischen Start- und Endpunkt
     * Gibt null zurück, wenn nicht alle Koordinaten verfügbar sind
     */
    fun calculateDistance(): Float? {
        val currentState = _uiState.value

        val startLat = CoordinateUtils.parseCoordinate(currentState.startLatitude)
        val startLon = CoordinateUtils.parseCoordinate(currentState.startLongitude)
        val endLat = CoordinateUtils.parseCoordinate(currentState.endLatitude)
        val endLon = CoordinateUtils.parseCoordinate(currentState.endLongitude)

        return if (startLat != null && startLon != null && endLat != null && endLon != null) {
            CoordinateUtils.calculateDistance(startLat, startLon, endLat, endLon)
        } else {
            null
        }
    }

    /**
     * Ermittelt, welche Berechtigungen noch benötigt werden
     * Hilfsfunktion für die UI, um die richtigen Berechtigungsanfragen zu stellen
     */
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