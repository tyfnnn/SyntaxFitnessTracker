# SyntaxFitness 🏃‍♂️

Eine moderne Android-Lauf-App, entwickelt mit Jetpack Compose, die GPS-basierte Laufverfolgung mit elegantem Design und intelligenten Funktionen kombiniert.

## 🎯 Projektübersicht

SyntaxFitness ist eine umfassende Lauf-App, die speziell als Übungsprojekt für fortgeschrittene Android-Konzepte entwickelt wurde:

- **Berechtigungen**: Dynamische GPS- und Benachrichtigungsberechtigungen
- **Notifications**: Intelligente Lauf-Updates und Erinnerungen
- **Intents & IntentFilter**: Teilen von Laufdaten mit anderen Apps
- **WorkManager**: Automatisierte Hintergrundaufgaben und Erinnerungen

## ✨ Features

### 🏃‍♂️ Kernfunktionen
- **GPS-Laufverfolgung**: Präzise Aufzeichnung von Start- und Endpositionen
- **Echtzeit-Distanzberechnung**: Haversine-Formel für exakte Messungen
- **Lauf-Historie**: Persistente Speicherung aller Läufe mit Room Database
- **Detaillierte Statistiken**: Distanz, Dauer, Geschwindigkeit und Pace

### 🎨 Modern UI/UX
- **Glassmorphism Design**: Moderne, halbtransparente UI-Elemente
- **Animierte Hintergründe**: Dynamische Mesh-Gradienten mit Partikelsystemen
- **Smooth Animations**: Flüssige Übergänge und Mikrointeraktionen
- **Swipe-to-Delete**: Intuitive Lauf-Verwaltung in der Historie

### 📱 Smart Features
- **Intelligente Berechtigungen**: Kontextuelle Dialoge mit Begründungen
- **Share-Funktionalität**: Teilen von Laufdaten als Text oder generierte Bilder
- **Adaptive Benachrichtigungen**: Systemabhängige Notification-Verwaltung
- **Persistente Lauf-Wiederherstellung**: Automatische Wiederherstellung bei App-Neustart

## 🛠️ Technische Architektur

### Core Technologies
- **Jetpack Compose**: Moderne, deklarative UI
- **Kotlin Coroutines**: Asynchrone Programmierung
- **Room Database**: Lokale Datenpersistierung
- **Koin**: Dependency Injection
- **Navigation Compose**: App-Navigation

### Android-spezifische Konzepte

#### 🔐 Berechtigungen (Permissions)
```kotlin
// Dynamische Berechtigungsanfragen mit ActivityResultContract
val requestPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    viewModel.updateLocationPermission(fineLocationGranted, coarseLocationGranted)
}
```

**Implementierte Berechtigungen:**
- `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: GPS-Tracking
- `POST_NOTIFICATIONS` (Android 13+): Push-Benachrichtigungen
- `WRITE_EXTERNAL_STORAGE`: Für Bild-Sharing (Legacy-Support)

#### 🔔 Notifications
```kotlin
class RunNotificationService(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "run_notifications"
        private const val NOTIFICATION_ID = 1001
    }
    
    private fun createRunNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lauf Benachrichtigungen",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

**Notification Features:**
- Lauf-Start/Stopp Benachrichtigungen
- Automatische Erinnerungen nach App-Schließung
- Adaptive Kanäle für verschiedene Android-Versionen

#### 📤 Intents & IntentFilter
```kotlin
// Share Intent mit FileProvider für sichere Dateifreigabe
suspend fun shareRun(context: Context, run: RunEntity, includeImage: Boolean = true): Intent {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = if (includeImage) "image/*" else "text/plain"
    }
    
    if (includeImage) {
        val imageUri = createShareableImage(context, run)
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    
    return Intent.createChooser(shareIntent, "Lauf teilen")
}
```

**Intent Implementierungen:**
- **ACTION_SEND**: Sharing von Laufdaten
- **FileProvider**: Sichere Dateifreigabe für generierte Bilder
- **System Settings**: Direkte Weiterleitung zu App-Einstellungen
- **Chooser Intent**: Benutzerfreundliche App-Auswahl

#### ⚙️ WorkManager
```kotlin
class AfterRunWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val notificationService = RunNotificationService(applicationContext)
            notificationService.showRunInfoNotification("Wie war dein Lauf heute? 🏃‍♂️")
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }
}
```

**WorkManager Features:**
- Verzögerte Erinnerungs-Notifications
- Hintergrundaufgaben nach App-Schließung
- Robuste Task-Verwaltung mit Retry-Mechanismen

## 📁 Projektstruktur

```
app/src/main/java/com/example/syntaxfitness/
├── data/
│   ├── local/
│   │   ├── dao/           # Room Database Access Objects
│   │   ├── database/      # Database & Converters
│   │   ├── entity/        # Database Entities
│   │   └── repository/    # Repository Pattern
│   └── model/             # Data Models
├── di/                    # Dependency Injection (Koin)
├── navigation/            # Navigation Logic
├── service/               # Background Services & Notifications
├── ui/
│   ├── running/
│   │   ├── component/     # Reusable UI Components
│   │   ├── screen/        # Screen Composables
│   │   └── viewmodel/     # ViewModels
│   ├── settings/          # Settings Screen
│   └── theme/             # App Theming
├── utils/                 # Utility Classes
└── work/                  # WorkManager Workers
```

## 🚀 Setup & Installation

### Voraussetzungen
- Android Studio Iguana oder neuer
- Android SDK 26+ (Minimum)
- Android SDK 35 (Target)
- Kotlin 2.0.21+

### Installation
1. Repository klonen:
```bash
git clone [repository-url]
cd SyntaxFitness
```

2. In Android Studio öffnen und Gradle sync durchführen

3. App auf Gerät/Emulator mit GPS-Funktionalität installieren

### 🔧 Konfiguration

#### FileProvider Setup
Die App nutzt FileProvider für sicheres Teilen von generierten Bildern:

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

#### Notification Channels
Automatische Erstellung von Notification Channels für verschiedene Android-Versionen:

```kotlin
// Für Android O+ (API 26+)
private fun createRunNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
}
```

## 📱 Benutzerführung

### Erste Nutzung
1. **Berechtigungen erteilen**: GPS und Notifications für vollständige Funktionalität
2. **Ersten Lauf starten**: GPS-Position wird automatisch erfasst
3. **Lauf beenden**: Distanz wird berechnet und gespeichert
4. **Historie einsehen**: Alle Läufe in chronologischer Übersicht

### Advanced Features
- **Swipe-to-Delete**: Läufe in der Historie nach links/rechts wischen
- **Lauf teilen**: Detailansicht → Share-Button → Format wählen
- **Einstellungen**: Berechtigungen verwalten, Statistiken einsehen

## 🎓 Lernziele & Konzepte

### Berechtigungen (Permissions)
- **Runtime Permissions**: Dynamische Berechtigungsanfragen zur Laufzeit
- **Permission Rationale**: Benutzerfreundliche Erklärungen für Berechtigungen
- **Permission States**: Unterscheidung zwischen "nie gefragt" und "verweigert"
- **Settings Integration**: Weiterleitung zu System-Einstellungen

### Notifications
- **Notification Channels**: Strukturierte Benachrichtigungsverwaltung
- **Adaptive Notifications**: Versionsabhängige Implementierung
- **Permission Handling**: POST_NOTIFICATIONS für Android 13+
- **User Experience**: Kontextuelle und hilfreiche Benachrichtigungen

### Intents & IntentFilter
- **Explicit Intents**: Direkte Navigation zu System-Einstellungen
- **Implicit Intents**: Share-Funktionalität mit ACTION_SEND
- **Intent Chooser**: Benutzerfreundliche App-Auswahl
- **FileProvider**: Sichere Dateifreigabe zwischen Apps
- **URI Permissions**: Temporäre Dateizugriffe für andere Apps

### WorkManager
- **Background Processing**: Zuverlässige Hintergrundaufgaben
- **Constraints**: Arbeitsanforderungen und -bedingungen
- **Worker Classes**: Strukturierte Aufgabenimplementierung
- **Scheduling**: Verzögerte und periodische Tasks

## 🛡️ Sicherheit & Best Practices

### Datenschutz
- **Lokale Speicherung**: Alle Daten bleiben auf dem Gerät
- **Minimale Berechtigungen**: Nur notwendige Permissions
- **Transparenz**: Klare Erklärungen für Berechtigungsanfragen

### Performance
- **Coroutines**: Asynchrone Operations ohne UI-Blocking
- **Room Database**: Effiziente lokale Datenpersistierung
- **Memory Management**: Optimierte Bitmap-Verarbeitung für Share-Feature

### Code-Qualität
- **MVVM Architecture**: Saubere Trennung von UI und Business Logic
- **Repository Pattern**: Abstrahierte Datenschicht
- **Dependency Injection**: Testbare und modulare Architektur

## 🔄 Zukünftige Erweiterungen

- **Map Integration**: Kartendarstellung der Laufrouten
- **Workout Tracking**: Erweiterte Fitness-Metriken
- **Social Features**: Teilen mit Fitness-Communities
- **Export Funktionen**: GPX/KML Export für Drittanbieter-Apps
- **Offline Sync**: Cloud-Backup für Laufdaten

## 📄 Lizenz

Dieses Projekt dient ausschließlich Bildungszwecken und zur Demonstration von Android-Entwicklungskonzepten.

---

**Entwickelt als Lernprojekt für fortgeschrittene Android-Konzepte** 🎓
