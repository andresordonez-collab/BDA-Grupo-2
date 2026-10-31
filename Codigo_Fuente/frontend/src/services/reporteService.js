import http from '../http-common';

const getDemandaEstacionHoraria = () =>
  http.get('/api/reportes/demanda-estacion-horaria');

export default { getDemandaEstacionHoraria };