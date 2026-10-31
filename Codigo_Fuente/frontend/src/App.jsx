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

// Usuario simulado solo para el modo mock. En modo real, el AuthContext
// debe entregar este mismo objeto desde /api/auth/me.
const usuarioMock =
  import.meta.env.VITE_USE_MOCKS === 'true'
    ? {
        idUsuario: 1,
        nombreUsuario: 'Camila Torres',
        email: 'camila.operadora@example.com',
        rol: 'Operador',
      }
    : undefined;

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />

      <BrowserRouter>
        <Routes>
          {/* Público */}
          <Route path="/" element={<Login />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* Usuario: se mantiene la pantalla actual mientras se migra */}
          <Route path="/usuario" element={<PanelUsuario />} />

          {/* Operador */}
          <Route
            path="/operador"
            element={
              <Layout role="Operador" usuario={usuarioMock} />
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
