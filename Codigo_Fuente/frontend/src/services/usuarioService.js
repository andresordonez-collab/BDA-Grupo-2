import http from '../http-common';

const getAll = () => http.get('/api/usuarios');

const get = (id) => http.get(`/api/usuarios/${id}`);

const create = (data) => http.post('/api/usuarios', data);

const update = (id, data) => http.put(`/api/usuarios/${id}`, data);

const remove = (id) => http.delete(`/api/usuarios/${id}`);

export default { getAll, get, create, update, remove };