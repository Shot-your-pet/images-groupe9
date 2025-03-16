package fr.miage.syp.imagesgroupe9.controlleur;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.miage.syp.imagesgroupe9.model.documents.Image;
import fr.miage.syp.imagesgroupe9.model.documents.ImageType;
import fr.miage.syp.imagesgroupe9.services.FacadeImage;
import fr.miage.syp.imagesgroupe9.services.RabbitEventSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controlleur.class)
@Import(ControlleurTest.MockFacadeImageConfiguration.class)
class ControlleurTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FacadeImage facadeImage;

    @Autowired
    private RabbitEventSender rabbitEventSender;

    private final String USER_UUID = "11111111-1111-1111-1111-111111111111";

    private RequestPostProcessor validJwt() {
        return jwt().jwt(Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", USER_UUID)
                .build());
    }

    @TestConfiguration
    static class MockFacadeImageConfiguration {
        @Bean
        public FacadeImage facadeImage() {
            return Mockito.mock(FacadeImage.class);
        }
        @Bean
        public RabbitEventSender rabbitEventSender() {
            return Mockito.mock(RabbitEventSender.class);
        }
    }

    @TempDir
    Path tempDir;

    private Image creerImage(long id, String fileName, ImageType type, String filePath) {
        Image image = new Image();
        image.setId(id);
        image.setNom(fileName);
        image.setType(type);
        image.setFilePath(filePath);
        image.setTaille((new File(filePath).length() / 1024));
        return image;
    }

    @Test
    void testUploadImageAvatarOk() throws Exception {
        byte[] content = "image".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", content);
        String dummyPath = tempDir.resolve("avatar.jpg").toString();
        Image dummyImage = creerImage(1L, "avatar.jpg", ImageType.AVATAR, dummyPath);

        when(facadeImage.saveImageAvatar(any(), any(), any())).thenReturn(dummyImage);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/images/upload")
                        .file(file)
                        .param("type", "AVATAR")
                        .with(validJwt())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().string(containsString("Image sauvegardée")));
    }

    @Test
    void testUploadImage_PublicationOK() throws Exception {
        byte[] content = "publication image".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "pub.jpg", "image/jpeg", content);
        String dummyPath = tempDir.resolve("pub.jpg").toString();
        Image dummyImage = creerImage(2L, "pub.jpg", ImageType.PUBLICATION, dummyPath);

        when(facadeImage.saveImagePublication(any(), any(), any())).thenReturn(dummyImage);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/images/upload")
                        .file(file)
                        .param("type", "PUBLICATION")
                        .with(validJwt())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().string(containsString("Image sauvegardée")));
    }

    @Test
    void testUploadImage_InvalidTypeKO() throws Exception {
        byte[] content = "fiujviuergier".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "fichier_qui_nest_pas_du_porno.jpg", "image/jpeg", content);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/images/upload")
                        .file(file)
                        .param("type", "UNSUPPORTED")
                        .with(validJwt())
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetImageOK() throws Exception {
        String contentStr = "image";
        Path imagePath = tempDir.resolve("testImage.jpg");
        try (FileOutputStream fos = new FileOutputStream(imagePath.toFile())) {
            fos.write(contentStr.getBytes());
        }
        Image dummyImage = creerImage(3L, "testImage.jpg", ImageType.PUBLICATION, imagePath.toString());
        when(facadeImage.getImage(3L)).thenReturn(dummyImage);


        mockMvc.perform(MockMvcRequestBuilders.get("/images/{id}", 3L)
                .with(validJwt()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(contentStr.getBytes()))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }
}