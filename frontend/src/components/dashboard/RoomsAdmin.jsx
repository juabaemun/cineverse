import React, { useState, useEffect } from 'react';

const RoomsAdmin = () => {
  const [rooms, setRooms] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState(null);
  const [formData, setFormData] = useState({ name: '', rowsCount: 0, seatsPerRow: 0 });

  useEffect(() => { fetchRooms(); }, []);

  const fetchRooms = async () => {
    const token = localStorage.getItem("token");
    const response = await fetch("/api/rooms", {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (response.ok) setRooms(await response.json());
  };

  const handleSaveRoom = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const method = editingRoom ? 'PUT' : 'POST';
    const url = editingRoom ? `/api/rooms/${editingRoom.id}` : "/api/rooms/create";

    await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(formData)
    });
    setIsModalOpen(false); fetchRooms();
  };

  const handleDeleteRoom = async (id) => {
    if (!confirm("¿Eliminar sala?")) return;
    const response = await fetch(`/api/rooms/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
    });
    if (response.ok) fetchRooms();
    else alert("No se puede eliminar: tiene dependencias.");
  };

  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-8">
      <div className="flex justify-between items-center mb-8">
        <h3 className="text-2xl font-black text-[#ffe81f]">GESTIÓN DE SALAS</h3>
        <button onClick={() => { setEditingRoom(null); setFormData({name:'', rowsCount:0, seatsPerRow:0}); setIsModalOpen(true); }} className="bg-white text-black px-5 py-2 rounded-xl font-bold text-sm">+ NUEVA SALA</button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {rooms.map(room => (
          <div key={room.id} className="bg-zinc-800/50 border border-zinc-700 p-6 rounded-2xl group relative">
            <div className="absolute top-4 right-4 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
               <button onClick={() => { setEditingRoom(room); setFormData(room); setIsModalOpen(true); }} className="p-2 bg-blue-500/20 text-blue-400 rounded-lg text-[10px] font-bold">EDITAR</button>
               <button onClick={() => handleDeleteRoom(room.id)} className="p-2 bg-red-500/20 text-red-400 rounded-lg text-[10px] font-bold">BORRAR</button>
            </div>
            <h4 className="text-xl font-bold text-white mb-2">{room.name}</h4>
            <div className="flex justify-between text-zinc-400 text-xs font-bold uppercase">
              <span>Capacidad: {room.capacity}</span>
              <span>{room.rowsCount} x {room.seatsPerRow}</span>
            </div>
          </div>
        ))}
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black/90 backdrop-blur-sm flex items-center justify-center p-4 z-[70]">
          <form onSubmit={handleSaveRoom} className="bg-zinc-900 border border-zinc-800 p-8 rounded-3xl max-w-sm w-full text-white">
            <h4 className="text-xl font-black mb-6 text-[#ffe81f] uppercase">{editingRoom ? 'Editar Sala' : 'Nueva Sala'}</h4>
            <input type="text" placeholder="Nombre" className="w-full bg-zinc-800 p-3 rounded-xl border border-zinc-700 mb-4 outline-none" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} required />
            <div className="grid grid-cols-2 gap-4 mb-4">
              <input type="number" placeholder="Filas" className="bg-zinc-800 p-3 rounded-xl border border-zinc-700 outline-none" value={formData.rowsCount} onChange={e => setFormData({...formData, rowsCount: parseInt(e.target.value)})} required />
              <input type="number" placeholder="Butacas" className="bg-zinc-800 p-3 rounded-xl border border-zinc-700 outline-none" value={formData.seatsPerRow} onChange={e => setFormData({...formData, seatsPerRow: parseInt(e.target.value)})} required />
            </div>
            <div className="flex gap-3">
              <button type="button" onClick={() => setIsModalOpen(false)} className="flex-1 py-3 bg-zinc-800 rounded-xl font-bold text-zinc-400">CANCELAR</button>
              <button type="submit" className="flex-1 py-3 bg-[#ffe81f] text-black rounded-xl font-bold">GUARDAR</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
export default RoomsAdmin;