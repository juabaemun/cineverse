import React, { useState, useEffect } from 'react';

const UsersAdmin = () => {
  const [users, setUsers] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formData, setFormData] = useState({ name: '', email: '', password: '', role: 'CLIENT' });

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = async () => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch("/api/users", {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        setUsers(await response.json());
      }
    } catch (err) {
      console.error("Error fetching users:", err);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    const dataToSend = { 
      nombreReal: formData.name, 
      email: formData.email, 
      password: formData.password, 
      role: formData.role 
    };
    
    const method = editingUser ? 'PUT' : 'POST';
    const url = editingUser ? `/api/users/${editingUser.id}` : "/api/users";
    
    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(dataToSend)
      });
      if (response.ok) { 
        setIsModalOpen(false); 
        fetchUsers(); 
      }
    } catch (err) {
      console.error("Error saving user:", err);
    }
  };

  const deleteUser = async (id) => {
    if (!confirm("¿Eliminar usuario?")) return;
    try {
      await fetch(`/api/users/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${localStorage.getItem("token")}` }
      });
      fetchUsers();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="animate-in fade-in duration-500">
      {/* CABECERA CON BOTÓN NUEVO */}
      <div className="flex justify-between items-end mb-8">
        <div>
          <h2 className="text-4xl font-black tracking-tighter text-[#ffe81f]">USUARIOS Y STAFF</h2>
          <p className="text-zinc-500 text-sm mt-1 uppercase tracking-widest font-bold">Gestión de accesos CineVerse</p>
        </div>
        <button 
          onClick={() => { 
            setEditingUser(null); 
            setFormData({name:'', email:'', password:'', role:'CLIENT'}); 
            setIsModalOpen(true); 
          }} 
          className="bg-white text-black px-6 py-3 rounded-xl font-black text-xs uppercase hover:bg-[#ffe81f] transition-all transform hover:scale-105"
        >
          + NUEVO USUARIO
        </button>
      </div>

      {/* TABLA DE USUARIOS */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-3xl overflow-hidden shadow-2xl">
        <table className="w-full text-left">
          <thead className="text-zinc-500 text-[10px] uppercase border-b border-zinc-800 bg-black/20">
            <tr>
              <th className="p-5">Nombre</th>
              <th className="p-5">Email</th>
              <th className="p-5">Rol</th>
              <th className="p-5 text-right">Acciones</th>
            </tr>
          </thead>
          <tbody className="text-sm">
            {users.map(u => (
              <tr key={u.id} className="border-b border-zinc-800/50 hover:bg-white/5 transition-colors">
                <td className="p-5 font-bold text-white">{u.nombreReal}</td>
                <td className="p-5 text-zinc-400">{u.email}</td>
                <td className="p-5">
                  <span className={`px-3 py-1 rounded text-[10px] font-black uppercase ${
                    u.role === 'ADMIN' ? 'bg-purple-500/20 text-purple-400' : 
                    u.role === 'EMPLOYEE' ? 'bg-blue-500/20 text-blue-400' : 'bg-zinc-800 text-zinc-400'
                  }`}>
                    {u.role}
                  </span>
                </td>
                <td className="p-5 text-right space-x-4">
                  <button 
                    onClick={() => { 
                      setEditingUser(u); 
                      setFormData({name: u.nombreReal, email: u.email, role: u.role}); 
                      setIsModalOpen(true); 
                    }} 
                    className="text-blue-400 font-bold text-xs hover:text-blue-300 uppercase tracking-widest"
                  >
                    Editar
                  </button>
                  <button 
                    onClick={() => deleteUser(u.id)} 
                    className="text-red-500 font-bold text-xs hover:text-red-400 uppercase tracking-widest"
                  >
                    Borrar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* MODAL */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/95 backdrop-blur-md flex items-center justify-center p-4 z-[100]">
          <div className="bg-zinc-900 border border-zinc-800 p-8 rounded-3xl max-w-sm w-full shadow-2xl animate-in zoom-in duration-300">
            <h4 className="text-2xl font-black mb-6 text-[#ffe81f] uppercase tracking-tighter">
              {editingUser ? 'EDITAR USUARIO' : 'NUEVO USUARIO'}
            </h4>
            <div className="space-y-4">
              <div>
                <label className="text-[10px] font-black text-zinc-500 uppercase ml-1">Nombre Completo</label>
                <input type="text" placeholder="Ej: Juan Pérez" className="w-full bg-zinc-800 p-4 rounded-xl border border-zinc-700 outline-none focus:border-[#ffe81f] text-white" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
              </div>
              <div>
                <label className="text-[10px] font-black text-zinc-500 uppercase ml-1">Correo Electrónico</label>
                <input type="email" placeholder="email@cineverse.com" className="w-full bg-zinc-800 p-4 rounded-xl border border-zinc-700 outline-none focus:border-[#ffe81f] text-white" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} />
              </div>
              {!editingUser && (
                <div>
                  <label className="text-[10px] font-black text-zinc-500 uppercase ml-1">Contraseña</label>
                  <input type="password" placeholder="••••••••" className="w-full bg-zinc-800 p-4 rounded-xl border border-zinc-700 outline-none focus:border-[#ffe81f] text-white" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} />
                </div>
              )}
              <div>
                <label className="text-[10px] font-black text-zinc-500 uppercase ml-1">Rol de Acceso</label>
                <select className="w-full bg-zinc-800 p-4 rounded-xl border border-zinc-700 outline-none focus:border-[#ffe81f] text-white appearance-none" value={formData.role} onChange={e => setFormData({...formData, role: e.target.value})}>
                  <option value="CLIENT">CLIENTE</option>
                  <option value="EMPLOYEE">EMPLEADO</option>
                  <option value="ADMIN">ADMINISTRADOR</option>
                </select>
              </div>
            </div>
            <div className="flex gap-3 mt-8">
              <button onClick={() => setIsModalOpen(false)} className="flex-1 py-4 bg-zinc-800 rounded-2xl font-black text-xs text-zinc-400 hover:bg-zinc-700 transition-colors uppercase">CANCELAR</button>
              <button onClick={handleSave} className="flex-1 py-4 bg-[#ffe81f] text-black rounded-2xl font-black text-xs hover:scale-105 transition-transform uppercase">GUARDAR</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UsersAdmin;