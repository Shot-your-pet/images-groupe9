package fr.miage.syp.imagesgroupe9.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.miage.syp.imagesgroupe9.model.UtilisateurDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Service
public class RabbitEventSender {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;


    public RabbitEventSender(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    // Uniquement pour le test de convertSendAndReceive coté utilisateur service
    public record DemandeInfosUtilisateurs(List<UUID> idsKeycloak) implements Serializable { }
    public record DemandeInfosUtilisateur(UUID idKeycloak) implements Serializable { }

    public UtilisateurDTO getUtilisateurFromUtilisateurServiceTest(UUID id) {
        try {
            UtilisateurDTO utilisateurDTO = rabbitTemplate.convertSendAndReceiveAsType("utilisateurs.infos_utilisateur", new DemandeInfosUtilisateur(id), new ParameterizedTypeReference<UtilisateurDTO>() {
            });
            return utilisateurDTO;
        } catch (Exception e) {
            return null;
        }
    }

    public List<UtilisateurDTO> getUtilisateursFromUtilisateurServiceTest(List<UUID> ids) {
        try {
            List<UtilisateurDTO> utilisateurDTO = rabbitTemplate.convertSendAndReceiveAsType("utilisateurs.infos_utilisateurs", new DemandeInfosUtilisateurs(ids), new ParameterizedTypeReference<List<UtilisateurDTO>>() {
            });
            return utilisateurDTO;
        } catch (Exception e) {
            return null;
        }
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
