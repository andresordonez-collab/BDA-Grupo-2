import http from '../http-common';

const getAll = () => http.get('/api/movimientos-billeteras');

const get = (id) => http.get(`/api/movimientos-billeteras/${id}`);

const create = (data) => http.post('/api/movimientos-billeteras', data);

const update = (id, data) => http.put(`/api/movimientos-billeteras/${id}`, data);

const remove = (id) => http.delete(`/api/movimientos-billeteras/${id}`);

export default { getAll, get, create, update, remove };
