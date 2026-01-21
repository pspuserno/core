// Globale Variablen
let tableData = [];
let changes = new Map(); // Map<"rowId_fieldName", {rowId, fieldName, oldValue, newValue}>
let currentEditCell = null;
let autocompleteVisible = false;
let selectedIndex = -1;
let currentFieldName = null;
let currentRowId = null;
let availableValues = [];

// Feldnamen Mapping (JavaScript Property Name -> Display Name)
const fieldMapping = {
    'id': 'ID',
    'dokumenttyp': 'Dokumenttyp',
    'leseKVNr': 'LeseKVNr',
    'leseIKNr': 'LeseIKNr',
    'leseAGNr': 'LeseAGNr',
    'fuhrendePartnerNr': 'FührendePartnerNr',
    'prozessauslosend': 'Prozessauslösend',
    'prozess': 'Prozess',
    'aufbewahrungszeit': 'Aufbewahrungszeit',
    'ablagemodus': 'Ablagemodus',
    'code': 'Code',
    'fachbereich': 'Fachbereich',
    'negativlisteRecherche': 'NegativlisteRecherche',
    'negativlisteLoschauftrag': 'NegativlisteLöschauftrag',
    'prioritat': 'Priorität',
    'eskalation1': 'Eskalation1',
    'aktenplan21c': 'Aktenplan21c',
    'eskalation2': 'Eskalation2',
    'eskalation3': 'Eskalation3',
    'eskalation4': 'Eskalation4',
    'eskalation5': 'Eskalation5',
    'eskalation6': 'Eskalation6',
    'vorzugsdokument': 'Vorzugsdokument',
    'stichwortId': 'StichwortId',
    'stichwort': 'Stichwort',
    'lesePartnerNr': 'LesePartnerNr',
    'exportXML': 'ExportXML',
    'exportStatistik': 'ExportStatistik',
    'mobileRelevant': 'MobileRelevant',
    'partnerNrAll': 'PartnerNrAll',
    'specialPartnerNr': 'SpecialPartnerNr',
    'empfangsbestatigung': 'Empfangsbestätigung',
    'interforumSoLe': 'InterforumSoLe',
    'tieferlesung': 'Tieferlesung',
    'export21cNG': 'Export21cNG',
    'fuzzyProjekt': 'FuzzyProjekt',
    'fachbereichKz': 'FachbereichKz',
    'fachlicheNachbearbeitung': 'FachlicheNachbearbeitung',
    'zustaendigkeit': 'Zustaendigkeit'
};

// Beim Laden der Seite
document.addEventListener('DOMContentLoaded', function() {
    loadTableData();
    setupEventListeners();
});

// Event Listeners einrichten
function setupEventListeners() {
    // Bestätigungs-Button
    document.getElementById('confirmChangesBtn').addEventListener('click', function() {
        if (changes.size > 0) {
            // Änderungen im SessionStorage speichern
            sessionStorage.setItem('pendingChanges', JSON.stringify(Array.from(changes.values())));
            // Zur Bestätigungsseite wechseln
            window.location.href = '/confirm';
        }
    });

    // Autocomplete Search Input
    const searchInput = document.getElementById('autocompleteSearch');
    searchInput.addEventListener('input', function() {
        filterAutocompleteValues(this.value);
    });

    searchInput.addEventListener('keydown', function(e) {
        handleAutocompleteKeydown(e);
    });

    // Klick außerhalb schließt Autocomplete
    document.addEventListener('click', function(e) {
        const autocomplete = document.getElementById('autocomplete');
        if (!autocomplete.contains(e.target) && e.target !== currentEditCell) {
            closeAutocomplete();
        }
    });
}

// Tabellendaten laden
async function loadTableData() {
    try {
        const response = await fetch('/api/records');
        tableData = await response.json();
        renderTable();
    } catch (error) {
        console.error('Fehler beim Laden der Daten:', error);
        alert('Fehler beim Laden der Daten. Bitte überprüfen Sie die Datenbankverbindung.');
    }
}

// Tabelle rendern
function renderTable() {
    const tbody = document.getElementById('tableBody');
    tbody.innerHTML = '';

    tableData.forEach(row => {
        const tr = document.createElement('tr');

        // Alle Felder durchgehen
        Object.keys(fieldMapping).forEach(fieldKey => {
            const td = document.createElement('td');
            const value = row[fieldKey];

            // Wert anzeigen
            td.textContent = formatValue(value);
            td.dataset.field = fieldKey;
            td.dataset.rowId = row.id;

            // ID-Feld nicht editierbar machen
            if (fieldKey !== 'id') {
                td.addEventListener('click', function() {
                    startEdit(this, row.id, fieldKey, value);
                });

                // Prüfen ob Änderung vorhanden
                const changeKey = `${row.id}_${fieldKey}`;
                if (changes.has(changeKey)) {
                    td.classList.add('changed');
                }
            }

            tr.appendChild(td);
        });

        tbody.appendChild(tr);
    });
}

// Wert formatieren für Anzeige
function formatValue(value) {
    if (value === null || value === undefined) {
        return '';
    }
    if (typeof value === 'boolean') {
        return value ? 'true' : 'false';
    }
    return value.toString();
}

// Bearbeitung starten
async function startEdit(cell, rowId, fieldName, currentValue) {
    // Vorherige Bearbeitung schließen
    if (currentEditCell) {
        closeAutocomplete();
    }

    currentEditCell = cell;
    currentRowId = rowId;
    currentFieldName = fieldName;

    cell.classList.add('editing');

    // Werte für das Feld laden
    await loadFieldValues(fieldName);

    // Autocomplete anzeigen
    showAutocomplete(cell);
}

// Feldwerte laden
async function loadFieldValues(fieldName) {
    try {
        const response = await fetch(`/api/field-values/${fieldName}`);
        availableValues = await response.json();
        displayAutocompleteValues(availableValues);
    } catch (error) {
        console.error('Fehler beim Laden der Feldwerte:', error);
        availableValues = [];
    }
}

// Autocomplete anzeigen
function showAutocomplete(cell) {
    const autocomplete = document.getElementById('autocomplete');
    const searchInput = document.getElementById('autocompleteSearch');

    // Position berechnen
    const rect = cell.getBoundingClientRect();
    autocomplete.style.left = rect.left + 'px';
    autocomplete.style.top = (rect.bottom + window.scrollY) + 'px';

    // Anzeigen
    autocomplete.style.display = 'flex';
    autocompleteVisible = true;

    // Focus auf Suchfeld
    searchInput.value = '';
    searchInput.focus();

    selectedIndex = -1;
}

// Autocomplete schließen
function closeAutocomplete() {
    const autocomplete = document.getElementById('autocomplete');
    autocomplete.style.display = 'none';
    autocompleteVisible = false;

    if (currentEditCell) {
        currentEditCell.classList.remove('editing');
        currentEditCell = null;
    }

    currentRowId = null;
    currentFieldName = null;
    availableValues = [];
    selectedIndex = -1;
}

// Autocomplete Werte anzeigen
function displayAutocompleteValues(values) {
    const list = document.getElementById('autocompleteList');
    list.innerHTML = '';

    values.forEach((value, index) => {
        const li = document.createElement('li');
        li.textContent = value;
        li.dataset.index = index;

        li.addEventListener('click', function() {
            selectValue(value);
        });

        list.appendChild(li);
    });
}

// Werte filtern (Fuzzy Search)
function filterAutocompleteValues(searchTerm) {
    if (!searchTerm) {
        displayAutocompleteValues(availableValues);
        return;
    }

    // Fuzzy-Suche auf dem Client
    const searchLower = searchTerm.toLowerCase();
    const filtered = availableValues.filter(value =>
        value.toLowerCase().includes(searchLower)
    );

    displayAutocompleteValues(filtered);
}

// Wert auswählen
function selectValue(value) {
    if (!currentEditCell || !currentFieldName || currentRowId === null) {
        return;
    }

    // Alten Wert ermitteln
    const row = tableData.find(r => r.id === currentRowId);
    const oldValue = row[currentFieldName];

    // Wenn Wert sich geändert hat
    if (formatValue(oldValue) !== value) {
        const changeKey = `${currentRowId}_${currentFieldName}`;

        changes.set(changeKey, {
            rowId: currentRowId,
            fieldName: currentFieldName,
            oldValue: formatValue(oldValue),
            newValue: value
        });

        // Wert in tableData aktualisieren (für weitere Vergleiche)
        row[currentFieldName] = parseValue(value, typeof oldValue);

        // Zelle als geändert markieren
        currentEditCell.textContent = value;
        currentEditCell.classList.add('changed');
    }

    // Änderungszähler aktualisieren
    updateChangeCount();

    // Autocomplete schließen
    closeAutocomplete();
}

// Wert parsen basierend auf Typ
function parseValue(value, originalType) {
    if (originalType === 'boolean') {
        return value === 'true';
    } else if (originalType === 'number') {
        return parseInt(value);
    }
    return value;
}

// Änderungszähler aktualisieren
function updateChangeCount() {
    const count = changes.size;
    document.getElementById('changeCount').textContent = count;
    document.getElementById('confirmChangesBtn').disabled = count === 0;
}

// Tastaturnavigation für Autocomplete
function handleAutocompleteKeydown(e) {
    const list = document.getElementById('autocompleteList');
    const items = list.querySelectorAll('li');

    if (items.length === 0) return;

    switch (e.key) {
        case 'ArrowDown':
            e.preventDefault();
            selectedIndex = (selectedIndex + 1) % items.length;
            updateSelection(items);
            break;

        case 'ArrowUp':
            e.preventDefault();
            selectedIndex = selectedIndex <= 0 ? items.length - 1 : selectedIndex - 1;
            updateSelection(items);
            break;

        case 'Enter':
            e.preventDefault();
            if (selectedIndex >= 0 && selectedIndex < items.length) {
                selectValue(items[selectedIndex].textContent);
            }
            break;

        case 'Escape':
            e.preventDefault();
            closeAutocomplete();
            break;
    }
}

// Auswahl in Liste aktualisieren
function updateSelection(items) {
    items.forEach((item, index) => {
        if (index === selectedIndex) {
            item.classList.add('selected');
            item.scrollIntoView({ block: 'nearest' });
        } else {
            item.classList.remove('selected');
        }
    });
}
