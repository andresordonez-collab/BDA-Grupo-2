import http from '../http-common';

const ejecutar = (payload) => http.post('/api/rebalanceos', payload);

export default { ejecutar };