import http from '../http-common';

const getAll = () => http.get('/api/tipos-vehiculos');

const get = (id) => http.get(`/api/tipos-vehiculos/${id}`);

const create = (data) => http.post('/api/tipos-vehiculos', data);

const update = (id, data) => http.put(`/api/tipos-vehiculos/${id}`, data);

const remove = (id) => http.delete(`/api/tipos-vehiculos/${id}`);

export default { getAll, get, create, update, remove };