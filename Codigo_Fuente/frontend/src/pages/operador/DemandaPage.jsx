import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
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
  Tooltip,
  Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import reporteService from '../../services/reporteService';
import { getErrorMessage } from '../../http-common';

const HORAS = Array.from({ length: 24 }, (_, hora) => hora);

function normalizarDemanda(item) {
  return {
    idEstacion: String(item.idEstacion ?? item.id_estacion ?? ''),
    nombreEstacion:
      item.nombreEstacion ??
      item.nombre_estacion ??
      'Sin nombre',
    franjaHoraria: Number(
      item.franjaHoraria ?? item.franja_horaria ?? 0
    ),
    viajesIniciados: Number(
      item.viajesIniciados ?? item.viajes_iniciados ?? 0
    ),
    viajesFinalizados: Number(
      item.viajesFinalizados ?? item.viajes_finalizados ?? 0
    ),
  };
}

function CeldaDemanda({ dato, maximo }) {
  if (!dato) {
    return (
      <TableCell align="center" sx={{ minWidth: 44 }}>
        —
      </TableCell>
    );
  }

  const total = dato.viajesIniciados + dato.viajesFinalizados;
  const intensidad = maximo > 0 ? total / maximo : 0;
  const fondo =
    total > 0
      ? `rgba(15, 118, 110, ${0.08 + intensidad * 0.55})`
      : 'transparent';
  const colorTexto = intensidad > 0.55 ? '#ffffff' : 'inherit';

  return (
    <Tooltip
      title={`Iniciados: ${dato.viajesIniciados} · Finalizados: ${dato.viajesFinalizados}`}
    >
      <TableCell
        align="center"
        sx={{
          minWidth: 44,
          p: 0.5,
          backgroundColor: fondo,
          color: colorTexto,
          fontVariantNumeric: 'tabular-nums',
          cursor: 'default',
        }}
      >
        {total}
      </TableCell>
    </Tooltip>
  );
}

export default function DemandaPage() {
  const [datos, setDatos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [filtroEstacion, setFiltroEstacion] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');

    try {
      const { data } = await reporteService.getDemandaEstacionHoraria();

      setDatos(
        Array.isArray(data) ? data.map(normalizarDemanda) : []
      );
    } catch (err) {
      if (err?.response?.status === 404) {
        setError(
          'La información de demanda no está disponible por el momento.'
        );
      } else {
        setError(getErrorMessage(err));
      }
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const estaciones = useMemo(() => {
    const mapa = new Map();

    datos.forEach((item) => {
      mapa.set(item.idEstacion, item.nombreEstacion);
    });

    return Array.from(mapa, ([id, nombre]) => ({ id, nombre })).sort(
      (a, b) => a.nombre.localeCompare(b.nombre)
    );
  }, [datos]);

  const filtrados = useMemo(
    () =>
      filtroEstacion
        ? datos.filter(
            (item) => item.idEstacion === filtroEstacion
          )
        : datos,
    [datos, filtroEstacion]
  );

  const filas = useMemo(() => {
    const mapa = new Map();

    filtrados.forEach((item) => {
      if (!mapa.has(item.idEstacion)) {
        mapa.set(item.idEstacion, {
          id: item.idEstacion,
          nombre: item.nombreEstacion,
          horas: Array.from({ length: 24 }, () => null),
          iniciados: 0,
          finalizados: 0,
        });
      }

      const fila = mapa.get(item.idEstacion);

      if (item.franjaHoraria >= 0 && item.franjaHoraria < 24) {
        fila.horas[item.franjaHoraria] = item;
      }

      fila.iniciados += item.viajesIniciados;
      fila.finalizados += item.viajesFinalizados;
    });

    return Array.from(mapa.values()).sort((a, b) =>
      a.nombre.localeCompare(b.nombre)
    );
  }, [filtrados]);

  const maximo = useMemo(() => {
    let mayor = 1;

    filas.forEach((fila) => {
      fila.horas.forEach((item) => {
        if (item) {
          mayor = Math.max(
            mayor,
            item.viajesIniciados + item.viajesFinalizados
          );
        }
      });
    });

    return mayor;
  }, [filas]);

  const resumen = useMemo(() => {
    const iniciados = filtrados.reduce(
      (total, item) => total + item.viajesIniciados,
      0
    );
    const finalizados = filtrados.reduce(
      (total, item) => total + item.viajesFinalizados,
      0
    );

    const porEstacion = new Map();
    const porHora = new Map();

    filtrados.forEach((item) => {
      const total = item.viajesIniciados + item.viajesFinalizados;

      porEstacion.set(
        item.nombreEstacion,
        (porEstacion.get(item.nombreEstacion) ?? 0) + total
      );

      porHora.set(
        item.franjaHoraria,
        (porHora.get(item.franjaHoraria) ?? 0) + total
      );
    });

    const topEstacion =
      Array.from(porEstacion.entries()).sort((a, b) => b[1] - a[1])[0] ??
      null;

    const topHora =
      Array.from(porHora.entries()).sort((a, b) => b[1] - a[1])[0] ??
      null;

    return {
      iniciados,
      finalizados,
      topEstacion,
      topHora,
    };
  }, [filtrados]);

  const limpiarFiltros = () => setFiltroEstacion('');

  const tarjetas = [
    {
      label: 'Viajes iniciados',
      valor: resumen.iniciados,
    },
    {
      label: 'Viajes finalizados',
      valor: resumen.finalizados,
    },
    {
      label: 'Estación con más demanda',
      valor: resumen.topEstacion?.[0] ?? '—',
      detalle: resumen.topEstacion?.[1] ?? '',
    },
    {
      label: 'Hora con más demanda',
      valor: resumen.topHora
        ? `${String(resumen.topHora[0]).padStart(2, '0')}:00`
        : '—',
      detalle: resumen.topHora?.[1] ?? '',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 700 }} gutterBottom>
        Demanda
      </Typography>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Viajes iniciados y finalizados por estación y franja horaria.
      </Typography>

      {error && (
        <Alert
          severity="error"
          sx={{ mb: 2 }}
          onClose={() => setError('')}
        >
          {error}
        </Alert>
      )}

      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: {
            xs: 'repeat(2, minmax(0, 1fr))',
            md: 'repeat(4, minmax(0, 1fr))',
          },
          gap: 2,
          mb: 3,
        }}
      >
        {tarjetas.map(({ label, valor, detalle }) => (
          <Paper key={label} variant="outlined" sx={{ p: 2 }}>
            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ fontWeight: 700, textTransform: 'uppercase' }}
            >
              {label}
            </Typography>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              {valor}
            </Typography>
            {detalle !== undefined && detalle !== '' && (
              <Typography variant="caption" color="text.secondary">
                {detalle}
              </Typography>
            )}
          </Paper>
        ))}
      </Box>

      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Box
          sx={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: 2,
            alignItems: 'center',
          }}
        >
          <TextField
            select
            size="small"
            label="Estación"
            value={filtroEstacion}
            onChange={(event) =>
              setFiltroEstacion(event.target.value)
            }
            sx={{ minWidth: 240 }}
          >
            <MenuItem value="">Todas</MenuItem>
            {estaciones.map((estacion) => (
              <MenuItem key={estacion.id} value={estacion.id}>
                {estacion.nombre}
              </MenuItem>
            ))}
          </TextField>

          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={cargar}
            disabled={cargando}
          >
            Actualizar
          </Button>

          {filtroEstacion && (
            <Button onClick={limpiarFiltros}>Limpiar</Button>
          )}
        </Box>
      </Paper>

      {cargando ? (
        <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
          <CircularProgress />
        </Box>
      ) : filas.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 5, textAlign: 'center' }}>
          <Typography color="text.secondary">
            No hay datos de demanda para mostrar.
          </Typography>
        </Paper>
      ) : (
        <Paper variant="outlined">
          <Box sx={{ p: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Demanda por estación y hora
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Iniciados + finalizados por celda.
            </Typography>
          </Box>

          <TableContainer sx={{ overflowX: 'auto' }}>
            <Table sx={{ minWidth: 1500 }}>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ minWidth: 190 }}>Estación</TableCell>
                  {HORAS.map((hora) => (
                    <TableCell
                      key={hora}
                      align="center"
                      sx={{ minWidth: 44, p: 0.5 }}
                    >
                      {String(hora).padStart(2, '0')}
                    </TableCell>
                  ))}
                  <TableCell align="right" sx={{ minWidth: 120 }}>
                    Total
                  </TableCell>
                </TableRow>
              </TableHead>

              <TableBody>
                {filas.map((fila) => {
                  const total = fila.iniciados + fila.finalizados;

                  return (
                    <TableRow key={fila.id} hover>
                      <TableCell>
                        <Typography
                          variant="body2"
                          sx={{ fontWeight: 700 }}
                        >
                          {fila.nombre}
                        </Typography>
                        <Typography
                          variant="caption"
                          color="text.secondary"
                        >
                          ID {fila.id}
                        </Typography>
                      </TableCell>

                      {HORAS.map((hora) => (
                        <CeldaDemanda
                          key={hora}
                          dato={fila.horas[hora]}
                          maximo={maximo}
                        />
                      ))}

                      <TableCell align="right">
                        <Typography
                          variant="body2"
                          sx={{ fontWeight: 700 }}
                        >
                          {total}
                        </Typography>
                        <Typography
                          variant="caption"
                          color="text.secondary"
                        >
                          {fila.iniciados} I · {fila.finalizados} F
                        </Typography>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>

          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              p: 2,
              flexWrap: 'wrap',
            }}
          >
            <Typography variant="caption" color="text.secondary">
              Menor demanda
            </Typography>

            <Box
              sx={{
                width: 140,
                height: 10,
                borderRadius: 1,
                background:
                  'linear-gradient(90deg, rgba(15,118,110,0.08), rgba(15,118,110,0.63))',
              }}
            />

            <Typography variant="caption" color="text.secondary">
              Mayor demanda
            </Typography>

            <Typography variant="caption" color="text.secondary">
              · I = iniciados, F = finalizados
            </Typography>
          </Box>
        </Paper>
      )}
    </Box>
  );
}