package fr.miage.syp.imagesgroupe9.model.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "images")
public class Image {

    @Id
    private long id; // Snowflake ID
    private String nom;
    private ImageType type;
    private Long taille;
    private String filePath;

    public Image() {
    }

    public Image(String nom, ImageType type, Long taille, String filePath) {
        this.nom = nom;
        this.type = type;
        this.taille = taille;
        this.filePath = filePath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ImageType getType() {
        return type;
    }

    public void setType(ImageType type) {
        this.type = type;
    }

    public Long getTaille() {
        return taille;
    }

    public void setTaille(Long taille) {
        this.taille = taille;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
