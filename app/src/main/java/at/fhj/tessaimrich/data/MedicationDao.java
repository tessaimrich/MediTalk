package at.fhj.tessaimrich.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MedicationDao {
    @Query("SELECT COUNT(*) FROM medications")
    int countAll();
    // Alle Medikamente einer Kategorie und Sprache synchron laden
    @Query("SELECT * FROM medications WHERE category = :cat AND language = :lang ORDER BY name ASC")
    List<Medication> getByCategory(String cat, String lang);

    //  Nach Name suchen, Teilstring-Suche
    @Query("SELECT * FROM medications WHERE name LIKE '%' || :search || '%' ORDER BY name ASC")
    List<Medication> searchByName(String search);

    // Einzelnes Medikament anhand der ID synchron abrufen
    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    Medication getById(int id);

    // Gibt nur die Pillennamen zurück
    @Query("SELECT name FROM medications")
    List<String> getAllNames();

    //  Alle Medikamente einer Sprache synchron laden
    @Query("SELECT * FROM medications WHERE language = :lang ORDER BY category, name")
    List<Medication> getAll(String lang);

    //  Insert / Update (synchron, über Executor im AppDatabase)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Medication... medications);

    // Löschen aller Einträge
    @Query("DELETE FROM medications")
    void deleteAll();

    // Exakte Namenssuche synchron
    @Query("SELECT * FROM medications WHERE name = :name LIMIT 1")
    Medication findByName(String name);
}
