import React from 'react';

const MovieFormModal = ({ isEditing, currentMovie, setCurrentMovie, onSave, onClose }) => {
  return (
    <div className="fixed inset-0 bg-black/90 backdrop-blur-sm flex items-center justify-center p-4 z-50">
      <div className="bg-zinc-900 border border-zinc-800 p-8 rounded-3xl max-w-md w-full shadow-2xl">
        <h3 className="text-2xl font-black mb-6 text-[#ffe81f] tracking-tight">
          {isEditing ? 'EDITAR PELÍCULA' : 'NUEVA PELÍCULA'}
        </h3>
        <form onSubmit={onSave} className="space-y-4">
          <input 
            type="text" placeholder="Título" 
            className="w-full bg-zinc-800 border border-zinc-700 p-3 rounded-xl outline-none focus:border-[#ffe81f] text-white" 
            value={currentMovie.title} 
            onChange={(e) => setCurrentMovie({...currentMovie, title: e.target.value})} required 
          />
          <div className="grid grid-cols-2 gap-4">
            <input 
              type="number" placeholder="Duración (min)" 
              className="w-full bg-zinc-800 border border-zinc-700 p-3 rounded-xl outline-none text-white" 
              value={currentMovie.duration} 
              onChange={(e) => setCurrentMovie({...currentMovie, duration: e.target.value})} required 
            />
            <input 
              type="text" placeholder="URL Imagen" 
              className="w-full bg-zinc-800 border border-zinc-700 p-3 rounded-xl outline-none text-white" 
              value={currentMovie.imageUrl} 
              onChange={(e) => setCurrentMovie({...currentMovie, imageUrl: e.target.value})} 
            />
          </div>
          <textarea 
            placeholder="Sinopsis" 
            className="w-full bg-zinc-800 border border-zinc-700 p-3 rounded-xl h-24 outline-none focus:border-[#ffe81f] resize-none text-white" 
            value={currentMovie.synopsis} 
            onChange={(e) => setCurrentMovie({...currentMovie, synopsis: e.target.value})}
          ></textarea>
          <div className="flex gap-4 pt-4">
            <button type="button" onClick={onClose} className="flex-1 bg-zinc-800 py-3 rounded-xl font-bold text-zinc-400">CANCELAR</button>
            <button type="submit" className="flex-1 bg-[#ffe81f] text-black py-3 rounded-xl font-bold">GUARDAR</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default MovieFormModal;