import http from '../http-common';

const getAll = () => http.get('/api/roles');

const get = (id) => http.get(`/api/roles/${id}`);

const create = (data) => http.post('/api/roles', data);

const update = (id, data) => http.put(`/api/roles/${id}`, data);

const remove = (id) => http.delete(`/api/roles/${id}`);

export default { getAll, get, create, update, remove };