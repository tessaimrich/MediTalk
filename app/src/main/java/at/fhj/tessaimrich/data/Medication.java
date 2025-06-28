package at.fhj.tessaimrich.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Repräsentiert einen Eintrag in der {@code medications}-Tabelle der Room-Datenbank.
 * Jede Instanz dieser Klasse enthält Informationen zu einem Medikament, einschließlich
 * Name, Kategorie, Sprache, TTS-Text und zugehöriger Dateiressourcen.
 */
@Entity(tableName = "medications")
public class Medication {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "category")
    private String category;
    @ColumnInfo(name = "language")
    private String language;
    @ColumnInfo(name = "tts_text")
    private String ttsText;
    @ColumnInfo(name = "filename")
    private String filename;
    @ColumnInfo(name = "pdf_asset")
    private String pdfAsset;

    public Medication(String name, String category, String language, String ttsText, String filename, String pdfAsset) {
        this.name = name;
        this.category = category;
        this.language = language;
        this.ttsText = ttsText;
        this.filename  = filename;
        this.pdfAsset = pdfAsset;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTtsText() {
        return ttsText;
    }
    public void setTtsText(String ttsText) {
        this.ttsText = ttsText;
    }

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPdfAsset() {
        return pdfAsset;
    }
    public void setPdfAsset(String pdfAsset) {
        this.pdfAsset = pdfAsset;
    }

}