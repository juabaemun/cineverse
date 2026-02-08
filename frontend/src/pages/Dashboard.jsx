import React, { useState, useEffect } from 'react';

// Componentes de Administración
import AdminTabs from '../components/dashboard/AdminTabs';
import MovieGrid from '../components/dashboard/MovieGrid';
import MovieFormModal from '../components/dashboard/MovieFormModal';
import UsersAdmin from '../components/dashboard/UsersAdmin';
import RoomsAdmin from '../components/dashboard/RoomsAdmin';
import ScreeningsAdmin from '../components/dashboard/ScreeningsAdmin';

// Componentes de Staff y Cliente
import EmployeePanel from '../components/dashboard/EmployeePanel';
import CustomerPanel from '../components/dashboard/CustomerPanel';

const Dashboard = () => {
  const [movies, setMovies] = useState([]);
  const [screenings, setScreenings] = useState([]);
  const [loading, setLoading] = useState(false); // Estado para la importación
  const [userRole, setUserRole] = useState(localStorage.getItem("role") || "");
  const [userEmail, setUserEmail] = useState(localStorage.getItem("email") || "");

  const [activeTab, setActiveTab] = useState('movies');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentMovie, setCurrentMovie] = useState({ id: null, title: '', synopsis: '', duration: '', imageUrl: '' });

  useEffect(() => {
    fetchMovies();
    fetchScreenings();
    
    const storedRole = localStorage.getItem("role");
    const storedEmail = localStorage.getItem("email");
    
    if (storedRole) setUserRole(storedRole);
    if (storedEmail) setUserEmail(storedEmail);

    console.log("👤 Sesión iniciada como:", storedEmail, "con rol:", storedRole);
  }, []);

  const fetchMovies = async () => {
    try {
      const response = await fetch("/api/movies");
      const data = await response.json();
      setMovies(data);
    } catch (err) { console.error("Error movies:", err); }
  };

  const fetchScreenings = async () => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch("/api/screenings", {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) setScreenings(await response.json());
    } catch (err) { console.error("Error screenings:", err); }
  };

  // Función para importar desde SWAPI
  const handleImportSWAPI = async () => {
    setLoading(true);
    const token = localStorage.getItem("token");
    try {
      const response = await fetch("/api/movies/import", {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        alert("✨ Películas de Star Wars importadas con éxito");
        fetchMovies();
      } else {
        alert("Error al importar películas.");
      }
    } catch (err) {
      console.error("Error SWAPI:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveMovie = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const movieData = { ...currentMovie, duration: parseInt(currentMovie.duration) };
    const url = isEditing ? `/api/movies/${currentMovie.id}` : "/api/movies";
    
    try {
      const response = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(movieData)
      });
      if (response.ok) { setIsModalOpen(false); fetchMovies(); }
    } catch (err) { console.error(err); }
  };

  const handleDelete = async (id) => {
    if (!confirm("¿Borrar película?")) return;
    const token = localStorage.getItem("token");
    await fetch(`/api/movies/${id}`, {
      method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
    });
    fetchMovies();
  };

  const renderContent = () => {
    switch (userRole) {
      case 'ADMIN':
        return (
          <>
            <AdminTabs activeTab={activeTab} setActiveTab={setActiveTab} />
            {activeTab === 'movies' && (
              <div className="animate-in">
                <div className="flex justify-between items-end mb-12">
                  <div>
                    <h2 className="text-4xl font-black tracking-tighter text-[#ffe81f]">PANEL CONTROL</h2>
                    <p className="text-zinc-500 text-sm mt-1 uppercase tracking-widest font-bold">Gestión Global CineVerse</p>
                  </div>
                  
                  <div className="flex gap-4">
                    {/* Botón SWAPI */}
                    <button 
                      onClick={handleImportSWAPI}
                      disabled={loading}
                      className="bg-zinc-800 text-[#ffe81f] border border-zinc-700 hover:border-[#ffe81f] px-6 py-3 rounded-xl font-black text-xs uppercase transition-all disabled:opacity-50"
                    >
                      {loading ? 'IMPORTANDO...' : '✨ IMPORTAR PELÍCULAS SWAPI'}
                    </button>

                    {/* Botón Nueva Película */}
                    <button 
                      onClick={() => { setIsEditing(false); setCurrentMovie({title:'', synopsis:'', duration:'', imageUrl:''}); setIsModalOpen(true); }} 
                      className="bg-white text-black hover:bg-[#ffe81f] px-6 py-3 rounded-xl font-black text-xs uppercase transition-all transform hover:scale-105"
                    >
                      + NUEVA PELÍCULA
                    </button>
                  </div>
                </div>
                <MovieGrid movies={movies} onEdit={(m) => { setCurrentMovie(m); setIsEditing(true); setIsModalOpen(true); }} onDelete={handleDelete} />
              </div>
            )}
            {activeTab === 'users' && <UsersAdmin />}
            {activeTab === 'rooms' && <RoomsAdmin />}
            {activeTab === 'screenings' && <ScreeningsAdmin movies={movies} />}
          </>
        );

      case 'EMPLOYEE':
        return (
          <div className="animate-in">
            <div className="mb-8">
              <h2 className="text-4xl font-black tracking-tighter text-[#ffe81f]">PANEL STAFF</h2>
              <p className="text-zinc-500 text-sm mt-1 uppercase tracking-widest font-bold">Operaciones de Cine</p>
            </div>
            <EmployeePanel screenings={screenings} fetchScreenings={fetchScreenings} userEmail={userEmail} />
          </div>
        );

      case 'CLIENT':
      default:
        return (
          <div className="animate-in">
            <div className="mb-8">
              <h2 className="text-4xl font-black tracking-tighter text-[#ffe81f]">HOLA, BIENVENIDO</h2>
              <p className="text-zinc-500 text-sm mt-1 uppercase tracking-widest font-bold">Cartelera y Reservas CineVerse</p>
            </div>
            <CustomerPanel screenings={screenings} fetchScreenings={fetchScreenings} userEmail={userEmail} />
          </div>
        );
    }
  };

  return (
    <div className="p-8 max-w-7xl mx-auto text-white">
      {renderContent()}

      {isModalOpen && (
        <MovieFormModal 
          isEditing={isEditing} 
          currentMovie={currentMovie} 
          setCurrentMovie={setCurrentMovie} 
          onSave={handleSaveMovie} 
          onClose={() => setIsModalOpen(false)} 
        />
      )}
    </div>
  );
};

export default Dashboard;