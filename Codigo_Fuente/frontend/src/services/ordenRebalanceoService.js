import http from '../http-common';

const getAll = () => http.get('/api/ordenes-rebalanceo');

const get = (id) => http.get(`/api/ordenes-rebalanceo/${id}`);

export default { getAll, get };
