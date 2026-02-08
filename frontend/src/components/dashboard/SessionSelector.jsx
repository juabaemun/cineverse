import React from 'react';

const SessionSelector = ({ screenings, onSelect }) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {screenings.map(s => {
        const pctOcupacion = 0; // Se puede calcular si el backend envía el total
        return (
          <div 
            key={s.id} 
            className="bg-zinc-800/40 border border-zinc-700 p-5 rounded-2xl hover:border-[#ffe81f] transition-all cursor-pointer group" 
            onClick={() => onSelect(s)}
          >
            <div className="flex justify-between items-start mb-4">
              <h4 className="font-bold text-white leading-tight group-hover:text-[#ffe81f] transition-colors">{s.movie?.title}</h4>
              <span className="text-[10px] bg-zinc-700 px-2 py-1 rounded text-[#ffe81f] font-bold">{s.room?.name}</span>
            </div>
            <div className="space-y-2">
              <div className="flex justify-between text-[10px] font-bold text-zinc-500 uppercase">
                <span>Aforo</span>
                <span>{pctOcupacion}% Ocupado</span>
              </div>
              <div className="w-full bg-zinc-900 h-1.5 rounded-full overflow-hidden">
                <div className="bg-[#ffe81f] h-full" style={{ width: `${pctOcupacion}%` }}></div>
              </div>
            </div>
            <div className="mt-4 text-[10px] text-zinc-400 font-bold uppercase">
              {new Date(s.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} • {s.price}€
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default SessionSelector;