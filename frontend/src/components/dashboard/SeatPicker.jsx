import React from 'react';

const SeatPicker = ({ room, occupiedSeats, selectedSeats, onToggleSeat }) => {
  return (
    <div className="flex flex-col items-center">
      <div className="mb-12 w-full">
        <div className="w-full h-2 bg-zinc-700 rounded-full mb-2 shadow-[0_10px_20px_rgba(255,232,31,0.1)]"></div>
        <p className="text-center text-[10px] font-bold text-zinc-600 tracking-[0.3em]">PANTALLA</p>
      </div>
      
      <div className="flex flex-col gap-2">
        {Array.from({ length: room.rowsCount }).map((_, rowIndex) => (
          <div key={rowIndex} className="flex gap-2">
            {Array.from({ length: room.seatsPerRow }).map((_, colIndex) => {
              const seatId = `${rowIndex + 1}-${colIndex + 1}`;
              const isOccupied = occupiedSeats.some(s => s.trim() === seatId);
              const isSelected = selectedSeats.includes(seatId);

              return (
                <button
                  key={seatId}
                  disabled={isOccupied}
                  onClick={() => onToggleSeat(seatId)}
                  className={`w-7 h-7 rounded-t-lg text-[8px] font-bold transition-all
                    ${isOccupied 
                      ? 'bg-red-900/40 text-red-500 border border-red-900/50 cursor-not-allowed' 
                      : isSelected 
                        ? 'bg-[#ffe81f] text-black scale-110 shadow-lg shadow-[#ffe81f]/20' 
                        : 'bg-zinc-700 text-transparent hover:bg-zinc-500 hover:text-white'}`}
                >
                  {colIndex + 1}
                </button>
              );
            })}
          </div>
        ))}
      </div>
      
      {/* Leyenda rápida */}
      <div className="flex gap-6 mt-12">
        <div className="flex items-center gap-2 text-[10px] font-black text-zinc-500 uppercase">
          <div className="w-3 h-3 bg-zinc-700 rounded-sm"></div> Libre
        </div>
        <div className="flex items-center gap-2 text-[10px] font-black text-zinc-500 uppercase">
          <div className="w-3 h-3 bg-[#ffe81f] rounded-sm"></div> Seleccionado
        </div>
        <div className="flex items-center gap-2 text-[10px] font-black text-zinc-500 uppercase">
          <div className="w-3 h-3 bg-red-900/40 border border-red-900/50 rounded-sm"></div> Ocupado
        </div>
      </div>
    </div>
  );
};

export default SeatPicker;