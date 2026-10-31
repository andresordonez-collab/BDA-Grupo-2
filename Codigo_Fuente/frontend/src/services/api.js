import http from '../http-common';

/**
 * Compatibilidad temporal con las pantallas existentes.
 *
 * La capa nueva del panel de operador usará servicios por entidad en
 * src/services/*Service.js. Este archivo se mantiene para no romper
 * PanelUsuario mientras se migra el frontend.
 */
export const GetEstacionesDisponibles = async () => {
  const { data } = await http.get('/api/estaciones/disponibilidad');
  return data;
};

export default http;
