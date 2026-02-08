import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

let stompClient = null;

/**
 * Conecta al servidor de WebSockets de forma robusta.
 */
export const connectChat = (userEmail, onMessageReceived, isAdmin = false, onConnectionUpdate = null) => {
    // 1. Limpieza de seguridad: Si ya hay una instancia, desconectar antes de crear otra
    // Esto evita que el cliente reciba mensajes duplicados si el componente se monta dos veces.
    if (stompClient !== null) {
        if (stompClient.connected) {
            stompClient.disconnect();
        }
        stompClient = null;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // 🚀 Silenciamos el debug de STOMP para que no ensucie la consola del navegador
    stompClient.debug = () => {}; 

    // Configuramos heartbeats para mantener la conexión viva (cada 10 segundos)
    stompClient.heartbeat.outgoing = 10000;
    stompClient.heartbeat.incoming = 10000;

    stompClient.connect({}, (frame) => {
        if (onConnectionUpdate) onConnectionUpdate(true);
        
        // Canal: El admin escucha el canal global de empleados, el cliente escucha su buzón privado
        const topic = isAdmin ? '/topic/admin.messages' : `/topic/messages.${userEmail}`;
        
        stompClient.subscribe(topic, (sdkEvent) => {
            try {
                const message = JSON.parse(sdkEvent.body);
                onMessageReceived(message);
            } catch (e) {
                console.error("Error parseando el mensaje recibido:", e);
            }
        });
    }, (error) => {
        console.error("Error de conexión STOMP:", error);
        if (onConnectionUpdate) onConnectionUpdate(false);
    });
};

/**
 * Envía mensajes diferenciando el flujo según el destinatario.
 */
export const sendMessage = (msg) => {
    if (stompClient && stompClient.connected) {
        // 🚦 DECISOR DE RUTAS:
        // Si el destinatario es ADMIN -> El cliente está pidiendo ayuda (chat.send)
        // Si el destinatario es un email -> El empleado está respondiendo (chat.reply)
        const endpoint = msg.recipient === "ADMIN" ? "/app/chat.send" : "/app/chat.reply";
        
        stompClient.send(endpoint, {}, JSON.stringify(msg));
    } else {
        console.warn("No se pudo enviar el mensaje: Socket no conectado.");
    }
};

/**
 * Cierra la conexión y limpia la variable para permitir reconexiones limpias.
 */
export const disconnectChat = () => {
    if (stompClient !== null) {
        try {
            stompClient.disconnect();
        } catch (e) {
            // Error silencioso al desconectar (ya estaba cerrado o el socket murió)
        } finally {
            stompClient = null;
        }
    }
};