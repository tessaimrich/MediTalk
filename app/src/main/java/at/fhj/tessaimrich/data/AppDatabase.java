package at.fhj.tessaimrich.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Medication.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MedicationDao medicationDao();

    // Singleton-Instanz
    private static volatile AppDatabase INSTANCE;

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
                            .fallbackToDestructiveMigration()  // Setzt die Datenbank bei einer Schemaänderung zurück
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
