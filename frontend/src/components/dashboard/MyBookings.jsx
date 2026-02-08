import React from 'react';
import TicketPDF, { generateTicketPDF } from './TicketPDF';

const MyBookings = ({ bookings }) => {
  return (
    <div className="mt-12 animate-in fade-in slide-in-from-bottom duration-700">
      <h3 className="text-[#ffe81f] font-black mb-6 uppercase tracking-widest text-sm border-b border-zinc-800 pb-2 flex justify-between items-center">
        Mis Entradas
        {bookings?.length > 0 && (
          <span className="text-zinc-600 text-[10px]">{bookings.length} total</span>
        )}
      </h3>
      
      {bookings && bookings.length > 0 ? (
        /* AQUÍ EL DETALLE VISUAL: 
           - max-h-[500px]: Altura máxima para que no crezca infinito
           - overflow-y-auto: Muestra scroll si hay muchas entradas
           - pr-2: Espacio para que el scroll no tape el contenido
        */
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
          {bookings.map((booking) => (
            <div 
              key={booking.id} 
              className="relative bg-zinc-950 border-l-4 border-l-[#ffe81f] border border-zinc-800 rounded-r-2xl p-6 flex justify-between items-center group hover:bg-zinc-900 transition-all overflow-hidden"
            >
              {/* Círculos decorativos para efecto de ticket troquelado */}
              <div className="absolute -left-3 top-1/2 -translate-y-1/2 w-6 h-6 bg-zinc-900 rounded-full border border-zinc-800"></div>
              
              <div className="pl-4">
                <span className="text-[10px] font-black text-zinc-500 uppercase tracking-widest block mb-1">
                  CINEVERSE TICKET
                </span>
                <h4 className="text-white font-bold text-lg leading-tight mb-2">
                  {booking.screening?.movie?.title || "Película no disponible"}
                </h4>
                <div className="flex gap-4 items-center">
                  <div className="bg-zinc-800 px-3 py-1 rounded-md">
                    <span className="text-white font-mono text-sm">Butaca {booking.seat}</span>
                  </div>
                  <span className="text-zinc-500 text-[10px] uppercase font-bold tracking-tighter">
                    {booking.screening?.startTime 
                      ? new Date(booking.screening.startTime).toLocaleDateString() 
                      : "Fecha pendiente"}
                  </span>
                </div>
              </div>
              
              <div className="text-right flex flex-col items-end gap-3 border-l border-dashed border-zinc-800 pl-6">
                {booking.validated ? (
                  <div className="bg-green-500/10 text-green-500 text-[8px] font-black px-2 py-1 rounded border border-green-500/20 uppercase">
                    Utilizada
                  </div>
                ) : (
                  <div className="bg-[#ffe81f]/10 text-[#ffe81f] text-[8px] font-black px-2 py-1 rounded border border-[#ffe81f]/20 uppercase">
                    Válida
                  </div>
                )}
                <div>
                  <span className="text-[9px] text-zinc-700 block font-mono">ID: #{booking.id}</span>
                  {/* Pequeño detalle de código de barras simulado */}
                  <div className="flex gap-0.5 mt-1 justify-end opacity-30 group-hover:opacity-60 transition-opacity">
                    <div className="w-0.5 h-4 bg-white"></div>
                    <div className="w-1 h-4 bg-white"></div>
                    <div className="w-0.5 h-4 bg-white"></div>
                    <div className="w-2 h-4 bg-white"></div>
                    <div className="w-0.5 h-4 bg-white"></div>
                  </div>
                </div>
                <button onClick={() => generateTicketPDF(booking)}
                  className="text-[#ffe81f] hover:text-white transition-colors p-2"
                  title="Descargar PDF"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                </button>
                
                <TicketPDF booking={booking} /> 
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-zinc-950/50 border border-dashed border-zinc-800 rounded-[32px] p-16 text-center">
          <div className="w-12 h-12 bg-zinc-900 rounded-full flex items-center justify-center mx-auto mb-4 border border-zinc-800 text-zinc-700 font-bold">!</div>
          <p className="text-zinc-600 text-[10px] font-black uppercase tracking-[0.3em]">
            Tu historial de compras está vacío
          </p>
        </div>
      )}
    </div>
  );
};

export default MyBookings;