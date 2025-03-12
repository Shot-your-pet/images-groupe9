package fr.miage.syp.imagesgroupe9.services;

import fr.miage.syp.imagesgroupe9.model.documents.Image;
import fr.miage.syp.imagesgroupe9.model.documents.ImageType;
import fr.miage.syp.imagesgroupe9.model.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FacadeImageImplTest {


    @TempDir
    Path tempDir;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private RabbitEventSender rabbitEventSender;

    @InjectMocks
    private FacadeImageImpl facadeImage;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(facadeImage, "IMAGE_PATH", tempDir.toString());
    }

    @Test
    void testSaveImagePublicationOK() throws IOException {
        byte[] content = "keycloak c'est de la merde quand il s'agit de le deployer".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "keycloak_caca.jpg", "image/jpeg", content);
        UUID userId = UUID.randomUUID();

        when(imageRepository.save(ArgumentMatchers.any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Image image = facadeImage.saveImagePublication(file, ImageType.PUBLICATION, userId);
        Path filePath = Paths.get(image.getFilePath());

        assertTrue(Files.exists(filePath), "Le fichier doit exister dans le répertoire de publication");
        assertArrayEquals(content, Files.readAllBytes(filePath), "C'est pas le même contenu fréro");
        assertEquals("keycloak_caca.jpg", image.getNom());
        assertEquals(ImageType.PUBLICATION, image.getType());
        assertEquals(content.length / 1024, image.getTaille());
    }

    @Test
    void testSaveImagePublicationKO() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("ko_mgl.jpg");
        when(file.getBytes()).thenThrow(new IOException("Erreur agar.io"));
        UUID userId = UUID.randomUUID();

        assertThrows(IOException.class, () -> {
            facadeImage.saveImagePublication(file, ImageType.PUBLICATION, userId);
        });
    }

    @Test
    void testSaveImageAvatarOK() throws IOException {
        byte[] content = "avatar image".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", content);
        UUID userId = UUID.randomUUID();

        when(imageRepository.save(ArgumentMatchers.any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Image image = facadeImage.saveImageAvatar(file, ImageType.AVATAR, userId);
        Path filePath = Paths.get(image.getFilePath());

        assertTrue(Files.exists(filePath), "Le fichier doit exister dans le répertoire de publication");
        assertArrayEquals(content, Files.readAllBytes(filePath), "C'est pas le même contenu fréro");
        assertEquals("avatar.jpg", image.getNom());
        assertEquals(ImageType.AVATAR, image.getType());
        assertEquals(content.length / 1024, image.getTaille());

        verify(rabbitEventSender, times(1)).sendUpdateAvatarEvent(userId, image.getId());
    }

    @Test
    void testSaveImageAvatarKO() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("not_a_porn.jpg");
        when(file.getBytes()).thenThrow(new IOException("Erreur !"));
        UUID userId = UUID.randomUUID();

        assertThrows(IOException.class, () -> {
            facadeImage.saveImageAvatar(file, ImageType.AVATAR, userId);
        });
    }

    @Test
    void testGetImageOK() {
        long dummyId = 123456789L;
        Image image = new Image();
        image.setId(123456789L);
        image.setNom("not_a_porn_too.jpg");
        when(imageRepository.getImageById(dummyId)).thenReturn(image);

        Image returnedImage = facadeImage.getImage(dummyId);

        assertNotNull(returnedImage);
        assertEquals(123456789L, returnedImage.getId());
        assertEquals("not_a_porn_too.jpg", returnedImage.getNom());
    }
}