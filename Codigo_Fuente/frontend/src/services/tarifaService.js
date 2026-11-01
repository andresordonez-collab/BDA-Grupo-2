import http from '../http-common';

const getAll = () => http.get('/api/tarifas');

const get = (id) => http.get(`/api/tarifas/${id}`);

const create = (data) => http.post('/api/tarifas', data);

const update = (id, data) => http.put(`/api/tarifas/${id}`, data);

const remove = (id) => http.delete(`/api/tarifas/${id}`);

export default { getAll, get, create, update, remove };