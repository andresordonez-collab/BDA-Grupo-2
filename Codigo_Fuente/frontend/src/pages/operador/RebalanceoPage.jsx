import { useCallback, useEffect, useMemo, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import RefreshIcon from '@mui/icons-material/Refresh';
import estacionService from '../../services/estacionService';
import rebalanceoService from '../../services/rebalanceoService';
import ordenRebalanceoService from '../../services/ordenRebalanceoService';
import usuarioService from '../../services/usuarioService';
import { formatearFechaHora } from '../../utils/formato';
import { getErrorMessage } from '../../http-common';

function idDe(objeto) {
  const id = objeto.idEstacion ?? objeto.id_estacion;
  return id === null || id === undefined ? null : String(id);
}

function normalizarDisponibilidad(item) {
  const id = idDe(item);

  return {
    id,
    nombre:
      item.nombreEstacion ??
      item.nombre_estacion ??
      `Estación #${id}`,
    cantidadBicicletas: Number(
      item.cantidadBicicletas ?? item.cantidad_bicicletas ?? 0
    ),
    cantidadScooters: Number(
      item.cantidadScooters ?? item.cantidad_scooters ?? 0
    ),
  };
}

function normalizarEstacion(estacion, disponibilidad) {
  const id = idDe(estacion);
  const capacidad = estacion.capacidad ?? null;
  const ocupacion =
    estacion.ocupacion ??
    estacion.ocupacionActual ??
    estacion.vehiculosAsignados ??
    null;
  const activa =
    estacion.estacionActiva ?? estacion.estacion_activa ?? null;

  return {
    id,
    nombre:
      estacion.nombreEstacion ??
      estacion.nombre_estacion ??
      `Estación #${id}`,
    capacidad: capacidad === null ? null : Number(capacidad),
    ocupacion: ocupacion === null ? null : Number(ocupacion),
    activa:
      activa === null
        ? null
        : activa === true || activa === 'true',
    disponibles: disponibilidad
      ? disponibilidad.cantidadBicicletas +
        disponibilidad.cantidadScooters
      : null,
    bicicletas: disponibilidad?.cantidadBicicletas ?? null,
    scooters: disponibilidad?.cantidadScooters ?? null,
  };
}

function desdeDisponibilidad(item) {
  return {
    id: item.id,
    nombre: item.nombre,
    capacidad: null,
    ocupacion: null,
    activa: null,
    disponibles:
      item.cantidadBicicletas + item.cantidadScooters,
    bicicletas: item.cantidadBicicletas,
    scooters: item.cantidadScooters,
  };
}

export default function RebalanceoPage() {
  const { usuario } = useOutletContext() ?? {};
  const idOperador = usuario?.idUsuario ?? null;

  const [estaciones, setEstaciones] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState('');
  const [exito, setExito] = useState('');

  const [origenId, setOrigenId] = useState('');
  const [destinoId, setDestinoId] = useState('');
  const [cantidad, setCantidad] = useState('1');
  const [enviando, setEnviando] = useState(false);

  const [ordenes, setOrdenes] = useState([]);
  const [usuarios, setUsuarios] = useState([]);
  const [cargandoOrdenes, setCargandoOrdenes] = useState(true);
  const [errorOrdenes, setErrorOrdenes] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');
    setAviso('');

    const [resEstaciones, resDisponibilidad] = await Promise.allSettled([
      estacionService.getAll(),
      estacionService.getDisponibilidad(),
    ]);

    try {
      const disponibilidad =
        resDisponibilidad.status === 'fulfilled' &&
        Array.isArray(resDisponibilidad.value.data)
          ? resDisponibilidad.value.data.map(normalizarDisponibilidad)
          : [];

      const mapa = new Map(
        disponibilidad.map((item) => [item.id, item])
      );

      if (
        resEstaciones.status === 'fulfilled' &&
        Array.isArray(resEstaciones.value.data)
      ) {
        setEstaciones(
          resEstaciones.value.data.map((estacion) =>
            normalizarEstacion(
              estacion,
              mapa.get(idDe(estacion))
            )
          )
        );

        if (resDisponibilidad.status === 'rejected') {
          setAviso(
            'No se pudo cargar la disponibilidad. Se muestran los datos base de las estaciones.'
          );
        }
      } else if (disponibilidad.length > 0) {
        setEstaciones(disponibilidad.map(desdeDisponibilidad));
        setAviso(
          'Algunos datos de las estaciones no están disponibles. Solo se muestran estaciones con vehículos disponibles.'
        );
      } else {
        const motivo =
          resEstaciones.status === 'rejected'
            ? resEstaciones.reason
            : resDisponibilidad.reason;

        setError(getErrorMessage(motivo));
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setCargando(false);
    }
  }, []);

  const cargarOrdenes = useCallback(async () => {
    setCargandoOrdenes(true);
    setErrorOrdenes('');

    try {
      const { data } = await ordenRebalanceoService.getAll();
      setOrdenes(Array.isArray(data) ? data : []);
    } catch (err) {
      setErrorOrdenes(getErrorMessage(err));
    } finally {
      setCargandoOrdenes(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  useEffect(() => {
    let activo = true;

    cargarOrdenes();

    usuarioService
      .getAll()
      .then(({ data }) => {
        if (activo) {
          setUsuarios(Array.isArray(data) ? data : []);
        }
      })
      .catch(() => {
        if (activo) {
          setUsuarios([]);
        }
      });

    return () => {
      activo = false;
    };
  }, [cargarOrdenes]);

  const nombreEstacion = (id) =>
    estaciones.find((estacion) => estacion.id === String(id))
      ?.nombre ?? `#${id}`;

  const nombreOperador = (id) =>
    usuarios.find(
      (usuario) => String(usuario.idUsuario) === String(id)
    )?.nombreUsuario ?? `Operador #${id}`;

  const origen = useMemo(
    () => estaciones.find((estacion) => estacion.id === origenId) ?? null,
    [estaciones, origenId]
  );

  const destino = useMemo(
    () => estaciones.find((estacion) => estacion.id === destinoId) ?? null,
    [estaciones, destinoId]
  );

  const cupoDestino =
    destino &&
    destino.capacidad !== null &&
    destino.ocupacion !== null
      ? Math.max(0, destino.capacidad - destino.ocupacion)
      : null;

  const validar = () => {
    if (!idOperador) {
      return 'No se pudo identificar al operador. Inicia sesión nuevamente.';
    }

    if (!origen || !destino) {
      return 'Selecciona la estación de origen y la de destino.';
    }

    if (origen.id === destino.id) {
      return 'Las estaciones de origen y destino deben ser diferentes.';
    }

    const numero = Number(cantidad);

    if (!Number.isInteger(numero) || numero <= 0) {
      return 'La cantidad de vehículos debe ser un entero mayor que cero.';
    }

    if (
      origen.disponibles !== null &&
      numero > origen.disponibles
    ) {
      return `La estación de origen solo tiene ${origen.disponibles} vehículos disponibles.`;
    }

    if (destino.activa === false) {
      return 'La estación de destino está inactiva.';
    }

    if (cupoDestino !== null && numero > cupoDestino) {
      return `La estación de destino solo tiene ${cupoDestino} cupos libres.`;
    }

    return '';
  };

  const enviar = async () => {
    const mensaje = validar();

    if (mensaje) {
      setError(mensaje);
      return;
    }

    setEnviando(true);
    setError('');
    setExito('');

    try {
      await rebalanceoService.ejecutar({
        idEstacionOrigen: Number(origen.id),
        idEstacionDestino: Number(destino.id),
        idOperador: Number(idOperador),
        cantidadVehiculos: Number(cantidad),
      });

      setExito(
        `Se trasladaron ${cantidad} vehículos de ${origen.nombre} a ${destino.nombre}.`
      );
      setCantidad('1');
      await cargar();
      await cargarOrdenes();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 700 }} gutterBottom>
        Rebalanceo
      </Typography>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Traslada vehículos desde una estación con exceso hacia una con
        déficit.
      </Typography>

      {!idOperador && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          No se pudo identificar al operador. La ejecución estará
          deshabilitada hasta que se integre la sesión.
        </Alert>
      )}

      {aviso && (
        <Alert
          severity="warning"
          sx={{ mb: 2 }}
          onClose={() => setAviso('')}
        >
          {aviso}
        </Alert>
      )}

      {error && (
        <Alert
          severity="error"
          sx={{ mb: 2 }}
          onClose={() => setError('')}
        >
          {error}
        </Alert>
      )}

      {exito && (
        <Alert
          severity="success"
          sx={{ mb: 2 }}
          onClose={() => setExito('')}
        >
          {exito}
        </Alert>
      )}

      {cargando ? (
        <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
          <CircularProgress />
        </Box>
      ) : (
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: {
              xs: '1fr',
              md: '1.2fr 1fr',
            },
            gap: 2,
          }}
        >
          <Paper variant="outlined" sx={{ p: 3 }}>
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                gap: 2,
                mb: 2,
              }}
            >
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Nueva orden
              </Typography>

              <Button
                size="small"
                startIcon={<RefreshIcon />}
                onClick={cargar}
                disabled={cargando}
              >
                Actualizar
              </Button>
            </Box>

            <Box sx={{ display: 'grid', gap: 2 }}>
              <TextField
                select
                fullWidth
                label="Estación de origen"
                value={origenId}
                onChange={(event) => setOrigenId(event.target.value)}
              >
                <MenuItem value="">Selecciona una estación</MenuItem>
                {estaciones.map((estacion) => (
                  <MenuItem key={estacion.id} value={estacion.id}>
                    {estacion.nombre}
                    {estacion.disponibles !== null
                      ? ` · ${estacion.disponibles} disponibles`
                      : ''}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                select
                fullWidth
                label="Estación de destino"
                value={destinoId}
                onChange={(event) => setDestinoId(event.target.value)}
              >
                <MenuItem value="">Selecciona una estación</MenuItem>
                {estaciones
                  .filter((estacion) => estacion.id !== origenId)
                  .map((estacion) => (
                    <MenuItem key={estacion.id} value={estacion.id}>
                      {estacion.nombre}
                      {estacion.capacidad !== null
                        ? ` · ${estacion.capacidad} de capacidad`
                        : ''}
                    </MenuItem>
                  ))}
              </TextField>

              <TextField
                type="number"
                fullWidth
                label="Cantidad de vehículos"
                value={cantidad}
                onChange={(event) => setCantidad(event.target.value)}
                slotProps={{
                  htmlInput: {
                    min: 1,
                    step: 1,
                  },
                }}
              />

              <Button
                variant="contained"
                size="large"
                startIcon={<SwapHorizIcon />}
                onClick={enviar}
                disabled={enviando || !idOperador}
              >
                {enviando
                  ? 'Ejecutando…'
                  : 'Ejecutar rebalanceo'}
              </Button>
            </Box>
          </Paper>

          <Paper variant="outlined" sx={{ p: 3 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }} gutterBottom>
              Resumen
            </Typography>

            <Box sx={{ display: 'grid', gap: 1.5 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Origen
                </Typography>
                <Typography variant="body2">
                  {origen
                    ? `${origen.nombre} · ${
                        origen.disponibles ?? '—'
                      } disponibles`
                    : 'Sin seleccionar'}
                </Typography>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  Destino
                </Typography>
                <Typography variant="body2">
                  {destino
                    ? `${destino.nombre} · ${
                        destino.ocupacion ?? '—'
                      }/${destino.capacidad ?? '—'} ocupados`
                    : 'Sin seleccionar'}
                </Typography>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  Cupo libre estimado en destino
                </Typography>
                <Typography variant="body2">
                  {cupoDestino ?? '—'}
                </Typography>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  Operador
                </Typography>
                <Typography variant="body2">
                  {usuario?.nombreUsuario ?? 'No identificado'}
                </Typography>
              </Box>
            </Box>

            <Alert severity="info" sx={{ mt: 3 }}>
              La operación se realiza de forma segura: si no hay
              vehículos suficientes o falta capacidad, no se modifica
              nada.
            </Alert>
          </Paper>
        </Box>
      )}

      <Paper variant="outlined" sx={{ mt: 3 }}>
        <Box
          sx={{
            p: 2,
            borderBottom: '1px solid',
            borderColor: 'divider',
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Historial de órdenes
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Últimos rebalanceos registrados en la base de datos.
          </Typography>
        </Box>

        {errorOrdenes && (
          <Alert
            severity="warning"
            sx={{ m: 2 }}
            onClose={() => setErrorOrdenes('')}
          >
            {errorOrdenes}
          </Alert>
        )}

        {cargandoOrdenes ? (
          <Box
            sx={{
              py: 4,
              display: 'flex',
              justifyContent: 'center',
            }}
          >
            <CircularProgress size={28} />
          </Box>
        ) : (
          <TableContainer>
            <Table sx={{ minWidth: 850 }}>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Origen</TableCell>
                  <TableCell>Destino</TableCell>
                  <TableCell align="right">Cantidad</TableCell>
                  <TableCell>Operador</TableCell>
                  <TableCell>Fecha</TableCell>
                  <TableCell>Estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {ordenes.map((orden) => (
                  <TableRow key={orden.idOrden} hover>
                    <TableCell>{orden.idOrden}</TableCell>
                    <TableCell>
                      {nombreEstacion(orden.idEstacionOrigen)}
                    </TableCell>
                    <TableCell>
                      {nombreEstacion(orden.idEstacionDestino)}
                    </TableCell>
                    <TableCell align="right">
                      {orden.cantidadVehiculos}
                    </TableCell>
                    <TableCell>
                      {nombreOperador(orden.idOperador)}
                    </TableCell>
                    <TableCell>
                      {formatearFechaHora(orden.fechaOrden)}
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        color={
                          orden.estadoOrden === 'Completada'
                            ? 'success'
                            : 'default'
                        }
                        label={orden.estadoOrden}
                      />
                    </TableCell>
                  </TableRow>
                ))}

                {ordenes.length === 0 && (
                  <TableRow>
                    <TableCell
                      colSpan={7}
                      align="center"
                      sx={{ py: 4 }}
                    >
                      <Typography color="text.secondary">
                        No hay órdenes de rebalanceo registradas.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>
    </Box>
  );
}