import React, { useCallback, useEffect, useMemo, useState } from 'react';
import axios from 'axios';

const DEFAULT_TABLE = '';

const TableSelector = ({ availableTables, tableName, onChange }) => (
  <div className="control-group">
    <label htmlFor="tableSelect">Tabelle</label>
    <input
      id="tableSelect"
      list="tables"
      value={tableName}
      placeholder="Tabellennamen eingeben"
      onChange={(event) => onChange(event.target.value)}
    />
    <datalist id="tables">
      {availableTables.map((table) => (
        <option key={table} value={table} />
      ))}
    </datalist>
  </div>
);

const RecordTable = ({ columns, records, onCellChange, dirtyCells, onSave }) => {
  if (!columns.length) {
    return <p>Keine Spaltenmetadaten gefunden.</p>;
  }

  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.name}>{column.name}</th>
            ))}
            <th>Aktion</th>
          </tr>
        </thead>
        <tbody>
          {records.map((record, recordIndex) => (
            <tr key={recordIndex}>
              {columns.map((column) => {
                const key = `${recordIndex}-${column.name}`;
                const isDirty = dirtyCells.has(key);
                return (
                  <td key={column.name} data-dirty={isDirty}>
                    <input
                      value={record[column.name] ?? ''}
                      onChange={(event) => onCellChange(recordIndex, column.name, event.target.value)}
                    />
                  </td>
                );
              })}
              <td className="action-cell">
                <button type="button" onClick={() => onSave(recordIndex)}>
                  Speichern
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

const HistoryList = ({ history, onUndo }) => (
  <div className="history">
    <h3>Historie</h3>
    {history.length === 0 ? (
      <p>Keine Änderungen vorhanden.</p>
    ) : (
      <ul>
        {history.map((entry) => (
          <li key={entry.id}>
            <div className="history-entry">
              <div>
                <span className="history-date">{new Date(entry.changedAt).toLocaleString()}</span>
                <span className="history-user">von {entry.changedBy}</span>
              </div>
              <details>
                <summary>Details anzeigen</summary>
                <pre>{JSON.stringify(entry, null, 2)}</pre>
              </details>
              <button type="button" onClick={() => onUndo(entry.id)}>
                Rückgängig
              </button>
            </div>
          </li>
        ))}
      </ul>
    )}
  </div>
);

const App = () => {
  const [tableName, setTableName] = useState(DEFAULT_TABLE);
  const [availableTables, setAvailableTables] = useState([]);
  const [columns, setColumns] = useState([]);
  const [records, setRecords] = useState([]);
  const [history, setHistory] = useState([]);
  const [dirtyCells, setDirtyCells] = useState(new Set());
  const [user, setUser] = useState('web-user');
  const [primaryKeyColumns, setPrimaryKeyColumns] = useState([]);
  const [selectedPrimaryKey, setSelectedPrimaryKey] = useState(null);

  const fetchTables = useCallback(async () => {
    const response = await axios.get('/api/v3/table-names');
    setAvailableTables(response.data);
  }, []);

  const fetchColumns = useCallback(async () => {
    if (!tableName) {
      return;
    }
    const response = await axios.get(`/api/tables/${tableName}/columns`);
    setColumns(response.data);
    setPrimaryKeyColumns(response.data.filter((column) => column.primaryKey).map((column) => column.name));
  }, [tableName]);

  const fetchRecords = useCallback(async () => {
    if (!tableName) {
      return;
    }
    const response = await axios.get(`/api/tables/${tableName}/records`);
    setRecords(response.data);
  }, [tableName]);

  const fetchHistory = useCallback(async (primaryKey) => {
    if (!tableName || !primaryKey || !Object.keys(primaryKey).length) {
      setHistory([]);
      return;
    }
    const params = new URLSearchParams(primaryKey).toString();
    const response = await axios.get(`/api/tables/${tableName}/history?${params}`);
    setHistory(response.data);
  }, [tableName]);

  useEffect(() => {
    fetchTables();
  }, [fetchTables]);

  useEffect(() => {
    if (!tableName) {
      return;
    }
    fetchColumns();
    fetchRecords();
  }, [tableName, fetchColumns, fetchRecords]);

  const onCellChange = useCallback((recordIndex, columnName, value) => {
    setRecords((prev) => {
      const updated = [...prev];
      updated[recordIndex] = { ...updated[recordIndex], [columnName]: value };
      return updated;
    });
    setDirtyCells((prev) => {
      const updated = new Set(prev);
      updated.add(`${recordIndex}-${columnName}`);
      return updated;
    });
  }, []);

  const buildPrimaryKey = useCallback((record) => {
    const key = {};
    primaryKeyColumns.forEach((column) => {
      const value = record[column];
      if (value !== undefined && value !== null && value !== '') {
        key[column] = value;
      }
    });
    return key;
  }, [primaryKeyColumns]);

  const onSave = useCallback(async (recordIndex) => {
    if (!primaryKeyColumns.length) {
      alert('Es wurden keine Primärschlüsselspalten gefunden.');
      return;
    }
    const record = records[recordIndex];
    const primaryKey = buildPrimaryKey(record);
    if (!Object.keys(primaryKey).length) {
      alert('Primärschlüsselwerte fehlen.');
      return;
    }
    const dirty = {};
    columns.forEach((column) => {
      const key = `${recordIndex}-${column.name}`;
      if (dirtyCells.has(key)) {
        dirty[column.name] = record[column.name];
      }
    });

    if (!Object.keys(dirty).length) {
      return;
    }

    await axios.put(`/api/tables/${tableName}/records`, {
      primaryKey,
      values: dirty,
      changedBy: user
    });

    await fetchRecords();
    await fetchHistory(primaryKey);
    setSelectedPrimaryKey(primaryKey);
    setDirtyCells(new Set());
  }, [records, buildPrimaryKey, columns, dirtyCells, tableName, user, fetchRecords, fetchHistory]);

  const onSelectRecord = useCallback((record) => {
    const primaryKey = buildPrimaryKey(record);
    if (!Object.keys(primaryKey).length) {
      setSelectedPrimaryKey(null);
      setHistory([]);
      return;
    }
    setSelectedPrimaryKey(primaryKey);
    fetchHistory(primaryKey);
  }, [buildPrimaryKey, fetchHistory]);

  const onUndo = useCallback(async (historyId) => {
    await axios.post(`/api/tables/history/${historyId}/undo`, { changedBy: user });
    await fetchRecords();
    if (selectedPrimaryKey) {
      await fetchHistory(selectedPrimaryKey);
    }
    setDirtyCells(new Set());
  }, [fetchRecords, fetchHistory, selectedPrimaryKey, user]);

  useEffect(() => {
    if (records.length) {
      onSelectRecord(records[0]);
    } else {
      setHistory([]);
      setSelectedPrimaryKey(null);
    }
  }, [records, onSelectRecord]);

  const tableSummary = useMemo(() => {
    if (!tableName) {
      return 'Keine Tabelle ausgewählt';
    }
    return `${tableName} (${records.length} Zeilen geladen)`;
  }, [tableName, records.length]);

  return (
    <div className="app">
      <header>
        <h1>MSSQL Tabelleneditor</h1>
        <p>{tableSummary}</p>
      </header>

      <section className="controls">
        <TableSelector
          availableTables={availableTables}
          tableName={tableName}
          onChange={setTableName}
        />
        <div className="control-group">
          <label htmlFor="user">Benutzer</label>
          <input id="user" value={user} onChange={(event) => setUser(event.target.value)} />
        </div>
      </section>

      <section className="content">
        <div className="records">
          <h2>Datensätze</h2>
          <RecordTable
            columns={columns}
            records={records}
            onCellChange={onCellChange}
            dirtyCells={dirtyCells}
            onSave={onSave}
          />
          <div className="record-list">
            <h3>Datensatz auswählen</h3>
            <ul>
              {records.map((record, index) => (
                <li key={index}>
                  <button type="button" onClick={() => onSelectRecord(record)}>
                    {primaryKeyColumns.map((column) => record[column]).join(' / ') || `Zeile ${index + 1}`}
                  </button>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <HistoryList history={history} onUndo={onUndo} />
      </section>
    </div>
  );
};

export default App;
