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

    // alte Methode entfernt oder umbenannt
    @Query("SELECT * FROM medications WHERE category = :category")
    List<Medication> findByCategory(String category);
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
    @Query("SELECT * FROM medications WHERE category = :category AND language = :lang")
    List<Medication> findByCategoryAndLanguage(String category, String lang);
    @Query("SELECT * FROM medications WHERE name = :name AND language = :lang LIMIT 1")
    Medication findByNameAndLanguage(String name, String lang);
    // für Teil-Matches:
    @Query("SELECT * FROM medications WHERE name LIKE '%' || :search || '%' AND language = :lang ORDER BY name")
    List<Medication> searchByNameAndLanguage(String search, String lang);
    // AutoComplete-Vorschläge (Wenn man alle Medikamenten-Namen einmalig aus der DB zieht)
    @Query("SELECT name FROM medications WHERE language = :lang ORDER BY name")
    List<String> getAllNamesForLanguage(String lang);

}
