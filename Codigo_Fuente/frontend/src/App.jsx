import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
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
import TarifasPage from './pages/operador/TarifasPage';
import UsuariosPage from './pages/operador/UsuariosPage';
import BilleterasPage from './pages/operador/BilleterasPage';
import MovimientosBilleteraPage from './pages/operador/MovimientosBilleteraPage';

import HistorialPage from './pages/usuario/HistorialPage';
import ResumenMensualPage from './pages/usuario/ResumenMensualPage';
import MiViajePage from './pages/usuario/MiViajePage';
import TarifasUsuarioPage from './pages/usuario/TarifasPage';
import BilleteraUsuarioPage from './pages/usuario/BilleteraPage';

import { AuthProvider, useAuth } from './auth/AuthContext';
import RutaProtegida from './auth/RutaProtegida';

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

function ContenidoApp() {
  const {
    usuario,
    cargando,
    error,
    iniciarSesion,
    cerrarSesion,
  } = useAuth();

  if (cargando) {
    return <p role="status">Comprobando sesión…</p>;
  }

  if (error) {
    return (
      <div role="alert">
        <p>{error}</p>
        <button type="button" onClick={() => window.location.reload()}>
          Reintentar
        </button>
      </div>
    );
  }

  const inicio =
    usuario?.rol?.toLowerCase() === 'operador'
      ? '/operador/flota'
      : '/usuario';

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            usuario
              ? <Navigate to={inicio} replace />
              : <Login onLogin={iniciarSesion} />
          }
        />

        <Route path="/unauthorized" element={<Unauthorized />} />

        <Route element={<RutaProtegida rol="usuario" />}>
          <Route
            path="/usuario"
            element={
              <Layout
                role="Usuario"
                usuario={usuario}
                onLogout={cerrarSesion}
              />
            }
          >
            <Route index element={<PanelUsuario />} />
            <Route path="historial" element={<HistorialPage />} />
            <Route path="resumen-mensual" element={<ResumenMensualPage />} />
            <Route path="mi-viaje" element={<MiViajePage />} />
            <Route path="billetera" element={<BilleteraUsuarioPage />} />
            <Route path="tarifas" element={<TarifasUsuarioPage />} />
          </Route>
        </Route>

        <Route element={<RutaProtegida rol="operador" />}>
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
            <Route path="tarifas" element={<TarifasPage />} />
            <Route path="usuarios" element={<UsuariosPage />} />
            <Route path="billeteras" element={<BilleterasPage />} />
            <Route
              path="movimientos-billeteras"
              element={<MovimientosBilleteraPage />}
            />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <ContenidoApp />
      </AuthProvider>
    </ThemeProvider>
  );
}
