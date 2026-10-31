import http from '../http-common';

const getAll = () => http.get('/api/vehiculos/auditorias');

export default { getAll };