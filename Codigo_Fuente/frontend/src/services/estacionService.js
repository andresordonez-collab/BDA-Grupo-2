import http from '../http-common';

const getAll = (signal) => http.get('/api/estaciones', { signal });

const get = (id) => http.get(`/api/estaciones/${id}`);

const create = (data) => http.post('/api/estaciones', data);

const update = (id, data) => http.put(`/api/estaciones/${id}`, data);

const remove = (id) => http.delete(`/api/estaciones/${id}`);

const getDisponibilidad = () =>
  http.get('/api/estaciones/disponibilidad');

export default {
  getAll,
  get,
  create,
  update,
  remove,
  getDisponibilidad,
};
