package com.example.cineversemobile.data.network

import android.util.Log
import com.example.cineversemobile.model.ChatMessage
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage

class StompManager(
    private val userEmail: String,
    private val socketUrl: String,
    private val onMessageReceived: (ChatMessage) -> Unit
) {

    private var mStompClient: StompClient? = null
    private val gson = Gson()

    fun connect() {
        // Log para verificar qué IP está intentando usar la App
        Log.d("STOMP", "Iniciando conexión a: $socketUrl")

        // Configuramos Stomp con la URL recibida del ViewModel
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, socketUrl)

        // Suscripción al ciclo de vida
        mStompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> Log.d("STOMP", "✅ ¡CONECTADO CON ÉXITO!")
                LifecycleEvent.Type.ERROR -> {
                    Log.e("STOMP", "❌ ERROR en conexión: ${lifecycleEvent.exception?.message}")
                    lifecycleEvent.exception?.printStackTrace()
                }
                LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "🔌 CONEXIÓN CERRADA")
                else -> Log.d("STOMP", "Estado: ${lifecycleEvent.type}")
            }
        }

        mStompClient?.connect()

        // Suscripción al canal de mensajes del usuario
        mStompClient?.topic("/topic/messages.$userEmail")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ topicMessage: StompMessage ->
                Log.d("STOMP", "📩 Mensaje recibido del servidor")
                try {
                    val chatMessage = gson.fromJson(topicMessage.payload, ChatMessage::class.java)
                    onMessageReceived(chatMessage)
                } catch (e: Exception) {
                    Log.e("STOMP", "❌ Error al parsear JSON: ${e.message}")
                }
            }, { throwable ->
                Log.e("STOMP", "❌ Error en suscripción", throwable)
            })
    }

    fun sendMessage(content: String) {
        if (mStompClient?.isConnected == false) {
            Log.e("STOMP", "No se puede enviar: Cliente desconectado")
            return
        }

        val message = ChatMessage(
            sender = userEmail,
            content = content,
            recipient = "ADMIN"
        )
        val jsonMessage = gson.toJson(message)

        mStompClient?.send("/app/chat.send", jsonMessage)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d("STOMP", "📤 Mensaje enviado correctamente")
            }, { error ->
                Log.e("STOMP", "❌ Error al enviar mensaje", error)
            })
    }

    fun disconnect() {
        Log.d("STOMP", "Desconectando cliente...")
        mStompClient?.disconnect()
    }
}