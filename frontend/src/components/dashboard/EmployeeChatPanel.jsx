import React, { useState, useEffect } from 'react';
import { connectChat, sendMessage, disconnectChat } from "../../services/ChatService";

const EmployeeChatPanel = ({ employeeEmail }) => {
  const [chats, setChats] = useState({});
  const [unreadCounts, setUnreadCounts] = useState({}); 
  const [selectedChat, setSelectedChat] = useState(null);
  const [reply, setReply] = useState("");
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    if (employeeEmail) {
      connectChat(
        employeeEmail, 
        (msg) => {
          // Identificar el email del cliente (si es entrada es sender, si es salida es recipient)
          const clientEmail = (msg.recipient === "ADMIN") ? msg.sender : msg.recipient;
          
          // 1. Actualizar el historial de mensajes
          setChats((prevChats) => {
            const currentChatMessages = prevChats[clientEmail] || [];
            return {
              ...prevChats,
              [clientEmail]: [...currentChatMessages, msg]
            };
          });

          // 2. Gestionar notificaciones (solo si el mensaje viene del cliente y no tenemos ese chat abierto)
          if (msg.recipient === "ADMIN" && clientEmail !== selectedChat) {
            setUnreadCounts(prev => ({
              ...prev,
              [clientEmail]: (prev[clientEmail] || 0) + 1
            }));
          }
        }, 
        true, // Modo ADMIN activo para escuchar /topic/admin.messages
        (status) => setIsConnected(status)
      );
    }
    return () => disconnectChat();
  }, [employeeEmail, selectedChat]); // Re-suscribir lógicamente para conocer el selectedChat actual

  const handleSelectChat = (email) => {
    setSelectedChat(email);
    // Limpiar notificaciones al seleccionar el chat
    setUnreadCounts(prev => ({
      ...prev,
      [email]: 0
    }));
  };

  const handleReply = () => {
    if (reply.trim() !== "" && selectedChat && isConnected) {
      const msg = {
        sender: employeeEmail,
        content: reply,
        recipient: selectedChat, 
        type: "CHAT",
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      
      sendMessage(msg); 
      setReply("");
    }
  };

  const clientEmails = Object.keys(chats);

  return (
    <div className="flex h-[500px] bg-zinc-900 border border-zinc-800 rounded-3xl overflow-hidden">
      {/* Sidebar: Lista de Clientes */}
      <div className="w-1/3 border-r border-zinc-800 bg-zinc-950/50 p-4 overflow-y-auto custom-scrollbar">
        <div className="flex items-center gap-2 mb-4">
            <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
            <h3 className="text-[#ffe81f] text-xs font-black uppercase tracking-widest">Conversaciones</h3>
        </div>
        
        {clientEmails.length === 0 ? (
          <p className="text-zinc-600 text-xs italic">Esperando mensajes...</p>
        ) : (
          clientEmails.map(email => (
            <button 
              key={email}
              onClick={() => handleSelectChat(email)}
              className={`w-full text-left p-3 rounded-xl mb-2 flex justify-between items-center transition-all ${
                selectedChat === email ? "bg-[#ffe81f] text-black" : "bg-zinc-800 text-zinc-400 hover:bg-zinc-700"
              }`}
            >
              <div className="truncate pr-2">
                <p className="text-xs font-bold truncate">{email}</p>
                <p className="text-[10px] opacity-70">{chats[email].length} mensajes</p>
              </div>
              
              {/* Notificación visual 🟡 */}
              {unreadCounts[email] > 0 && (
                <span className="bg-red-500 text-white text-[9px] font-black px-1.5 py-0.5 rounded-full animate-bounce">
                  {unreadCounts[email]}
                </span>
              )}
            </button>
          ))
        )}
      </div>

      {/* Ventana de Chat */}
      <div className="flex-1 flex flex-col">
        {selectedChat ? (
          <>
            <div className="p-4 border-b border-zinc-800 bg-zinc-800/20 flex justify-between items-center">
              <div>
                <p className="text-xs text-zinc-500 uppercase font-black">Soporte Activo</p>
                <p className="text-[#ffe81f] font-bold">{selectedChat}</p>
              </div>
            </div>
            
            <div className="flex-1 p-4 overflow-y-auto flex flex-col gap-3 custom-scrollbar">
              {chats[selectedChat].map((m, idx) => (
                <div 
                  key={idx} 
                  className={`max-w-[85%] p-3 rounded-2xl text-sm ${
                    m.sender === employeeEmail 
                    ? "bg-zinc-100 text-black self-end rounded-tr-none" 
                    : "bg-zinc-800 text-white self-start rounded-tl-none border border-zinc-700"
                  }`}
                >
                  <p>{m.content}</p>
                  <p className="text-[9px] opacity-40 mt-1 text-right">{m.timestamp}</p>
                </div>
              ))}
            </div>

            <div className="p-4 bg-black/40 border-t border-zinc-800 flex gap-2">
              <input 
                type="text" 
                value={reply}
                disabled={!isConnected}
                onChange={(e) => setReply(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleReply()}
                placeholder={`Responder a ${selectedChat.split('@')[0]}...`}
                className="flex-1 bg-zinc-950 border border-zinc-800 p-3 rounded-xl outline-none text-white text-sm focus:border-[#ffe81f] disabled:opacity-50" 
              />
              <button 
                onClick={handleReply} 
                disabled={!isConnected || reply.trim() === ""}
                className="bg-[#ffe81f] text-black px-4 rounded-xl font-bold text-xs uppercase hover:scale-105 transition-transform disabled:opacity-50"
              >
                Enviar
              </button>
            </div>
          </>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-zinc-600 space-y-2">
            <div className="w-12 h-12 rounded-full border-2 border-dashed border-zinc-800 flex items-center justify-center text-xl">✉️</div>
            <p className="text-sm italic">Selecciona un cliente para comenzar</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default EmployeeChatPanel;