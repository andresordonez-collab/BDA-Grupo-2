import { delay, http, HttpResponse } from 'msw';
import {
  auditorias,
  demanda,
  estaciones,
  operadores,
  vehiculos,
} from './data';

const ESTADOS = ['Disponible', 'En uso', 'En mantención', 'Dado de baja'];

let siguienteAuditoria = 5;

function nombreEstacion(idEstacion) {
  return (
    estaciones.find((estacion) => estacion.idEstacion === idEstacion)
      ?.nombreEstacion ?? null
  );
}

function disponibilidad() {
  return estaciones
    .map((estacion) => {
      const disponibles = vehiculos.filter(
        (vehiculo) =>
          vehiculo.idEstacion === estacion.idEstacion &&
          vehiculo.estadoVehiculo === 'Disponible'
      );

      const bicicletas = disponibles.filter(
        (vehiculo) => vehiculo.tipoVehiculo === 'Bicicleta'
      );
      const scooters = disponibles.filter(
        (vehiculo) => vehiculo.tipoVehiculo === 'Scooter'
      );
      const baterias = scooters
        .map((vehiculo) => vehiculo.nivelBateria)
        .filter((nivel) => typeof nivel === 'number');

      const bateriaPromedio = baterias.length
        ? Number(
            (
              baterias.reduce((total, nivel) => total + nivel, 0) /
              baterias.length
            ).toFixed(2)
          )
        : null;

      return {
        idEstacion: estacion.idEstacion,
        nombreEstacion: estacion.nombreEstacion,
        cantidadBicicletas: bicicletas.length,
        cantidadScooters: scooters.length,
        bateriaPromedio,
      };
    })
    .filter(
      (estacion) =>
        estacion.cantidadBicicletas + estacion.cantidadScooters > 0
    );
}

function estacionesConOcupacion() {
  return estaciones.map((estacion) => {
    const asignados = vehiculos.filter(
      (vehiculo) => vehiculo.idEstacion === estacion.idEstacion
    );

    return {
      ...estacion,
      ocupacion: asignados.length,
      disponibles: asignados.filter(
        (vehiculo) => vehiculo.estadoVehiculo === 'Disponible'
      ).length,
    };
  });
}

function vehiculosConEstacion() {
  return vehiculos.map((vehiculo) => ({
    ...vehiculo,
    nombreEstacion: nombreEstacion(vehiculo.idEstacion),
  }));
}

function error(mensaje, status = 400, codigo = 'SOLICITUD_INVALIDA') {
  return HttpResponse.json({ codigo, mensaje }, { status });
}

export const handlers = [
  http.get('/api/estaciones/disponibilidad', async () => {
    await delay(200);
    return HttpResponse.json(disponibilidad());
  }),

  http.get('/api/estaciones', async () => {
    await delay(200);
    return HttpResponse.json(estacionesConOcupacion());
  }),

  http.get('/api/vehiculos/auditorias', async () => {
    await delay(200);
    return HttpResponse.json(
      [...auditorias].sort(
        (a, b) =>
          new Date(b.fechaAuditoria).getTime() -
          new Date(a.fechaAuditoria).getTime()
      )
    );
  }),

  http.get('/api/vehiculos', async () => {
    await delay(200);
    return HttpResponse.json(vehiculosConEstacion());
  }),

  http.patch('/api/vehiculos/:id/estado', async ({ params, request }) => {
    const idVehiculo = Number(params.id);
    const vehiculo = vehiculos.find(
      (item) => item.idVehiculo === idVehiculo
    );

    if (!vehiculo) {
      return error('Vehículo no encontrado.', 404, 'VEHICULO_NO_ENCONTRADO');
    }

    let body = {};

    try {
      body = await request.json();
    } catch {
      return error('Debes enviar un estado y un motivo.');
    }

    const { estadoVehiculo, motivo } = body;

    if (!ESTADOS.includes(estadoVehiculo)) {
      return error('El estado del vehículo no es válido.');
    }

    const requiereMotivo = ['En mantención', 'Dado de baja'].includes(
      estadoVehiculo
    );

    if (requiereMotivo && !String(motivo ?? '').trim()) {
      return error(
        'Debes indicar un motivo para mantención o baja.',
        400,
        'MOTIVO_REQUERIDO'
      );
    }

    const estadoAnterior = vehiculo.estadoVehiculo;
    vehiculo.estadoVehiculo = estadoVehiculo;

    if (estadoVehiculo === 'Dado de baja') {
      vehiculo.idEstacion = null;
    }

    if (requiereMotivo) {
      auditorias.unshift({
        idAuditoria: siguienteAuditoria,
        idVehiculo: vehiculo.idVehiculo,
        codigoVehiculo: vehiculo.codigoVehiculo,
        idOperador: operadores[0].idOperador,
        nombreOperador: operadores[0].nombreOperador,
        estadoAnterior,
        estadoNuevo: estadoVehiculo,
        fechaAuditoria: new Date().toISOString(),
        motivo: String(motivo).trim(),
      });
      siguienteAuditoria += 1;
    }

    await delay(300);
    return new HttpResponse(null, { status: 204 });
  }),

  http.post('/api/rebalanceos', async ({ request }) => {
    let body = {};

    try {
      body = await request.json();
    } catch {
      return error('Debes enviar los datos del rebalanceo.');
    }

    const idEstacionOrigen = Number(body.idEstacionOrigen);
    const idEstacionDestino = Number(body.idEstacionDestino);
    const cantidadVehiculos = Number(body.cantidadVehiculos);
    const idOperador = Number(body.idOperador);

    const origen = estaciones.find(
      (estacion) => estacion.idEstacion === idEstacionOrigen
    );
    const destino = estaciones.find(
      (estacion) => estacion.idEstacion === idEstacionDestino
    );
    const operador = operadores.find(
      (item) => item.idOperador === idOperador
    );

    if (!origen || !destino) {
      return error('Debe indicar estaciones de origen y destino válidas.');
    }

    if (idEstacionOrigen === idEstacionDestino) {
      return error(
        'Las estaciones de origen y destino deben ser diferentes.',
        409,
        'REBALANCEO_RECHAZADO'
      );
    }

    if (!operador) {
      return error(
        'El operador no existe o no tiene permisos.',
        409,
        'REBALANCEO_RECHAZADO'
      );
    }

    if (!Number.isInteger(cantidadVehiculos) || cantidadVehiculos <= 0) {
      return error('La cantidad de vehículos debe ser mayor que cero.');
    }

    if (origen.estacionActiva === false || destino.estacionActiva === false) {
      return error(
        'La estación de origen o destino está inactiva.',
        409,
        'REBALANCEO_RECHAZADO'
      );
    }

    const disponibles = vehiculos.filter(
      (vehiculo) =>
        vehiculo.idEstacion === idEstacionOrigen &&
        vehiculo.estadoVehiculo === 'Disponible'
    );

    if (disponibles.length < cantidadVehiculos) {
      return error(
        `La estación de origen ${origen.nombreEstacion} tiene solo ${disponibles.length} vehículos disponibles; se solicitaron ${cantidadVehiculos}.`,
        409,
        'REBALANCEO_RECHAZADO'
      );
    }

    const ocupacionDestino = vehiculos.filter(
      (vehiculo) => vehiculo.idEstacion === idEstacionDestino
    ).length;

    if (ocupacionDestino + cantidadVehiculos > destino.capacidad) {
      return error(
        `La estación de destino ${destino.nombreEstacion} no tiene capacidad para recibir ${cantidadVehiculos} vehículos.`,
        409,
        'REBALANCEO_RECHAZADO'
      );
    }

    disponibles.slice(0, cantidadVehiculos).forEach((vehiculo) => {
      vehiculo.idEstacion = idEstacionDestino;
    });

    await delay(400);
    return new HttpResponse(null, { status: 204 });
  }),

  http.get('/api/reportes/demanda-estacion-horaria', async () => {
    await delay(250);
    return HttpResponse.json(demanda);
  }),

  http.get('/api/auth/me', async () => {
    await delay(100);
    return HttpResponse.json({
      idUsuario: 1,
      nombreUsuario: 'Camila Torres',
      email: 'camila.operadora@example.com',
      rol: 'Operador',
    });
  }),

  http.post('/api/auth/login', async ({ request }) => {
    const body = await request.json().catch(() => ({}));
    await delay(200);

    return HttpResponse.json({
      idUsuario: 1,
      nombreUsuario: 'Camila Torres',
      email: body.email ?? 'camila.operadora@example.com',
      rol: 'Operador',
    });
  }),

  http.post('/api/auth/logout', async () => {
    await delay(100);
    return new HttpResponse(null, { status: 204 });
  }),
];
