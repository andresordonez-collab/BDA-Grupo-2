import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from 'react';
import authService from '../services/authService';
import {
  getErrorMessage,
  registrarSesionVencida,
} from '../http-common';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const iniciarSesion = useCallback((perfil) => {
    setUsuario(perfil);
    setError('');
  }, []);

  const cerrarSesion = useCallback(async () => {
    try {
      await authService.logout();
    } catch (err) {
      // Si la cookie ya venció, igualmente limpiamos la sesión local.
      if (err.response?.status !== 401) {
        throw err;
      }
    }

    setUsuario(null);
    setError('');
  }, []);

  useEffect(() => {
    let activo = true;

    const retirarInterceptor = registrarSesionVencida(() => {
      if (activo) {
        setUsuario(null);
        setError('');
      }
    });

    async function recuperarSesion() {
      try {
        const perfil = await authService.me();

        if (!activo) return;

        const rol =
          typeof perfil?.rol === 'string'
            ? perfil.rol.toLowerCase()
            : '';

        if (
          !perfil?.idUsuario ||
          !['usuario', 'operador'].includes(rol)
        ) {
          setError('No pudimos identificar tu perfil de usuario.');
          return;
        }

        setUsuario(perfil);
      } catch (err) {
        if (!activo) return;

        if (err.response?.status === 401) {
          setUsuario(null);
        } else {
          setError(getErrorMessage(err));
        }
      } finally {
        if (activo) {
          setCargando(false);
        }
      }
    }

    recuperarSesion();

    return () => {
      activo = false;
      retirarInterceptor();
    };
  }, []);

  return (
    <AuthContext.Provider
      value={{
        usuario,
        cargando,
        error,
        iniciarSesion,
        cerrarSesion,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const contexto = useContext(AuthContext);

  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de AuthProvider.');
  }

  return contexto;
}