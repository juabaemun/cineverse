import React, { useState, useEffect } from 'react';
import { connectChat, sendMessage, disconnectChat } from "../../services/ChatService";

//Panel para el chat

const ChatPanel = ({ userEmail: propEmail }) => {
  const userEmail = propEmail || localStorage.getItem("userEmail") || "";
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    if (userEmail) {
      connectChat(
        userEmail, 
        (newMessage) => {
          setMessages((prev) => [...prev, newMessage]);
        }, 
        false, 
        (status) => setIsConnected(status)
      );
    }
    // Limpieza al salir para evitar mensajes duplicados
    return () => disconnectChat();
  }, [userEmail]);

  const handleSend = () => {
    if (inputMessage.trim() !== "" && isConnected) {
      const msg = {
        sender: userEmail,
        content: inputMessage,
        recipient: "ADMIN",
        type: "CHAT",
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };

      sendMessage(msg);
      setInputMessage(""); 
    }
  };

  return (
    <div className="flex flex-col h-[450px] bg-zinc-800/20 rounded-3xl border border-zinc-800 overflow-hidden">
      <div className="bg-zinc-900/50 px-4 py-2 flex items-center gap-2 border-b border-zinc-800">
        <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
        <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-500">
          {isConnected ? 'Soporte en línea' : 'Conectando...'}
        </span>
      </div>

      <div className="flex-1 p-6 overflow-y-auto flex flex-col gap-3 custom-scrollbar">
        {messages.map((m, idx) => (
          <div key={idx} className={`max-w-[80%] p-3 rounded-2xl text-sm ${
            m.sender === userEmail ? "bg-[#ffe81f] text-black self-end rounded-tr-none" : "bg-zinc-800 text-white self-start rounded-tl-none"
          }`}>
            {m.content}
          </div>
        ))}
      </div>
      
      <div className="p-4 bg-zinc-800/50 border-t border-zinc-700 flex gap-2">
        <input 
          type="text" 
          value={inputMessage}
          disabled={!isConnected}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSend()}
          placeholder={isConnected ? "Escribir mensaje..." : "Esperando conexión..."} 
          className="flex-1 bg-zinc-900 border border-zinc-700 p-3 rounded-xl outline-none text-white text-sm focus:border-[#ffe81f] disabled:opacity-50" 
        />
        <button 
          onClick={handleSend}
          disabled={!isConnected || inputMessage.trim() === ""}
          className="bg-[#ffe81f] text-black px-6 rounded-xl font-bold text-xs uppercase hover:scale-105 transition-transform disabled:grayscale disabled:opacity-50"
        >
          Enviar
        </button>
      </div>
    </div>
  );
};

export default ChatPanel;