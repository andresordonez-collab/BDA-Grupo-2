import http from '../http-common';

const getAll = () => http.get('/api/billeteras');

const get = (id) => http.get(`/api/billeteras/${id}`);

const create = (data) => http.post('/api/billeteras', data);

const update = (id, data) => http.put(`/api/billeteras/${id}`, data);

const remove = (id) => http.delete(`/api/billeteras/${id}`);

export default { getAll, get, create, update, remove };
