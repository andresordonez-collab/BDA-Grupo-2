import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { useEffect, useState } from 'react'; 
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

import Layout from './components/layout';
import Login from './pages/Login';
import PanelUsuario from './pages/PanelUsuario';
import Unauthorized from './pages/Unauthorized';
import FlotaPage from './pages/operador/FlotaPage';
import EstacionesPage from './pages/operador/EstacionesPage';
import RebalanceoPage from './pages/operador/RebalanceoPage';
import AuditoriaPage from './pages/operador/AuditoriaPage';
import DemandaPage from './pages/operador/DemandaPage';
import authService from './services/authService';

const theme = createTheme({
  palette: {
    primary: { main: '#0f766e' },
    secondary: { main: '#f59e0b' },
    background: {
      default: '#f5f7fa',
      paper: '#ffffff',
    },
  },
  shape: {
    borderRadius: 10,
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});

function App() {
  const [usuario, setUsuario] = useState(null);
  const [verificandoSesion, setVerificandoSesion] = useState(true);
  const [errorSesion, setErrorSesion] = useState('');
  const cerrarSesion = async () => {
    await authService.logout();
    setUsuario(null);
  };

  useEffect(() => {
  let activo = true;

  async function recuperarSesion() {
    try {
      const perfil = await authService.me();

      if (!perfil?.idUsuario || typeof perfil.rol !== 'string') {
        throw new Error('Respuesta de sesión inválida');
      }

      if (activo) {
        setUsuario(perfil);
      }
    } catch (err) {
      if (!activo) return;

      if (err.response?.status === 401) {
        setUsuario(null);
      } else {
        setErrorSesion('No pudimos comprobar tu sesión. Intenta nuevamente.');
      }
    } finally {
      if (activo) {
        setVerificandoSesion(false);
      }
    }
  }

  recuperarSesion();

  return () => {
    activo = false;
  };
}, []);

if (verificandoSesion) {
  return <p role="status">Comprobando sesión…</p>;
}

if (errorSesion) {
  return (
    <div role="alert">
      <p>{errorSesion}</p>
      <button type="button" onClick={() => window.location.reload()}>
        Reintentar
      </button>
    </div>
  );
}
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

      <BrowserRouter>
        <Routes>
          {/* Público */}
          <Route path="/" element={<Login onLogin={setUsuario} />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* Usuario: se mantiene la pantalla actual mientras se migra */}
          <Route path="/usuario" element={<PanelUsuario onLogout = {cerrarSesion} />} />

          {/* Operador */}
          <Route
            path="/operador"
            element={
              <Layout role="Operador" usuario={usuario} onLogout={cerrarSesion} />
            }
          >
            <Route index element={<Navigate to="/operador/flota" replace />} />

            <Route path="flota" element={<FlotaPage />} />
            <Route path="estaciones" element={<EstacionesPage />} />
            <Route path="rebalanceo" element={<RebalanceoPage />} />
            <Route path="auditoria" element={<AuditoriaPage />} />
            <Route path="demanda" element={<DemandaPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
