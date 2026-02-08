package com.cineverse.api.controllers; // Ajusta a tu paquete

import com.cineverse.api.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * El cliente envía mensajes aquí (/app/chat.send)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        logger.info("📩 MENSAJE RECIBIDO de [{}]: {}",
                chatMessage.getSender(),
                chatMessage.getContent());

        // 1. Lo enviamos al canal privado del cliente (para que él vea sus propios mensajes)
        messagingTemplate.convertAndSend("/topic/messages." + chatMessage.getSender(), chatMessage);

        // 2. Lo enviamos al canal global de administración (para que todos los empleados lo vean)
        messagingTemplate.convertAndSend("/topic/admin.messages", chatMessage);

        logger.debug("Redirigiendo mensaje a /topic/messages.{} y /topic/admin.messages", chatMessage.getSender());
    }

    /**
     * El empleado responde aquí (/app/chat.reply)
     */
    @MessageMapping("/chat.reply")
    public void replyMessage(@Payload ChatMessage chatMessage) {
        logger.info("📤 EMPLEADO [{}] responde a [{}]: {}",
                chatMessage.getSender(),
                chatMessage.getRecipient(),
                chatMessage.getContent());

        // 1. Enviamos la respuesta al cliente (esto ya funcionaba)
        messagingTemplate.convertAndSend("/topic/messages." + chatMessage.getRecipient(), chatMessage);

        // 2. NUEVO: Enviamos la respuesta también al canal de administración
        // para que el empleado (y otros compañeros) vean la respuesta en su panel.
        messagingTemplate.convertAndSend("/topic/admin.messages", chatMessage);
    }
}