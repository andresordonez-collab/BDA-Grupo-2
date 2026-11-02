import axios from 'axios';

// Tarea 2: Cliente HTTP con cookies habilitadas para que el JWT viaje en cada solicitud.
const http = axios.create({
  baseURL: '',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});



export function getErrorMessage(error) {
  const status = error?.response?.status;
  const data = error?.response?.data;

  if (status === 403 || data?.codigo === 'ACCESO_DENEGADO') {
    return 'No tienes permiso para esta acción';
  }

  if (status === 401) {
    return 'Tu sesión venció. Inicia sesión nuevamente.';
  }

  if (!error?.response) {
    return 'No pudimos conectar con el servidor. Intenta nuevamente.';
  }

  if (status === 404) {
    return 'La información solicitada no está disponible por el momento.';
  }

  if (status >= 500) {
    return 'No pudimos completar la solicitud. Intenta nuevamente más tarde.';
  }

  return (
    data?.mensaje ??
    data?.message ??
    data?.error ??
    'No pudimos completar la solicitud.'
  );
}

export function registrarSesionVencida(onSesionVencida) {
  const interceptor = http.interceptors.response.use(
    (respuesta) => respuesta,
    (error) => {
      const esLogin = error.config?.url === '/api/auth/login';

      if (error.response?.status === 401 && !esLogin) {
        onSesionVencida();
      }

      return Promise.reject(error);
    },
  );

  return () => {
    http.interceptors.response.eject(interceptor);
  };
}

export default http;