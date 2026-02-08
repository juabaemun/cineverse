import React, { useState, useEffect } from 'react';

const ScreeningsAdmin = ({ movies: initialMovies }) => { // Renombramos la prop
  const [screenings, setScreenings] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [movies, setMovies] = useState(initialMovies || []); // Estado local para asegurar que existan
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingScreening, setEditingScreening] = useState(null);
  const [formData, setFormData] = useState({ movieId: '', roomId: '', startTime: '', price: 9.50 });

  useEffect(() => { 
    fetchScreenings(); 
    fetchRooms();
    // Si la prop movies cambia o no existe, intentamos cargar películas aquí también
    if (!initialMovies || initialMovies.length === 0) {
      fetchMovies();
    } else {
      setMovies(initialMovies);
    }
  }, [initialMovies]);

  // función para asegurar que las películas se carguen si la prop falla
  const fetchMovies = async () => {
    const response = await fetch("/api/movies", {
      headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
    });
    if (response.ok) setMovies(await response.json());
  };

  const fetchRooms = async () => {
    // El AdminController usa /api/admin/rooms
    const response = await fetch("/api/admin/rooms", {
      headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
    });
    if (response.ok) setRooms(await response.json());
  };

  const fetchScreenings = async () => {
    const response = await fetch("/api/screenings", {
      headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
    });
    if (response.ok) setScreenings(await response.json());
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const method = editingScreening ? 'PUT' : 'POST';
    
    const url = editingScreening 
      ? `/api/screenings/${editingScreening.id}` 
      : "/api/screenings/create"; 

    const dataToSend = {
      movieId: parseInt(formData.movieId),
      roomId: parseInt(formData.roomId),
      startTime: formData.startTime,
      price: parseFloat(formData.price)
    };

    const response = await fetch(url, {
      method,
      headers: { 
        'Content-Type': 'application/json', 
        'Authorization': `Bearer ${token}` 
      },
      body: JSON.stringify(dataToSend)
    });

    if (response.ok) { 
    setIsModalOpen(false); 
    fetchScreenings(); 
    } else if (response.status === 409) {
        alert("¡Conflicto! Esa sala ya está ocupada a esa hora.");
    } else {
        alert("Error al guardar: " + response.status); 
    }
  };

  const handleDeleteScreening = async (id) => {
    if (!id || !confirm("¿Seguro que quieres eliminar esta sesión?")) return;
    const token = localStorage.getItem("token");
    
    try {
      const response = await fetch(`/api/admin/screenings/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (response.ok) {
        setScreenings(prev => prev.filter(s => s.id !== id));
      }
    } catch (err) {
      console.error("Error de red:", err);
    }
  };

  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-8">
      <div className="flex justify-between items-center mb-8">
        <h3 className="text-2xl font-black text-[#ffe81f]">SESIONES</h3>
        <button onClick={() => { setEditingScreening(null); setFormData({movieId:'', roomId:'', startTime:'', price:9.50}); setIsModalOpen(true); }} className="bg-white text-black px-5 py-2 rounded-xl font-bold text-sm">+ PROGRAMAR</button>
      </div>
      
      <div className="space-y-4">
        {screenings.map(s => (
          <div key={s.id} className="flex items-center justify-between bg-zinc-800/50 p-6 rounded-2xl border border-zinc-700">
            <div>
              <p className="font-black text-[#ffe81f] text-lg">{s.movie?.title}</p>
              <p className="text-xs text-zinc-400 font-bold uppercase">{s.room?.name} • {new Date(s.startTime).toLocaleString()} • {s.price}€</p>
            </div>
            <div className="flex gap-4">
              <button onClick={() => { 
                setEditingScreening(s); 
                setFormData({
                  movieId: s.movie?.id || '', 
                  roomId: s.room?.id || '', 
                  startTime: s.startTime?.substring(0,16) || '', 
                  price: s.price
                }); 
                setIsModalOpen(true); 
              }} className="text-blue-400 text-[10px] font-black">EDITAR</button>
              <button onClick={() => handleDeleteScreening(s.id)} className="text-red-500 text-[10px] font-black hover:bg-red-500/10 p-2 rounded-lg transition-colors">BORRAR</button>
            </div>
          </div>
        ))}
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black/90 backdrop-blur-sm flex items-center justify-center p-4 z-[80]">
          <form onSubmit={handleSave} className="bg-zinc-900 border border-zinc-800 p-8 rounded-3xl max-w-md w-full text-white shadow-2xl">
            <h4 className="text-xl font-black mb-6 text-[#ffe81f] uppercase tracking-tighter">Gestionar Sesión</h4>
            
            <select className="w-full bg-zinc-800 p-3 rounded-xl border border-zinc-700 mb-4" value={formData.movieId} onChange={e => setFormData({...formData, movieId: e.target.value})} required>
              <option value="">Selecciona Película...</option>
              {/* Usamos el estado local movies que ya está validado */}
              {movies && movies.length > 0 ? (
                movies.map((movie) => (
                  <option key={movie.id} value={movie.id}>{movie.title}</option>
                ))
              ) : (
                <option disabled>Cargando películas...</option>
              )}
            </select>

            <select className="w-full bg-zinc-800 p-3 rounded-xl border border-zinc-700 mb-4" value={formData.roomId} onChange={e => setFormData({...formData, roomId: e.target.value})} required>
              <option value="">Selecciona Sala...</option>
              {rooms.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
            </select>

            <input type="datetime-local" className="w-full bg-zinc-800 p-3 rounded-xl border border-zinc-700 mb-4" value={formData.startTime} onChange={e => setFormData({...formData, startTime: e.target.value})} required />
            <input type="number" step="0.01" className="w-full bg-zinc-800 p-3 rounded-xl border border-zinc-700 mb-6" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} required />
            
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

export default ScreeningsAdmin;