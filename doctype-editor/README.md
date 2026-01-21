# DocType Details Editor

Eine Spring Boot Webanwendung zum Bearbeiten der DocTypeDetails-Tabelle aus einer MSSQL-Datenbank.

## Funktionen

- ✅ Darstellung aller Datensätze aus der DocTypeDetails-Tabelle
- ✅ Alle Spalten sind editierbar (außer ID)
- ✅ Auswahlliste mit allen verfügbaren Werten beim Bearbeiten
- ✅ Unscharfe Suche (Fuzzy Search) in der Auswahlliste
- ✅ Änderungsverfolgung mit visueller Kennzeichnung
- ✅ Bestätigungsseite mit Zusammenfassung aller Änderungen
- ✅ Checkbox-Bestätigung vor dem Übernehmen
- ✅ Button-Bestätigung zum finalen Übernehmen der Änderungen

## Voraussetzungen

- Java 17 oder höher
- Maven 3.6 oder höher
- MSSQL Server mit der DocTypeDetails-Tabelle

## Installation & Konfiguration

### 1. Datenbank-Konfiguration

Öffnen Sie die Datei `src/main/resources/application.properties` und passen Sie die Datenbankverbindung an:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=YourDatabase;encrypt=true;trustServerCertificate=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Ersetzen Sie:
- `localhost:1433` - mit Ihrer MSSQL Server Adresse und Port
- `YourDatabase` - mit dem Namen Ihrer Datenbank
- `your_username` - mit Ihrem Datenbank-Benutzernamen
- `your_password` - mit Ihrem Datenbank-Passwort

### 2. Projekt bauen

```bash
cd doctype-editor
mvn clean install
```

### 3. Anwendung starten

```bash
mvn spring-boot:run
```

Oder alternativ die generierte JAR-Datei ausführen:

```bash
java -jar target/doctype-editor-1.0.0.jar
```

### 4. Anwendung öffnen

Öffnen Sie Ihren Browser und navigieren Sie zu:

```
http://localhost:8080
```

## Verwendung

### Daten bearbeiten

1. Klicken Sie auf eine beliebige Zelle in der Tabelle (außer ID)
2. Es öffnet sich eine Auswahlliste mit allen verfügbaren Werten für dieses Feld
3. Nutzen Sie das Suchfeld für eine unscharfe Suche
4. Wählen Sie einen Wert aus der Liste oder navigieren Sie mit Pfeiltasten
5. Drücken Sie Enter oder klicken Sie auf einen Wert zum Übernehmen
6. Geänderte Zellen werden blau markiert und mit einem Punkt gekennzeichnet

### Änderungen bestätigen und übernehmen

1. Klicken Sie auf den Button "Änderungen bestätigen" (zeigt die Anzahl der Änderungen)
2. Sie werden zur Bestätigungsseite weitergeleitet
3. Überprüfen Sie alle Änderungen in der Zusammenfassung
4. Aktivieren Sie die Checkbox "Ich bestätige..."
5. Klicken Sie auf "Änderungen übernehmen"
6. Bei Erfolg werden Sie automatisch zurück zur Hauptseite geleitet

### Tastaturnavigation in der Auswahlliste

- **↓** (Pfeil runter) - Nächster Eintrag
- **↑** (Pfeil hoch) - Vorheriger Eintrag
- **Enter** - Ausgewählten Wert übernehmen
- **Escape** - Auswahlliste schließen

## Projektstruktur

```
doctype-editor/
├── src/
│   ├── main/
│   │   ├── java/com/example/doctypeeditor/
│   │   │   ├── entity/
│   │   │   │   └── DocTypeDetails.java          # JPA Entity
│   │   │   ├── repository/
│   │   │   │   └── DocTypeDetailsRepository.java # Data Access Layer
│   │   │   ├── service/
│   │   │   │   └── DocTypeDetailsService.java    # Business Logic
│   │   │   ├── controller/
│   │   │   │   └── DocTypeDetailsController.java # REST & View Controller
│   │   │   ├── dto/
│   │   │   │   ├── ChangeRecord.java             # DTO für Änderungen
│   │   │   │   └── ChangeBatch.java              # DTO für Änderungs-Batch
│   │   │   └── DoctypeEditorApplication.java     # Main Application
│   │   └── resources/
│   │       ├── application.properties             # Konfiguration
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── styles.css                # Stylesheet
│   │       │   └── js/
│   │       │       ├── app.js                    # Haupt-JavaScript
│   │       │       └── confirm.js                # Bestätigungs-JavaScript
│   │       └── templates/
│   │           ├── index.html                     # Haupt-Ansicht
│   │           └── confirm.html                   # Bestätigungs-Ansicht
│   └── test/
└── pom.xml                                        # Maven Dependencies
```

## Technologie-Stack

- **Backend:**
  - Spring Boot 3.2.1
  - Spring Data JPA
  - MSSQL JDBC Driver
  - Lombok

- **Frontend:**
  - Thymeleaf (Template Engine)
  - Vanilla JavaScript
  - CSS3

## API-Endpunkte

- `GET /` - Hauptseite mit der Tabelle
- `GET /api/records` - Alle Datensätze als JSON
- `GET /api/field-values/{fieldName}?search={term}` - Werte für ein Feld (mit optionaler Suche)
- `GET /confirm` - Bestätigungsseite
- `POST /api/apply-changes` - Änderungen übernehmen

## Troubleshooting

### Verbindungsfehler zur Datenbank

- Überprüfen Sie die Datenbankverbindung in `application.properties`
- Stellen Sie sicher, dass der MSSQL Server läuft
- Prüfen Sie Firewall-Einstellungen

### Port bereits belegt

Ändern Sie den Port in `application.properties`:

```properties
server.port=8081
```

### Lombok funktioniert nicht

Stellen Sie sicher, dass Ihr IDE das Lombok-Plugin installiert hat:
- IntelliJ IDEA: File → Settings → Plugins → "Lombok"
- Eclipse: Folgen Sie der Anleitung auf https://projectlombok.org/setup/eclipse

## Lizenz

Dieses Projekt ist für interne Verwendung erstellt.
