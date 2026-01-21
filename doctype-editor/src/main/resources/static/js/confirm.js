// Änderungen aus SessionStorage laden
let pendingChanges = [];

document.addEventListener('DOMContentLoaded', function() {
    loadChanges();
    setupEventListeners();
});

function loadChanges() {
    const changesJson = sessionStorage.getItem('pendingChanges');

    if (!changesJson) {
        document.getElementById('changesSummary').innerHTML =
            '<p style="text-align: center; padding: 20px;">Keine Änderungen vorhanden.</p>';
        return;
    }

    pendingChanges = JSON.parse(changesJson);
    displayChanges();
}

function displayChanges() {
    const container = document.getElementById('changesSummary');
    container.innerHTML = '';

    if (pendingChanges.length === 0) {
        container.innerHTML =
            '<p style="text-align: center; padding: 20px;">Keine Änderungen vorhanden.</p>';
        return;
    }

    // Änderungen nach Row-ID gruppieren
    const changesByRow = {};
    pendingChanges.forEach(change => {
        if (!changesByRow[change.rowId]) {
            changesByRow[change.rowId] = [];
        }
        changesByRow[change.rowId].push(change);
    });

    // Für jede Row eine Zusammenfassung erstellen
    Object.keys(changesByRow).forEach(rowId => {
        const rowChanges = changesByRow[rowId];

        const changeItem = document.createElement('div');
        changeItem.className = 'change-item';

        const title = document.createElement('h3');
        title.textContent = `Datensatz ID: ${rowId}`;
        changeItem.appendChild(title);

        rowChanges.forEach(change => {
            const changeDetails = document.createElement('div');
            changeDetails.className = 'change-details';

            // Feldname
            const fieldLabel = document.createElement('div');
            fieldLabel.className = 'change-label';
            fieldLabel.textContent = change.fieldName + ':';

            // Werte
            const valueContainer = document.createElement('div');
            valueContainer.className = 'change-value';

            const oldValueSpan = document.createElement('span');
            oldValueSpan.className = 'old-value';
            oldValueSpan.textContent = change.oldValue || '(leer)';

            const arrow = document.createTextNode(' → ');

            const newValueSpan = document.createElement('span');
            newValueSpan.className = 'new-value';
            newValueSpan.textContent = change.newValue;

            valueContainer.appendChild(oldValueSpan);
            valueContainer.appendChild(arrow);
            valueContainer.appendChild(newValueSpan);

            changeDetails.appendChild(fieldLabel);
            changeDetails.appendChild(valueContainer);

            changeItem.appendChild(changeDetails);
        });

        container.appendChild(changeItem);
    });
}

function setupEventListeners() {
    // Checkbox
    const checkbox = document.getElementById('confirmCheckbox');
    const applyBtn = document.getElementById('applyBtn');

    checkbox.addEventListener('change', function() {
        applyBtn.disabled = !this.checked;
    });

    // Zurück-Button
    document.getElementById('backBtn').addEventListener('click', function() {
        window.location.href = '/';
    });

    // Änderungen übernehmen
    applyBtn.addEventListener('click', async function() {
        await applyChanges();
    });
}

async function applyChanges() {
    const applyBtn = document.getElementById('applyBtn');
    const backBtn = document.getElementById('backBtn');
    const resultMessage = document.getElementById('resultMessage');

    // Buttons deaktivieren
    applyBtn.disabled = true;
    backBtn.disabled = true;
    applyBtn.textContent = 'Wird übernommen...';

    try {
        const response = await fetch('/api/apply-changes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                changes: pendingChanges
            })
        });

        const result = await response.json();

        if (result.success) {
            // Erfolg
            resultMessage.className = 'message success';
            resultMessage.textContent = result.message;
            resultMessage.style.display = 'block';

            // SessionStorage leeren
            sessionStorage.removeItem('pendingChanges');

            // Nach 2 Sekunden zur Hauptseite zurück
            setTimeout(() => {
                window.location.href = '/';
            }, 2000);
        } else {
            // Fehler
            resultMessage.className = 'message error';
            resultMessage.textContent = result.message;
            resultMessage.style.display = 'block';

            // Buttons wieder aktivieren
            applyBtn.disabled = false;
            backBtn.disabled = false;
            applyBtn.textContent = 'Änderungen übernehmen';
        }
    } catch (error) {
        console.error('Fehler beim Übernehmen der Änderungen:', error);

        resultMessage.className = 'message error';
        resultMessage.textContent = 'Netzwerkfehler: ' + error.message;
        resultMessage.style.display = 'block';

        // Buttons wieder aktivieren
        applyBtn.disabled = false;
        backBtn.disabled = false;
        applyBtn.textContent = 'Änderungen übernehmen';
    }
}
