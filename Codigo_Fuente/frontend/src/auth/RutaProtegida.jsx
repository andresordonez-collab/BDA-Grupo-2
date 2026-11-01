import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * Bloquea una rama de rutas según el rol del usuario autenticado.
 * - Sin sesión: vuelve al login.
 * - Sesión con rol incorrecto: vuelve a /unauthorized.
 */
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
