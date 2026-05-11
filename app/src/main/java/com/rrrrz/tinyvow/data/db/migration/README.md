# Room Migration Checklist

When changing Room schema:

1. Bump `AppDatabase` version.
2. Add a migration object in `AppDatabaseMigrations`.
3. Register the migration in `AppDatabaseMigrations.ALL`.
4. Export and commit the new schema JSON under `app/schemas/com.rrrrz.tinyvow.data.db.AppDatabase`.
5. Add or update `AppDatabaseMigrationTest` coverage for the changed tables, indexes, and preserved data.
6. Run the migration instrumented test when a device/emulator is available.

Do not mutate historical archive snapshots or user-created data unless the migration has an explicit preservation strategy.
