package fr.miage.syp.imagesgroupe9.services;

import fr.miage.syp.imagesgroupe9.model.documents.Image;
import fr.miage.syp.imagesgroupe9.model.documents.ImageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FacadeImage {

    Image saveImagePublication(MultipartFile file, ImageType type, UUID idKeycloak) throws IOException;
    Image saveImageAvatar(MultipartFile file, ImageType type, UUID idKeycloak) throws IOException;
    Image getImage(long idImage);

}
