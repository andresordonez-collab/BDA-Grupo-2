import http from '../http-common';

const getAll = () => http.get('/api/estaciones');

const getDisponibilidad = () =>
  http.get('/api/estaciones/disponibilidad');

export default { getAll, getDisponibilidad };