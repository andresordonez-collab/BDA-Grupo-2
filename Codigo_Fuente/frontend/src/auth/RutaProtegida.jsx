import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

// Tarea 2: Protege las rutas por rol: sin sesión vuelve al login y con otro rol, a /unauthorized.
export default function RutaProtegida({ rol }) {
  const { usuario } = useAuth();

  if (!usuario) {
    return <Navigate to="/" replace />;
  }

  const rolActual =
    typeof usuario.rol === 'string' ? usuario.rol.toLowerCase() : '';

  if (rolActual !== rol) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
