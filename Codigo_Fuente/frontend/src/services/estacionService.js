import http from '../http-common';

const getAll = (signal) => http.get('/api/estaciones', { signal });

const getDisponibilidad = () =>
  http.get('/api/estaciones/disponibilidad');

export default { getAll, getDisponibilidad };