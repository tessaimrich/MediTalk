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

@Database(entities = {Medication.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE medications ADD COLUMN filename TEXT");
        }
    };
    //Migration von Version 3 → 4 hinzufügen
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            db.execSQL(  "ALTER TABLE medications ADD COLUMN pdf_asset TEXT DEFAULT 'undefined'"
            );
            // hier alle weiteren Änderungen eintragen, die im Schema gemacht werden
            db.execSQL(
                    "UPDATE medications " +
                            "SET pdf_asset = 'AmlodipineValsartanMylan_EN.pdf' " +
                            "WHERE name = 'Amlodipine Valsartan Mylan' " +
                            "  AND language = 'en'"
            );
        }
    };
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // optional: nochmal default-Werte setzen oder Prüf-UPDATE ausführen
        }
    };
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // keine weiteren Änderungen nötig
        }
    };
    public abstract MedicationDao medicationDao();

    // Singleton-Instanz
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    // Singleton-Muster zur Vermeidung von mehrfacher Instanzierung
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
                            .allowMainThreadQueries()           // DB-Abfragen im Main-Thread erlauben
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}
