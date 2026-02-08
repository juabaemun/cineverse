import React from 'react';

const MovieGrid = ({ movies, onEdit, onDelete }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
      {movies.map(movie => (
        <div key={movie.id} className="bg-zinc-900 border border-zinc-800 rounded-2xl overflow-hidden group hover:border-[#ffe81f]/50 transition-colors">
          <div className="aspect-[2/3] bg-zinc-800 relative overflow-hidden">
            <img 
              src={movie.imageUrl || `https://placehold.co/400x600/18181b/ffe81f?text=${encodeURIComponent(movie.title)}`} 
              alt={movie.title} 
              className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" 
            />
          </div>
          <div className="p-5">
            <h4 className="text-lg font-bold text-white leading-tight mb-2 truncate">{movie.title}</h4>
            <div className="flex gap-2 mt-4">
              <button onClick={() => onEdit(movie)} className="flex-1 bg-blue-900/20 hover:bg-blue-900/40 text-blue-400 py-2 rounded-lg text-[10px] font-black border border-blue-900/30 transition">EDITAR</button>
              <button onClick={() => onDelete(movie.id)} className="flex-1 bg-red-900/20 hover:bg-red-900/40 text-red-500 py-2 rounded-lg text-[10px] font-black border border-red-900/30 transition">BORRAR</button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default MovieGrid;