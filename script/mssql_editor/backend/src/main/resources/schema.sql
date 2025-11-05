IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'change_history')
BEGIN
    CREATE TABLE change_history (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        table_name NVARCHAR(255) NOT NULL,
        primary_key NVARCHAR(MAX) NOT NULL,
        previous_values NVARCHAR(MAX) NULL,
        new_values NVARCHAR(MAX) NULL,
        changed_by NVARCHAR(255) NOT NULL,
        changed_at DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
END;
