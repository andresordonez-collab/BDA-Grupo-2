import http from '../http-common';

async function getActivas(signal) {
  const [tarifas, tipos] = await Promise.all([
    http.get('/api/tarifas', { signal }),
    http.get('/api/tipos-vehiculos', { signal }),
  ]);

  const nombres = new Map(
    tipos.data.map((tipo) => [
      tipo.idTipoVehiculo,
      tipo.nombreTipoVehiculo,
    ]),
  );

  return tarifas.data
    .filter((tarifa) => tarifa.activa)
    .map((tarifa) => ({
      ...tarifa,
      nombreTipoVehiculo:
        nombres.get(tarifa.idTipoVehiculo) ?? 'Vehículo',
    }));
}

export default { getActivas };