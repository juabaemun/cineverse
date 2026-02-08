import React from 'react';
import TicketPDF, { generateTicketPDF } from './TicketPDF';

const TicketList = ({ bookings, onValidate }) => {
  return (
    <div className="bg-black/20 rounded-3xl border border-zinc-800 overflow-hidden shadow-xl">
      <table className="w-full text-left border-collapse">
        <thead className="text-[10px] text-zinc-500 uppercase border-b border-zinc-800 bg-zinc-900/50">
          <tr>
            <th className="p-5 font-black">Butaca</th>
            <th className="p-5 font-black">Cliente</th>
            <th className="p-5 font-black">Estado</th>
            <th className="p-5 font-black text-center">Ticket</th>
            <th className="p-5 font-black text-right">Acción</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-zinc-800/50">
          {bookings.length > 0 ? bookings.map(b => (
            <tr key={b.id} className="hover:bg-white/5 transition-colors">
              <td className="p-5 font-black text-[#ffe81f]">{b.seat}</td>
              <td className="p-5 text-sm text-zinc-300">{b.user?.email || "Taquilla"}</td>
              <td className="p-5">
                {b.validated ? (
                  <span className="text-green-500 text-[9px] font-black uppercase bg-green-500/10 px-2 py-1 rounded border border-green-500/20">✓ Validada</span>
                ) : (
                  <span className="text-zinc-500 text-[9px] font-black uppercase bg-zinc-800 px-2 py-1 rounded">Pendiente</span>
                )}
              </td>
              <td className="p-5 text-center">
                <button 
                  onClick={() => generateTicketPDF(b)}
                  className="text-zinc-500 hover:text-[#ffe81f] p-2 bg-zinc-800/50 rounded-lg transition-all"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                </button>
                <TicketPDF booking={b} />
              </td>
              <td className="p-5 text-right">
                {!b.validated && (
                  <button 
                    onClick={() => onValidate(b.id)}
                    className="bg-white text-black text-[10px] font-black px-4 py-2 rounded-lg hover:bg-[#ffe81f] transition-all"
                  >
                    VALIDAR
                  </button>
                )}
              </td>
            </tr>
          )) : (
            <tr>
              <td colSpan="5" className="p-20 text-center text-zinc-600 text-[10px] font-black uppercase tracking-[0.3em]">
                No hay entradas vendidas
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default TicketList;