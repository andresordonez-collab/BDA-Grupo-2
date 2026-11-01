import {
  BrowserRouter,
  Navigate,
  Outlet,
  Route,
  Routes,
} from 'react-router-dom';
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
import { getErrorMessage, registrarSesionVencida } from './http-common';
import HistorialPage from './pages/usuario/HistorialPage';
import ResumenMensualPage from './pages/usuario/ResumenMensualPage';
import MiViajePage from './pages/usuario/MiViajePage';



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

function RutaProtegida({ usuario, rol }) {
  if (!usuario) {
    return <Navigate to="/" replace />;
  }

  if (usuario.rol.toLowerCase() !== rol) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}

function App() {
  const [usuario, setUsuario] = useState(null);
  const [verificandoSesion, setVerificandoSesion] = useState(true);
  const [errorSesion, setErrorSesion] = useState('');

  async function cerrarSesion() {
    try {
      await authService.logout();
    } catch (err) {
      // Si la sesión ya venció, igualmente podemos salir del panel.
      if (err.response?.status !== 401) {
        throw err;
      }
    }

    setUsuario(null);
  }

  useEffect(() => {
    let activo = true;

    const retirarInterceptor = registrarSesionVencida(() => {
      if (activo) {
        setUsuario(null);
        setErrorSesion('');
      }
    });

    async function recuperarSesion() {
      try {
        const perfil = await authService.me();

        if (!activo) return;

        const rol = typeof perfil?.rol === 'string'
          ? perfil.rol.toLowerCase()
          : '';

        if (!perfil?.idUsuario || !['usuario', 'operador'].includes(rol)) {
          setErrorSesion('No pudimos identificar tu perfil de usuario.');
          return;
        }

        setUsuario(perfil);
      } catch (err) {
        if (!activo) return;

        if (err.response?.status === 401) {
          setUsuario(null);
        } else {
          setErrorSesion(getErrorMessage(err));
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
      retirarInterceptor();
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

  const inicio = usuario?.rol.toLowerCase() === 'operador'
    ? '/operador/flota'
    : '/usuario';

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

      <BrowserRouter>
        <Routes>
          <Route
            path="/"
            element={
              usuario
                ? <Navigate to={inicio} replace />
                : <Login onLogin={setUsuario} />
            }
          />

          <Route path="/unauthorized" element={<Unauthorized />} />

          <Route element={<RutaProtegida usuario={usuario} rol="usuario" />}>
            <Route
              path="/usuario"
              element={<Layout role="Usuario" usuario={usuario} onLogout={cerrarSesion} />}
            >
              <Route index element={<PanelUsuario />} />
              <Route path="historial" element={<HistorialPage />} />
              <Route path="resumen-mensual" element={<ResumenMensualPage />} />
              <Route path = "mi-viaje" element={<MiViajePage />} />
            </Route>
          </Route>
          <Route element={<RutaProtegida usuario={usuario} rol="operador" />}>
            <Route
              path="/operador"
              element={
                <Layout
                  role="Operador"
                  usuario={usuario}
                  onLogout={cerrarSesion}
                />
              }
            >
              <Route index element={<Navigate to="/operador/flota" replace />} />
              <Route path="flota" element={<FlotaPage />} />
              <Route path="estaciones" element={<EstacionesPage />} />
              <Route path="rebalanceo" element={<RebalanceoPage />} />
              <Route path="auditoria" element={<AuditoriaPage />} />
              <Route path="demanda" element={<DemandaPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;