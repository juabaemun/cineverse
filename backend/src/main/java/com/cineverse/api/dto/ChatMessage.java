package com.cineverse.api.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String sender;    // Email de quien envía
    private String content;   // Texto del mensaje
    private String timestamp; // Hora formateada
    private String recipient; // Email del destinatario (usado por el empleado para responder)
    private MessageType type; // Tipo de mensaje (CHAT, JOIN, LEAVE)

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    // Constructor vacío (Obligatorio para que Spring pueda procesar el JSON)
    public ChatMessage() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // Constructor completo
    public ChatMessage(String sender, String content, String recipient, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.recipient = recipient;
        this.type = type;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // --- GETTERS Y SETTERS ---

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}