package fr.miage.syp.imagesgroupe9.controlleur;

import fr.miage.syp.imagesgroupe9.model.documents.Image;
import fr.miage.syp.imagesgroupe9.model.documents.ImageType;
import fr.miage.syp.imagesgroupe9.services.FacadeImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/images")
public class Controlleur {

    private final FacadeImage facadeImage;

    private static final Logger LOG = LoggerFactory.getLogger(Controlleur.class);

    public Controlleur(FacadeImage facadeImage) {
        this.facadeImage = facadeImage;
    }

    public record ReponseAPI<T>(
            int code,
            String message,
            T contenu
    ){}

    public record IdPhotoDTO(
            long idPhoto
    ){}

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam("type") ImageType type,
                                         Authentication authentication) {
        try {
            Image image;
            switch (type){
                case AVATAR:
                    image = this.facadeImage.saveImageAvatar(file, type, UUID.fromString(authentication.getName()));
                    break;
                case PUBLICATION:
                    image = this.facadeImage.saveImagePublication(file, type, UUID.fromString(authentication.getName()));
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Type d'image non supporté");
            }
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(image.getId()).toUri();
            return ResponseEntity.created(location).body(new ReponseAPI<>(200, "Image sauvegardée", new IdPhotoDTO(image.getId())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload de l'image : " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImageById(@PathVariable("id") UUID id) {

        Image image = this.facadeImage.getImage(id);

        Path path = Paths.get(image.getFilePath());
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] imageData = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
