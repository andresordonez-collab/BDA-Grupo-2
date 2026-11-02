import http from '../http-common';

export const GetEstacionesDisponibles = async () => {
  const { data } = await http.get('/api/estaciones/disponibilidad');
  return data;
};

export default http;
