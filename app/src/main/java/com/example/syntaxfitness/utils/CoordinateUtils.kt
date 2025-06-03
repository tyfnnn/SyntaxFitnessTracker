package com.example.syntaxfitness.utils

import android.location.Location
import kotlin.math.*

object CoordinateUtils {

    /**
     * Formatiert eine GPS-Koordinate für die Benutzeranzeige
     *
     * @param coordinate Die zu formatierende Koordinate (Breitengrad oder Längengrad)
     * @param decimalPlaces Anzahl der Dezimalstellen (Standard: 4)
     * @return Formatierte Koordinate als String
     */
    fun formatCoordinate(coordinate: Double, decimalPlaces: Int = 4): String {
        return String.format("%.${decimalPlaces}f", coordinate)
    }

    /**
     * Berechnet die Luftliniendistanz zwischen zwei GPS-Punkten
     *
     * Diese Methode nutzt die Haversine-Formel für präzise Berechnungen
     * auf der gekrümmten Erdoberfläche.
     *
     * @param startLat Breitengrad des Startpunkts
     * @param startLon Längengrad des Startpunkts
     * @param endLat Breitengrad des Endpunkts
     * @param endLon Längengrad des Endpunkts
     * @return Distanz in Metern
     */
    fun calculateDistance(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Float {
        // Android's eingebaute Methode nutzen für Genauigkeit
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLon, endLat, endLon, results)
        return results[0]
    }

    /**
     * Alternative Distanzberechnung mit der Haversine-Formel
     *
     * Diese Implementierung zeigt die mathematischen Details und könnte
     * für erweiterte Berechnungen nützlich sein.
     */
    fun calculateDistanceHaversine(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(endLat - startLat)
        val dLon = Math.toRadians(endLon - startLon)

        val lat1Rad = Math.toRadians(startLat)
        val lat2Rad = Math.toRadians(endLat)

        val a = sin(dLat / 2).pow(2) +
                sin(dLon / 2).pow(2) * cos(lat1Rad) * cos(lat2Rad)
        val c = 2 * asin(sqrt(a))

        return earthRadiusKm * c * 1000 // Umwandlung in Meter
    }

    /**
     * Überprüft, ob Koordinaten gültig sind
     *
     * @param latitude Breitengrad (-90 bis +90)
     * @param longitude Längengrad (-180 bis +180)
     * @return true wenn beide Koordinaten im gültigen Bereich liegen
     */
    fun areCoordinatesValid(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * Formatiert Koordinaten für verschiedene Anzeigetypen
     */
    enum class CoordinateFormat {
        DECIMAL,        // 52.1234
        DEGREES_MINUTES, // 52° 7.404'
        DEGREES_MINUTES_SECONDS // 52° 7' 24.24"
    }

    /**
     * Erweiterte Koordinatenformatierung mit verschiedenen Formaten
     *
     * @param coordinate Die zu formatierende Koordinate
     * @param format Das gewünschte Anzeigeformat
     * @param isLatitude true für Breitengrad (N/S), false für Längengrad (E/W)
     * @return Formatierte Koordinate als String
     */
    fun formatCoordinateAdvanced(
        coordinate: Double,
        format: CoordinateFormat = CoordinateFormat.DECIMAL,
        isLatitude: Boolean = true
    ): String {
        return when (format) {
            CoordinateFormat.DECIMAL -> {
                val direction = if (isLatitude) {
                    if (coordinate >= 0) "N" else "S"
                } else {
                    if (coordinate >= 0) "E" else "W"
                }
                "${formatCoordinate(abs(coordinate))}° $direction"
            }

            CoordinateFormat.DEGREES_MINUTES -> {
                val absCoordinate = abs(coordinate)
                val degrees = absCoordinate.toInt()
                val minutes = (absCoordinate - degrees) * 60
                val direction = if (isLatitude) {
                    if (coordinate >= 0) "N" else "S"
                } else {
                    if (coordinate >= 0) "E" else "W"
                }
                "$degrees° ${formatCoordinate(minutes, 3)}' $direction"
            }

            CoordinateFormat.DEGREES_MINUTES_SECONDS -> {
                val absCoordinate = abs(coordinate)
                val degrees = absCoordinate.toInt()
                val minutes = ((absCoordinate - degrees) * 60).toInt()
                val seconds = ((absCoordinate - degrees) * 60 - minutes) * 60
                val direction = if (isLatitude) {
                    if (coordinate >= 0) "N" else "S"
                } else {
                    if (coordinate >= 0) "E" else "W"
                }
                "$degrees° $minutes' ${formatCoordinate(seconds, 2)}\" $direction"
            }
        }
    }

    /**
     * Hilfsfunktion zur sicheren Koordinaten-Parsing
     *
     * @param coordinateString String-Darstellung einer Koordinate
     * @return Koordinate als Double oder null bei Parsing-Fehlern
     */
    fun parseCoordinate(coordinateString: String): Double? {
        return try {
            coordinateString.replace("--", "").toDoubleOrNull()
        } catch (e: NumberFormatException) {
            null
        }
    }
}