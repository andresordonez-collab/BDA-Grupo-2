import http from '../http-common';

async function getHistorial(limite = 50, signal) {
  const { data } = await http.get('/api/viajes/mis-viajes', {
    params: { limite },
    signal,
  });
  return data;
}

async function getResumenMensual(anio, mes, signal) {
  const { data } = await http.get('/api/viajes/resumen-mensual', {
    params: { anio, mes },
    signal,
  });
  return data;
}

async function iniciar(idVehiculo) {
  const { data } = await http.post('/api/viajes', { idVehiculo });
  return data;
}

async function finalizar(idViaje, idEstacionDestino) {
  await http.post(`/api/viajes/${idViaje}/finalizar`, {
    idEstacionDestino,
  });
}

export default {
  getHistorial,
  getResumenMensual,
  iniciar,
  finalizar,
};