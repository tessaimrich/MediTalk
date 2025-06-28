package at.fhj.tessaimrich.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Die zentrale Room-Datenbankklasse der App.
 * Enthält die Definition der Datenbankstruktur, Migrationslogik und stellt den Zugriff
 * auf die DAO bereit.
 */
@Database(entities = {Medication.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    /**
     * Migration von Datenbankversion 2 auf 3.
     * Fügt die Spalte {@code filename} zur Tabelle {@code medications} hinzu.
     */
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE medications ADD COLUMN filename TEXT");
        }
    };
    /**
     * Migration von Datenbankversion 3 auf 4.
     * Fügt die Spalte {@code pdf_asset} mit einem Standardwert hinzu und
     * aktualisiert bestehende Einträge für ein bestimmtes Medikament.
     */
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            db.execSQL(  "ALTER TABLE medications ADD COLUMN pdf_asset TEXT DEFAULT 'undefined'"
            );
            db.execSQL(
                    "UPDATE medications " +
                            "SET pdf_asset = 'AmlodipineValsartanMylan_EN.pdf' " +
                            "WHERE name = 'Amlodipine Valsartan Mylan' " +
                            "  AND language = 'en'"
            );
        }
    };
    /**
     * Migration von Version 4 auf 5. Es werden keine Änderungen vorgenommen.
     * Diese Methode dient als Platzhalter für zukünftige Updates.
     */
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
        }
    };
    /**
     * Migration von Version 5 auf 6. Es werden keine Änderungen vorgenommen.
     */
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
        }
    };
    /**
     * Liefert das {@link MedicationDao}, um auf Medikamenten-Daten zuzugreifen.
     * @return das {@link MedicationDao}-Objekt
     */
    public abstract MedicationDao medicationDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    /**
     * Gibt die Singleton-Instanz der {@link AppDatabase} zurück. Falls noch keine Instanz
     * existiert, wird sie erzeugt und konfiguriert.
     * @param context der Anwendungskontext
     * @return die Datenbankinstanz
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "medidb"
                            )
                            .addMigrations(MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,
                                    MIGRATION_5_6)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}
