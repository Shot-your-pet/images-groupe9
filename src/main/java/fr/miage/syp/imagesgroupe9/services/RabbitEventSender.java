package fr.miage.syp.imagesgroupe9.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.UUID;

@Service
public class RabbitEventSender {

    private final RabbitTemplate rabbitTemplate;


    public RabbitEventSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Message<T>(UUID idDemande, UUID idReponse, T data) implements Serializable {}
    public record MessageNewAvatar(UUID idKeycloak, Long idImage) implements Serializable {}

    public <T> void send(UUID idDemande, UUID idReponse, T data){
        Message message = new Message(idDemande, idReponse, data);
        this.rabbitTemplate.convertAndSend("exchange", "routingkey", message);
    }

    public void sendUpdateAvatarEvent(UUID idKeycloak, long idImage) {
        MessageNewAvatar message = new MessageNewAvatar(idKeycloak, idImage);
        this.rabbitTemplate.convertAndSend("images.update_avatar", message);
    }
}
