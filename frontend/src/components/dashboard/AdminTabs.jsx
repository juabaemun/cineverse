import React from 'react';

//Pestañas para la página principal

const AdminTabs = ({ activeTab, setActiveTab }) => {
  const tabs = [
    { id: 'movies', label: 'PELÍCULAS' },
    { id: 'users', label: 'USUARIOS / STAFF' },
    { id: 'rooms', label: 'SALAS' },
    { id: 'screenings', label: 'SESIONES' }
  ];

  return (
    <div className="flex gap-2 mb-8 bg-zinc-900 p-1 rounded-xl w-fit border border-zinc-800">
      {tabs.map(tab => (
        <button
          key={tab.id}
          onClick={() => setActiveTab(tab.id)}
          className={`px-6 py-2 rounded-lg font-bold text-xs transition-all ${
            activeTab === tab.id ? 'bg-[#ffe81f] text-black' : 'hover:bg-zinc-800 text-zinc-400'
          }`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
};

export default AdminTabs;