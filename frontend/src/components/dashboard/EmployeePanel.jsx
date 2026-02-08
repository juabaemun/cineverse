import React, { useState, useEffect } from 'react';
import SessionSelector from './SessionSelector';
import SeatPicker from './SeatPicker';
import TicketList from './TicketList';
import EmployeeChatPanel from './EmployeeChatPanel';

const EmployeePanel = ({ screenings, fetchScreenings }) => {
  const [activeSubTab, setActiveSubTab] = useState('sell');
  
  // Obtenemos el usuario real del localStorage o un fallback de seguridad
  const userString = localStorage.getItem("user");
  const user = userString ? JSON.parse(userString) : { email: "empleado@cineverse.com" };

  // Estados para Venta Manual
  const [selectedScreening, setSelectedScreening] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [occupiedSeats, setOccupiedSeats] = useState([]);

  // Estados para Validación
  const [selectedValScreening, setSelectedValScreening] = useState(null);
  const [sessionBookings, setSessionBookings] = useState([]);

  useEffect(() => {
    if (selectedScreening) fetchOccupiedSeats(selectedScreening.id);
  }, [selectedScreening]);

  const fetchOccupiedSeats = async (screeningId) => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`/api/bookings/occupied/${screeningId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) setOccupiedSeats(await response.json());
    } catch (err) { console.error("Error asientos:", err); }
  };

  const handleFinalizeSale = async () => {
    const token = localStorage.getItem("token");
    try {
      for (const seatId of selectedSeats) {
        await fetch("/api/bookings/reserve", {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
          body: JSON.stringify({ screeningId: selectedScreening.id, seat: seatId })
        });
      }
      alert("¡Venta completada!");
      setSelectedSeats([]);
      setSelectedScreening(null);
      fetchScreenings();
    } catch (err) { alert("Error en la venta"); }
  };

  const fetchSessionBookings = async (screeningId) => {
    const token = localStorage.getItem("token");
    const response = await fetch(`/api/bookings/screening/${screeningId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (response.ok) setSessionBookings(await response.json());
  };

  const handleValidateTicket = async (id) => {
    const token = localStorage.getItem("token");
    const response = await fetch(`/api/bookings/validate/${id}`, {
      method: 'PATCH',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (response.ok) fetchSessionBookings(selectedValScreening.id);
  };

  return (
    <div className="bg-zinc-900 border border-zinc-800 rounded-3xl p-8 shadow-2xl">
      {/* Tabs */}
      <div className="flex gap-6 mb-8 border-b border-zinc-800 pb-4">
        {['sell', 'validate', 'chat'].map((tab) => (
          <button 
            key={tab}
            onClick={() => setActiveSubTab(tab)} 
            className={`text-xs font-black tracking-widest uppercase transition-colors ${
              activeSubTab === tab ? 'text-[#ffe81f]' : 'text-zinc-500 hover:text-zinc-300'
            }`}
          >
            {tab === 'sell' && 'Taquilla'}
            {tab === 'validate' && 'Validar Acceso'}
            {tab === 'chat' && 'Chat Soporte'}
          </button>
        ))}
      </div>

      {activeSubTab === 'sell' && (
        !selectedScreening ? (
          <SessionSelector screenings={screenings} onSelect={setSelectedScreening} />
        ) : (
          <div className="animate-in fade-in zoom-in-95 duration-500">
            <button onClick={() => setSelectedScreening(null)} className="text-zinc-500 text-[10px] mb-4 uppercase hover:text-white transition-colors">← Volver</button>
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
              <div className="lg:col-span-2">
                <SeatPicker 
                  room={selectedScreening.room} 
                  occupiedSeats={occupiedSeats} 
                  selectedSeats={selectedSeats} 
                  onToggleSeat={(id) => setSelectedSeats(prev => prev.includes(id) ? prev.filter(s => s !== id) : [...prev, id])} 
                />
              </div>
              <div className="bg-zinc-800/50 p-6 rounded-3xl border border-zinc-700 h-fit">
                <h4 className="text-sm font-black mb-4 uppercase text-[#ffe81f]">Venta en Ventanilla</h4>
                <p className="text-xl font-bold">{selectedScreening.movie.title}</p>
                <div className="space-y-2 my-6">
                  {selectedSeats.map(s => (
                    <div key={s} className="flex justify-between text-xs font-bold bg-zinc-900 p-2 rounded-lg">
                      <span>Asiento {s}</span><span>{selectedScreening.price}€</span>
                    </div>
                  ))}
                </div>
                <div className="border-t border-zinc-700 pt-4 mb-6 flex justify-between items-end">
                  <span className="text-3xl font-black text-[#ffe81f]">{(selectedSeats.length * selectedScreening.price).toFixed(2)}€</span>
                </div>
                <button 
                    onClick={handleFinalizeSale} 
                    disabled={selectedSeats.length === 0} 
                    className="w-full bg-[#ffe81f] text-black py-4 rounded-xl font-black text-xs hover:scale-105 transition-transform disabled:opacity-50"
                >
                    COBRAR Y ENTREGAR
                </button>
              </div>
            </div>
          </div>
        )
      )}

      {activeSubTab === 'validate' && (
        !selectedValScreening ? (
          <SessionSelector screenings={screenings} onSelect={(s) => { setSelectedValScreening(s); fetchSessionBookings(s.id); }} />
        ) : (
          <div className="animate-in fade-in slide-in-from-left-4 duration-500">
            <button onClick={() => setSelectedValScreening(null)} className="text-zinc-500 text-[10px] mb-4 uppercase hover:text-white transition-colors">← Volver</button>
            <TicketList bookings={sessionBookings} onValidate={handleValidateTicket} />
          </div>
        )
      )}

      {activeSubTab === 'chat' && (
        <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
          {/* Pasar el email del empleado logueado */}
          <EmployeeChatPanel employeeEmail={user.email} />
        </div>
      )}
    </div>
  );
};

export default EmployeePanel;