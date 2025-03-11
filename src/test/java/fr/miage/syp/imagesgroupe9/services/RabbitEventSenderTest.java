package fr.miage.syp.imagesgroupe9.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitEventSenderTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitEventSender rabbitEventSender;

    @Test
    public void testSend() {
        UUID idDemande = UUID.randomUUID();
        UUID idReponse = UUID.randomUUID();
        String data = "Blc des tests";

        rabbitEventSender.send(idDemande, idReponse, data);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("exchange"), eq("routingkey"), messageCaptor.capture());

        Object messageObject = messageCaptor.getValue();

        RabbitEventSender.Message<String> message = (RabbitEventSender.Message<String>) messageObject;
        assertEquals(idDemande, message.idDemande());
        assertEquals(idReponse, message.idReponse());
        assertEquals(data, message.data());
    }

    @Test
    public void testSendUpdateAvatarEvent() {
        UUID idKeycloak = UUID.randomUUID();
        long idImage = 123456789L;

        rabbitEventSender.sendUpdateAvatarEvent(idKeycloak, idImage);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("images.update_avatar"), messageCaptor.capture());

        Object messageObject = messageCaptor.getValue();
        RabbitEventSender.MessageNewAvatar message = (RabbitEventSender.MessageNewAvatar) messageObject;
        assertEquals(idKeycloak, message.idKeycloak());
        assertEquals(idImage, message.idImage());
    }
}