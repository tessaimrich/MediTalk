package at.fhj.tessaimrich.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MedicationDao {

    // 1) Alle Medikamente von Pills,Drops,Creams oder Inhalationen + Sprache abrufen
    @Query("SELECT * FROM medications WHERE category = :cat AND language = :lang ORDER BY name ASC")
    List<Medication> getByCategory(String cat, String lang);

    // 2) Nach Name (für die Suchleiste)
    @Query("SELECT * FROM medications WHERE name LIKE '%' || :search || '%' ORDER BY name ASC")
    List<Medication> searchByName(String search);

    // 3) Einzelnes Medikament anhand der ID abrufen (für DetailActivity)
    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    Medication getById(int id);

    // 4) (Optional) alle Medikamente sämtlicher Kategorien einer Sprache laden
    @Query("SELECT * FROM medications WHERE language = :lang ORDER BY category, name")
    List<Medication> getAll(String lang);

    // 5) Einfügen / Update vielleicht noch gebraucht
   /* @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Medication medication);
   */
    // 6) Löschen (falls später gebraucht)
    @Query("DELETE FROM medications")
    void deleteAll();
}
