import axios from 'axios';

/**
 * Cliente HTTP central del frontend.
 *
 * baseURL vacío: los servicios llaman a rutas relativas como "/api/...".
 * En desarrollo, Vite reenvía esas rutas a Spring Boot mediante el proxy
 * configurado en vite.config.js. En producción debe existir un proxy
 * equivalente (Nginx o similar).
 *
 * withCredentials: true es necesario porque el JWT viaja en una cookie
 * HttpOnly llamada access_token.
 */
const http = axios.create({
  baseURL: '',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Extrae un mensaje legible desde una respuesta de error del backend.
 */
export function getErrorMessage(error) {
  return (
    error?.response?.data?.message ??
    error?.response?.data?.error ??
    error?.message ??
    'Ocurrió un error inesperado.'
  );
}

export default http;
