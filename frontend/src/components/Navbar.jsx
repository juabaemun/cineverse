import React from 'react';

const Navbar = ({ userEmail, onLogout }) => {
  return (
    <nav className="bg-zinc-900 border-b border-zinc-800 px-6 py-4 flex justify-between items-center">
      <h1 className="text-2xl font-black text-[#ffe81f]">CINEVERSE <span className="text-xs font-normal text-zinc-500 uppercase tracking-widest ml-2">Staff</span></h1>
      <div className="flex items-center gap-4">
        <span className="text-zinc-400 text-sm">Conectado como: <b className="text-white">{userEmail}</b></span>
        <button 
          onClick={onLogout}
          className="bg-zinc-800 hover:bg-zinc-700 text-xs font-bold py-2 px-4 rounded transition"
        >
          Cerrar Sesión
        </button>
      </div>
    </nav>
  );
};

export default Navbar;