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

@Database(entities = {Medication.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE medications ADD COLUMN filename TEXT");
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
                            .addMigrations(MIGRATION_2_3)
                            .allowMainThreadQueries()           // DB-Abfragen im Main-Thread erlauben
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}
