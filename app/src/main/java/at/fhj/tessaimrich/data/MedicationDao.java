package at.fhj.tessaimrich.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object (DAO) für den Zugriff auf Medikamenten-Daten in der Room-Datenbank.
 * Definiert Abfragen zum Laden, Einfügen und Löschen von {@link Medication}-Einträgen.
 */
@Dao
public interface MedicationDao {



    /**
     * Gibt alle eindeutigen Medikamentennamen für eine bestimmte Sprache zurück.
     * @param lang die Sprache (z. B. "en")
     * @return alphabetisch sortierte Liste der Namen
     */
    @Query("SELECT DISTINCT name FROM medications WHERE language = :lang ORDER BY name ASC")
    List<String> getAllNames(String lang);


    /**
     * Gibt alle Medikamente einer bestimmten Sprache zurück.
     * Die Ergebnisse sind gruppiert nach Kategorie und alphabetisch sortiert nach Name.
     * @param lang die Sprache
     * @return Liste aller Medikamente in dieser Sprache
     */
    @Query("SELECT * FROM medications WHERE language = :lang ORDER BY category, name")
    List<Medication> getAll(String lang);

    /**
     * Fügt eine oder mehrere Medikamenten-Einträge ein oder aktualisiert sie bei Konflikten.
     * @param medications die einzufügenden Medikamente
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Medication... medications);


    /**
     * Gibt alle Medikamente zurück, die einer bestimmten Kategorie und Sprache entsprechen.
     * @param category die Kategorie (z.B "cream")
     * @param lang     die Sprache (z.B "es")
     * @return Liste der passenden Medikamente
     */
    @Query("SELECT * FROM medications WHERE category = :category AND language = :lang")
    List<Medication> findByCategoryAndLanguage(String category, String lang);

    /**
     * Sucht ein Medikament mit exakt übereinstimmendem Namen und Sprache.
     * @param name der vollständige Name des Medikaments
     * @param lang die Sprache
     * @return das gefundene Medikament oder {@code null}, wenn nicht vorhanden
     */
    @Query("SELECT * FROM medications WHERE name = :name AND language = :lang LIMIT 1")
    Medication findByNameAndLanguage(String name, String lang);
}
