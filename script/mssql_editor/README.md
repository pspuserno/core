# MSSQL Tabelleneditor (Spring Boot & React)

Diese Beispielanwendung trennt Frontend und Backend klar voneinander und ermöglicht das Anzeigen, Bearbeiten und Zurücksetzen von Datensätzen einer Microsoft SQL Server Tabelle. Jede Änderung wird revisionssicher in einer Historie gespeichert. Zusätzlich steht eine Swagger UI für die Backend-API zur Verfügung.

## Architekturüberblick

- **Backend**: Java 21 / Spring Boot 3.2 mit REST-API, Swagger UI (`/swagger-ui.html`) und JDBC-Anbindung an eine MSSQL-Datenbank. Änderungen an Datensätzen werden in einer Tabelle `change_history` versioniert und können rückgängig gemacht werden.
- **Frontend**: React (Vite) Single-Page-Anwendung zur Anzeige und Bearbeitung der Tabelleninhalte sowie zum Einsehen der Historie.

```
script/mssql_editor/
├── backend/   # Spring-Boot-Projekt
└── frontend/  # React-Frontend
```

## Voraussetzungen

- Java 21 (z. B. Temurin 21)
- Maven 3.9+
- Node.js 18+ & npm 9+
- Laufender MSSQL-Server mit Netzwerkzugriff für die Anwendung

## Backend installieren & starten

1. Navigiere ins Backend-Verzeichnis und installiere die Abhängigkeiten:

   ```bash
   cd script/mssql_editor/backend
   mvn spring-boot:run
   ```

2. Standardmäßig verbindet sich der Dienst mit `jdbc:sqlserver://localhost:1433;databaseName=master`. Passe die Zugangsdaten in `src/main/resources/application.yml` oder per Umgebungsvariablen (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) an.

3. Beim Start wird – falls nicht vorhanden – automatisch die Tabelle `change_history` angelegt. Die REST-API ist anschließend unter `http://localhost:8080/api/...` erreichbar, die interaktive Swagger UI unter `http://localhost:8080/swagger-ui.html`.

## Frontend installieren & starten

1. In einem zweiten Terminal in das Frontend wechseln und die Pakete installieren:

   ```bash
   cd script/mssql_editor/frontend
   npm install
   npm run dev
   ```

2. Der Dev-Server läuft standardmäßig auf `http://localhost:5173` und proxyt API-Aufrufe an das Backend (`http://localhost:8080`).

## Funktionsumfang

- Tabellenliste laden (`GET /api/v3/table-names`)
- Spalteninformationen anzeigen (`GET /api/tables/{table}/columns`)
- Datensätze paginiert abrufen (`GET /api/tables/{table}/records?limit=...`)
- Werte bearbeiten (`PUT /api/tables/{table}/records`)
- Historie zu einem Datensatz einsehen (`GET /api/tables/{table}/history?...`)
- Einzelne Änderungen rückgängig machen (`POST /api/tables/history/{id}/undo`)

Alle Änderungen werden zusammen mit Benutzer, Zeitstempel sowie alten und neuen Werten gespeichert.

## Hinweise zur Anpassung

- Die Tabellen-Endpoints setzen eine eindeutige Primärschlüsseldefinition voraus. Mehrspaltige Primärschlüssel werden unterstützt.
- Für produktive Einsätze sollten zusätzliche Validierungen, Fehlerbehandlung, Paging-/Filteroptionen sowie ein dediziertes Berechtigungskonzept ergänzt werden.
- Die Frontend-Komponenten sind bewusst kompakt gehalten und können für größere Datenmengen durch Virtualisierung oder serverseitige Filter erweitert werden.

## Tests

Zum schnellen Syntax-Check kann das Backend kompiliert werden:

```bash
cd script/mssql_editor/backend
mvn -q -DskipTests package
```

Für das Frontend lässt sich ein Produktionsbuild erzeugen:

```bash
cd script/mssql_editor/frontend
npm install
npm run build
```
