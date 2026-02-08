import React, { useState, useEffect, useCallback } from 'react';
import SessionSelector from './SessionSelector';
import SeatPicker from './SeatPicker';
import ChatPanel from './ChatPanel';
import MyBookings from './MyBookings';

// Panel para que el cliente vea sus reservas/entradas
const CustomerPanel = ({ screenings, fetchScreenings, userEmail }) => {
  const [activeTab, setActiveTab] = useState('movies');
  const [selectedScreening, setSelectedScreening] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [occupiedSeats, setOccupiedSeats] = useState([]);
  const [myBookings, setMyBookings] = useState([]);

  const fetchMyBookings = useCallback(async () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
      const response = await fetch("/api/bookings/my-bookings", {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setMyBookings(data);
      }
    } catch (err) {
      console.error("Error cargando mis reservas:", err);
    }
  }, []);

  useEffect(() => {
    fetchMyBookings();
  }, [fetchMyBookings]);

  const fetchOccupiedSeats = async (screeningId) => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`/api/bookings/occupied/${screeningId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) setOccupiedSeats(await response.json());
    } catch (err) {
      console.error("Error cargando asientos ocupados:", err);
    }
  };

  const handleBook = async () => {
    const token = localStorage.getItem("token");
    try {
      for (const seatId of selectedSeats) {
        await fetch("/api/bookings/reserve", {
          method: 'POST',
          headers: { 
            'Content-Type': 'application/json', 
            'Authorization': `Bearer ${token}` 
          },
          body: JSON.stringify({ 
            screeningId: selectedScreening.id, 
            seat: seatId 
          })
        });
      }
      
      alert("¡Compra realizada con éxito!");
      
      setSelectedSeats([]);
      setSelectedScreening(null);
      
      fetchScreenings(); 
      fetchMyBookings(); 
      
    } catch (err) {
      alert("Hubo un error al procesar la reserva.");
    }
  };

  const toggleSeat = (seatId) => {
    setSelectedSeats(prev => 
      prev.includes(seatId) ? prev.filter(s => s !== seatId) : [...prev, seatId]
    );
  };

  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-8 shadow-2xl">
      <div className="flex gap-8 mb-8 border-b border-zinc-800 pb-4">
        <button 
          onClick={() => setActiveTab('movies')} 
          className={`text-xs font-black tracking-[0.2em] uppercase transition-all ${activeTab === 'movies' ? 'text-[#ffe81f] scale-110' : 'text-zinc-500 hover:text-white'}`}
        >
          Cartelera y Compra
        </button>
        <button 
          onClick={() => setActiveTab('chat')} 
          className={`text-xs font-black tracking-[0.2em] uppercase transition-all ${activeTab === 'chat' ? 'text-[#ffe81f] scale-110' : 'text-zinc-500 hover:text-white'}`}
        >
          Soporte Online
        </button>
      </div>

      {activeTab === 'movies' && (
        <div className="animate-in fade-in slide-in-from-top-4 duration-500">
          {!selectedScreening ? (
            <div className="space-y-16">
              <section>
                <div className="flex items-center gap-4 mb-8">
                  <div className="h-px flex-1 bg-zinc-800"></div>
                  <h3 className="text-zinc-400 font-black uppercase tracking-[0.3em] text-[10px]">Sesiones Disponibles</h3>
                  <div className="h-px flex-1 bg-zinc-800"></div>
                </div>
                <SessionSelector 
                  screenings={screenings} 
                  onSelect={(s) => {
                    setSelectedScreening(s);
                    fetchOccupiedSeats(s.id);
                  }} 
                />
              </section>

              <section className="pt-8">
                <MyBookings bookings={myBookings} />
              </section>
            </div>
          ) : (
            <div className="animate-in slide-in-from-right duration-500">
              <div className="flex justify-between items-end mb-10">
                <button 
                  onClick={() => { setSelectedScreening(null); setSelectedSeats([]); }} 
                  className="group flex items-center gap-2 text-zinc-500 text-[10px] font-black uppercase hover:text-[#ffe81f] transition-all"
                >
                  <span className="text-lg group-hover:-translate-x-1 transition-transform">←</span> Volver
                </button>
                <div className="text-right border-r-4 border-[#ffe81f] pr-4">
                  <p className="text-[10px] text-zinc-500 font-black uppercase tracking-widest">Película seleccionada</p>
                  <h2 className="text-2xl font-black text-white italic">{selectedScreening.movie.title}</h2>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
                <div className="lg:col-span-2 bg-zinc-950/50 p-8 rounded-[40px] border border-zinc-800/50">
                  <SeatPicker 
                    room={selectedScreening.room} 
                    occupiedSeats={occupiedSeats} 
                    selectedSeats={selectedSeats} 
                    onToggleSeat={toggleSeat} 
                  />
                </div>

                <div className="bg-zinc-800/40 p-8 rounded-[40px] border border-zinc-700 h-fit sticky top-8">
                  <h4 className="text-[10px] font-black text-[#ffe81f] uppercase mb-6 tracking-[0.2em] border-b border-zinc-700 pb-2">Ticket Summary</h4>
                  
                  <div className="space-y-3 mb-8 max-h-[200px] overflow-y-auto pr-2 custom-scrollbar">
                    {selectedSeats.length > 0 ? (
                      selectedSeats.map(seat => (
                        <div key={seat} className="flex justify-between items-center text-xs font-bold bg-zinc-900/80 p-4 rounded-2xl border border-zinc-800 animate-in zoom-in-95">
                          <span className="text-zinc-400 uppercase tracking-tighter">Butaca {seat}</span>
                          <span className="text-[#ffe81f]">{selectedScreening.price.toFixed(2)}€</span>
                        </div>
                      ))
                    ) : (
                      <div className="text-zinc-600 text-[10px] font-bold uppercase py-10 text-center border-2 border-dashed border-zinc-700/50 rounded-3xl">
                        Ninguna butaca <br/> seleccionada
                      </div>
                    )}
                  </div>

                  <div className="mb-8 flex justify-between items-end px-2">
                    <span className="text-zinc-500 font-black uppercase text-[10px] tracking-widest">Total</span>
                    <span className="text-4xl font-black text-white">
                      {(selectedSeats.length * selectedScreening.price).toFixed(2)}€
                    </span>
                  </div>
                  
                  <button 
                    onClick={handleBook}
                    disabled={selectedSeats.length === 0}
                    className="w-full bg-[#ffe81f] text-black py-5 rounded-2xl font-black text-[11px] tracking-[0.2em] uppercase hover:bg-white hover:shadow-[0_0_30px_rgba(255,255,255,0.1)] active:scale-95 transition-all disabled:opacity-10 disabled:grayscale"
                  >
                    Finalizar Compra
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'chat' && (
        <div className="animate-in fade-in zoom-in-95 duration-500 h-[600px]">
          {/* Pasamos la prop userEmail al ChatPanel */}
          <ChatPanel userEmail={userEmail} />
        </div>
      )}
    </div>
  );
};

export default CustomerPanel;