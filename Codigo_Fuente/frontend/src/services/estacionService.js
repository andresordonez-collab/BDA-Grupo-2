import http from '../http-common';

const getAll = (signal) => http.get('/api/estaciones', { signal });

const get = (id) => http.get(`/api/estaciones/${id}`);

const create = (data) => http.post('/api/estaciones', data);

const update = (id, data) => http.put(`/api/estaciones/${id}`, data);

const remove = (id) => http.delete(`/api/estaciones/${id}`);

const getDisponibilidad = () =>
  http.get('/api/estaciones/disponibilidad');

// Vehículos disponibles y anclados en una estación (lectura para cualquier usuario autenticado).
const getVehiculosDisponibles = (idEstacion, signal) =>
  http.get(`/api/estaciones/${idEstacion}/vehiculos-disponibles`, { signal });

export default {
  getAll,
  get,
  create,
  update,
  remove,
  getDisponibilidad,
  getVehiculosDisponibles,
};
