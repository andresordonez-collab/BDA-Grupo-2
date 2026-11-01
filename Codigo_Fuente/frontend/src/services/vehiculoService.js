import http from '../http-common';

const getAll = () => http.get('/api/vehiculos');

const get = (id) => http.get(`/api/vehiculos/${id}`);

const create = (data) => http.post('/api/vehiculos', data);

const update = (id, data) =>
  http.put(`/api/vehiculos/${id}`, data);

const cambiarEstado = (idVehiculo, estadoVehiculo, motivo) =>
  http.patch(`/api/vehiculos/${idVehiculo}/estado`, {
    estadoVehiculo,
    motivo,
  });

const remove = (id) => http.delete(`/api/vehiculos/${id}`);

export default {
  getAll,
  get,
  create,
  update,
  cambiarEstado,
  remove,
};
