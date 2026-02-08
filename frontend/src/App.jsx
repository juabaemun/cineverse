import React, { useState, useEffect } from 'react';
import Navbar from "./components/Navbar";
import Dashboard from "./pages/Dashboard";
import Login from "./pages/Login";

function App() {
  const [user, setUser] = useState(null);

  // 1. Efecto para recuperar la sesión al cargar o refrescar (F5)
  useEffect(() => {
    const savedRole = localStorage.getItem("role");
    const savedEmail = localStorage.getItem("userEmail");
    const savedToken = localStorage.getItem("token");

    // Si tenemos token y rol, reconstruimos el estado del usuario
    if (savedToken && savedRole) {
      setUser({ 
        role: savedRole, 
        email: savedEmail || "Usuario" 
      });
    }
  }, []);

  // 2. Manejador de éxito en el login
  const handleLoginSuccess = (data) => {
    // Seteamos el estado con lo que viene del Backend (Java)
    setUser({
      role: data.role,
      email: data.displayName // 'displayName' es como lo llamamos en el AuthController
    });

    // Guardamos en LocalStorage para persistencia
    localStorage.setItem("role", data.role);
    localStorage.setItem("userEmail", data.displayName);
    localStorage.setItem("token", data.token);
  };

  // 3. Manejador de cierre de sesión
  const handleLogout = () => {
    localStorage.clear(); // Limpia token, rol y email de un golpe
    setUser(null);
  };

  // Si no hay usuario en el estado, mostramos la pantalla de Login
  if (!user) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="min-h-screen bg-black">
      {/* Pasamos el email y la función de logout al Navbar */}
      <Navbar userEmail={user.email} onLogout={handleLogout} />
      
      {/* El Dashboard usará el token guardado en localStorage para sus peticiones */}
      <Dashboard />
    </div>
  );
}

export default App;