import React from 'react';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { QRCodeCanvas } from 'qrcode.react';

export const generateTicketPDF = async (booking) => {
  const element = document.getElementById(`ticket-pdf-${booking.id}`);
  const canvasQR = document.querySelector(`#qr-real-${booking.id} canvas`);
  
  if (!element || !canvasQR) {
    alert("Error: El sistema aún está procesando los datos. Reintenta en 1 segundo.");
    return;
  }

  // 1. Mostrar temporalmente
  element.style.display = "block";
  
  try {
    // 2. Capturar el diseño del ticket (sin el QR)
    const ticketCanvas = await html2canvas(element, { 
      scale: 2, 
      backgroundColor: "#000000",
      useCORS: true
    });
    
    const ticketImg = ticketCanvas.toDataURL('image/png');
    const qrImg = canvasQR.toDataURL('image/png'); // Extraer el QR directamente del canvas

    // 3. Crear PDF
    const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: [80, 150] });
    
    // Añadir el cuerpo del ticket
    pdf.addImage(ticketImg, 'PNG', 0, 0, 80, 150);
    
    // 4. SOBRESCRIBIR EL QR (Forzado manual en el PDF)
    // Esto pone el QR encima del hueco blanco, garantizando que aparezca
    pdf.setFillColor(255, 255, 255);
    pdf.roundedRect(20, 95, 40, 40, 3, 3, 'F'); // Fondo blanco manual
    pdf.addImage(qrImg, 'PNG', 22, 97, 36, 36); // El QR centrado

    pdf.save(`Ticket-CineVerse-${booking.id}.pdf`);
  } catch (error) {
    console.error("Error crítico:", error);
  } finally {
    element.style.display = "none";
  }
};

const TicketPDF = ({ booking }) => {
  if (!booking) return null;

  return (
    <>
      {/* QR REAL (Invisible para el usuario, pero accesible para el script) */}
      <div id={`qr-real-${booking.id}`} style={{ position: 'fixed', left: '-5000px' }}>
        <QRCodeCanvas value={`VALIDATE-${booking.id}`} size={200} level="H" />
      </div>

      {/* DISEÑO DEL TICKET */}
      <div 
        id={`ticket-pdf-${booking.id}`} 
        style={{ 
          display: 'none', 
          width: '300px', 
          padding: '30px', 
          background: '#000', 
          color: '#fff', 
          fontFamily: 'monospace',
          textAlign: 'center',
          position: 'fixed',
          left: '-2000px',
          top: '0'
        }}
      >
        <div style={{ border: '2px solid #ffe81f', padding: '20px' }}>
          <h1 style={{ color: '#ffe81f', margin: '0', fontSize: '24px' }}>CINEVERSE</h1>
          <p style={{ fontSize: '10px', marginBottom: '20px' }}>TICKET DE ENTRADA</p>
          
          <div style={{ borderTop: '1px solid #333', borderBottom: '1px solid #333', padding: '15px 0' }}>
            <h2 style={{ fontSize: '18px', marginBottom: '10px' }}>{booking.screening?.movie?.title}</h2>
            <p>FECHA: {new Date(booking.screening?.startTime).toLocaleDateString()}</p>
            <p style={{ fontSize: '24px', fontWeight: 'bold', color: '#ffe81f' }}>BUTACA: {booking.seat}</p>
          </div>

          {/* Dejamos un espacio vacío donde el script inyectará el QR manualmente */}
          <div style={{ height: '140px' }}></div>
          
          <p style={{ fontSize: '9px', color: '#555' }}>ID RESERVA: #{booking.id}</p>
        </div>
      </div>
    </>
  );
};

export default TicketPDF;