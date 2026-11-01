import http from '../http-common';

const getAll = () => http.get('/api/auditorias-vehiculos');

export default { getAll };