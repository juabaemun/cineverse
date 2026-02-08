import React, { useState } from 'react';

const Login = ({ onLoginSuccess }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);

    try {
      const response = await fetch("/api/auth/login", { 
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password })
      });

      if (response.ok) {
        const data = await response.json();
        
        // 1. Limpieza de sesión previa
        localStorage.clear(); 

        // 2. Guardado de datos del nuevo login
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);
        localStorage.setItem("userEmail", data.displayName);

        console.log("Sesión iniciada con éxito para:", data.role);

        // 3. Notificamos al componente padre
        onLoginSuccess(data);
      } else {
        // Capturamos el error JSON enviado por el Backend (Java)
        const errorData = await response.json();
        setErrorMsg(errorData.error || "Credenciales inválidas");
      }
    } catch (err) {
      setErrorMsg("No se pudo conectar con el servidor. Verifica que el backend esté corriendo.");
      console.error("Error de red:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-zinc-950 px-4">
      <div className="max-w-md w-full bg-zinc-900 p-8 rounded-xl border border-zinc-800 shadow-2xl">
        <h1 className="text-4xl font-extrabold text-[#ffe81f] text-center mb-8 tracking-tighter uppercase italic">
          CINEVERSE
        </h1>

        {/* Sección de alerta de error */}
        {errorMsg && (
          <div className="bg-red-500/10 border border-red-500 text-red-500 p-3 rounded-lg mb-6 text-center text-sm font-semibold animate-pulse">
            {errorMsg}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <input 
              type="email" 
              placeholder="Email del empleado"
              className="w-full bg-zinc-800 border border-zinc-700 rounded-lg py-3 px-4 text-white outline-none focus:ring-2 focus:ring-[#ffe81f] transition-all"
              onChange={(e) => setEmail(e.target.value)}
              value={email}
              required 
            />
          </div>

          <div>
            <input 
              type="password" 
              placeholder="Contraseña"
              className="w-full bg-zinc-800 border border-zinc-700 rounded-lg py-3 px-4 text-white outline-none focus:ring-2 focus:ring-[#ffe81f] transition-all"
              onChange={(e) => setPassword(e.target.value)}
              value={password}
              required 
            />
          </div>

          <button 
            type="submit" 
            disabled={loading}
            className={`w-full bg-[#ffe81f] hover:bg-yellow-500 text-black font-bold py-3 rounded-lg transition-all active:scale-95 flex justify-center items-center ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {loading ? (
              <span className="flex items-center">
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-black" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Verificando...
              </span>
            ) : (
              'Entrar al Panel'
            )}
          </button>
        </form>


      </div>
    </div>
  );
};

export default Login;