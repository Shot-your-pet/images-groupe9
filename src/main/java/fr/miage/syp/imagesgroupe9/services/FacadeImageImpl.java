package fr.miage.syp.imagesgroupe9.services;

import fr.miage.syp.imagesgroupe9.model.documents.Image;
import fr.miage.syp.imagesgroupe9.model.documents.ImageType;
import fr.miage.syp.imagesgroupe9.model.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FacadeImageImpl implements FacadeImage{

    @Value("${upload.dir}")
    private String IMAGE_PATH;

    private final ImageRepository imageRepository;
    private final RabbitEventSender rabbitEventSender;

    private static final Logger LOG = LoggerFactory.getLogger(FacadeImageImpl.class);

    public FacadeImageImpl(ImageRepository imageRepository, RabbitEventSender rabbitEventSender) {
        this.imageRepository = imageRepository;
        this.rabbitEventSender = rabbitEventSender;
    }


    @Override
    public Image saveImagePublication(MultipartFile file, ImageType type, UUID idKeycloak) throws IOException {
        /*
        * Est appelée lors de l'upload d'une image de publication et renvoie au front l'id de l'image qui est généré pour que la publication sache quelle image lui est associé
        * */
        UUID uuid = UUID.randomUUID();
        String uniqueFileName = uuid + "_" + file.getOriginalFilename();
        Path uploadDirPath = Paths.get(IMAGE_PATH + "/publications");

        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);
        }

        Path filePath = uploadDirPath.resolve(uniqueFileName);

        // Sauvegarder le fichier dans le système de fichiers
        Files.write(filePath, file.getBytes());

        Image image = new Image();
        image.setId(uuid);
        image.setNom(file.getOriginalFilename());
        image.setType(ImageType.PUBLICATION);
        image.setTaille(file.getSize() / 1024); // conversion bytes -> Ko
        image.setFilePath(filePath.toString());
        LOG.info("Nouvelle image de publication sauvegardée : " + image.getId());
        return this.imageRepository.save(image);
    }

    @Override
    public Image saveImageAvatar(MultipartFile file, ImageType type, UUID idKeycloak) throws IOException {
        /*
        * L'utilisateur change son image et l'information est directement transmise au service des utilisateurs. Il n'y a pas de double appel coté front
        * */
        UUID uuid = UUID.randomUUID();
        String uniqueFileName = uuid + "_" + file.getOriginalFilename();
        Path uploadDirPath = Paths.get(IMAGE_PATH+ "/avatar");

        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);
        }

        Path filePath = uploadDirPath.resolve(uniqueFileName);

        Files.write(filePath, file.getBytes());

        Image image = new Image();
        image.setId(uuid);
        image.setNom(file.getOriginalFilename());
        image.setType(ImageType.AVATAR);
        image.setTaille(file.getSize() / 1024); // conversion bytes -> Ko
        image.setFilePath(filePath.toString());
        image = this.imageRepository.save(image);
        LOG.info("Nouvelle image d'avatar sauvegardée : " + image.getId());
        this.rabbitEventSender.sendUpdateAvatarEvent(idKeycloak, image.getId());
        LOG.info("Envoie de l'évènement de changement d'avatar pour l'utilisateur : " + idKeycloak);
        return image;
    }

    @Override
    public Image getImage(UUID idImage) {
        return this.imageRepository.getImageById(idImage);
    }
}
