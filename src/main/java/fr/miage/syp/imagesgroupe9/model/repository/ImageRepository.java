package fr.miage.syp.imagesgroupe9.model.repository;

import fr.miage.syp.imagesgroupe9.model.documents.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends MongoRepository<Image, UUID> {
    Image getImageById(long id);
}
