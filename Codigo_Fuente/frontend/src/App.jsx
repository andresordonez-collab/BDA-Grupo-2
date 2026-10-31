import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

import Layout from './components/layout';
import Login from './pages/Login';
import PanelUsuario from './pages/PanelUsuario';
import Unauthorized from './pages/Unauthorized';
import PlaceholderPage from './pages/PlaceholderPage';
import FlotaPage from './pages/operador/FlotaPage';

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
          <Route path="/operador" element={<Layout role="Operador" />}>
            <Route index element={<Navigate to="/operador/flota" replace />} />

            <Route path="flota" element={<FlotaPage />} />

            <Route
              path="estaciones"
              element={
                <PlaceholderPage
                  title="Estaciones"
                  description="Aquí se mostrará la capacidad, ocupación y disponibilidad de cada estación."
                />
              }
            />

            <Route
              path="rebalanceo"
              element={
                <PlaceholderPage
                  title="Rebalanceo"
                  description="Aquí se ejecutará el traslado de vehículos entre estaciones."
                />
              }
            />

            <Route
              path="auditoria"
              element={
                <PlaceholderPage
                  title="Auditoría"
                  description="Aquí se revisarán los cambios de estado de los vehículos."
                />
              }
            />

            <Route
              path="demanda"
              element={
                <PlaceholderPage
                  title="Demanda"
                  description="Aquí se mostrará la demanda por estación y franja horaria."
                />
              }
            />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
