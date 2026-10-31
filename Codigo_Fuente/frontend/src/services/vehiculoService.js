import http from '../http-common';

const getAll = () => http.get('/api/vehiculos');

const cambiarEstado = (idVehiculo, estadoVehiculo, motivo) =>
  http.patch(`/api/vehiculos/${idVehiculo}/estado`, {
    estadoVehiculo,
    motivo,
  });

export default { getAll, cambiarEstado };
