import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  InputAdornment,
  LinearProgress,
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
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import estacionService from '../../services/estacionService';
import EstacionFormDialog from '../../components/operador/EstacionFormDialog';
import { getErrorMessage } from '../../http-common';

const FILTROS_ACTIVA = [
  { value: '', label: 'Todas' },
  { value: 'true', label: 'Activas' },
  { value: 'false', label: 'Inactivas' },
];

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
    bateriaPromedio:
      item.bateriaPromedio ?? item.bateria_promedio ?? null,
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
    direccion: estacion.direccion ?? '—',
    latitud: estacion.latitud ?? null,
    longitud: estacion.longitud ?? null,
    capacidad: capacidad === null ? null : Number(capacidad),
    ocupacion: ocupacion === null ? null : Number(ocupacion),
    activa: activa === null ? null : Boolean(activa),
    cantidadBicicletas:
      estacion.cantidadBicicletas ??
      estacion.cantidad_bicicletas ??
      disponibilidad?.cantidadBicicletas ??
      null,
    cantidadScooters:
      estacion.cantidadScooters ??
      estacion.cantidad_scooters ??
      disponibilidad?.cantidadScooters ??
      null,
    bateriaPromedio:
      estacion.bateriaPromedio ??
      estacion.bateria_promedio ??
      disponibilidad?.bateriaPromedio ??
      null,
  };
}

function desdeDisponibilidad(item) {
  return {
    ...item,
    direccion: '—',
    capacidad: null,
    ocupacion: null,
    activa: null,
  };
}

function Bateria({ valor }) {
  if (valor === null || valor === undefined) {
    return (
      <Typography variant="body2" color="text.secondary">
        —
      </Typography>
    );
  }

  const nivel = Number(valor);
  const color = nivel < 20 ? 'error' : nivel < 50 ? 'warning' : 'success';

  return (
    <Chip
      size="small"
      variant="outlined"
      color={color}
      label={`${nivel.toFixed(1)}%`}
    />
  );
}

function EstadoEstacion({ activa }) {
  if (activa === null || activa === undefined) {
    return <Chip size="small" label="—" />;
  }

  return (
    <Chip
      size="small"
      color={activa ? 'success' : 'default'}
      variant={activa ? 'filled' : 'outlined'}
      label={activa ? 'Activa' : 'Inactiva'}
    />
  );
}

function Ocupacion({ estacion }) {
  if (estacion.capacidad === null || estacion.ocupacion === null) {
    return (
      <Typography variant="body2" color="text.secondary">
        —
      </Typography>
    );
  }

  const porcentaje = Math.min(
    100,
    Math.round((estacion.ocupacion / estacion.capacidad) * 100)
  );
  const color =
    porcentaje >= 90 ? 'error' : porcentaje >= 70 ? 'warning' : 'success';

  return (
    <Box sx={{ minWidth: 130 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1 }}>
        <Typography variant="caption">
          {estacion.ocupacion}/{estacion.capacidad}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {porcentaje}%
        </Typography>
      </Box>

      <LinearProgress
        variant="determinate"
        value={porcentaje}
        color={color}
        sx={{ mt: 0.5, height: 6, borderRadius: 1 }}
      />
    </Box>
  );
}

function Disponibles({ estacion }) {
  if (
    estacion.cantidadBicicletas === null &&
    estacion.cantidadScooters === null
  ) {
    return (
      <Typography variant="body2" color="text.secondary">
        —
      </Typography>
    );
  }

  return (
    <Typography variant="body2">
      {estacion.cantidadBicicletas ?? 0} bicis ·{' '}
      {estacion.cantidadScooters ?? 0} scooters
    </Typography>
  );
}

export default function EstacionesPage() {
  const [estaciones, setEstaciones] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState('');

  const [busqueda, setBusqueda] = useState('');
  const [filtroActiva, setFiltroActiva] = useState('');
  const [formAbierto, setFormAbierto] = useState(false);
  const [estacionEditando, setEstacionEditando] = useState(null);

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
          'Algunos datos de las estaciones no están disponibles. Se muestra la información de disponibilidad.'
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

  useEffect(() => {
    cargar();
  }, [cargar]);

  const filtradas = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();

    return estaciones.filter((estacion) => {
      const coincideTexto =
        !texto ||
        estacion.nombre.toLowerCase().includes(texto) ||
        estacion.direccion.toLowerCase().includes(texto);

      const coincideActiva =
        !filtroActiva ||
        (filtroActiva === 'true'
          ? estacion.activa === true
          : estacion.activa === false);

      return coincideTexto && coincideActiva;
    });
  }, [estaciones, busqueda, filtroActiva]);

  const resumen = useMemo(() => {
    const todasConCapacidad =
      estaciones.length > 0 &&
      estaciones.every((estacion) => estacion.capacidad !== null);

    const todasConOcupacion =
      estaciones.length > 0 &&
      estaciones.every((estacion) => estacion.ocupacion !== null);

    const capacidadTotal = todasConCapacidad
      ? estaciones.reduce(
          (total, estacion) => total + estacion.capacidad,
          0
        )
      : null;

    const ocupacionTotal = todasConOcupacion
      ? estaciones.reduce(
          (total, estacion) => total + estacion.ocupacion,
          0
        )
      : null;

    const disponibles = estaciones.reduce(
      (total, estacion) =>
        total +
        Number(estacion.cantidadBicicletas ?? 0) +
        Number(estacion.cantidadScooters ?? 0),
      0
    );

    const porcentaje =
      capacidadTotal !== null &&
      ocupacionTotal !== null &&
      capacidadTotal > 0
        ? Math.round((ocupacionTotal / capacidadTotal) * 100)
        : null;

    return {
      total: estaciones.length,
      capacidadTotal,
      ocupacionTotal,
      disponibles,
      porcentaje,
    };
  }, [estaciones]);

  const hayFiltros = Boolean(busqueda || filtroActiva);

  const limpiarFiltros = () => {
    setBusqueda('');
    setFiltroActiva('');
  };

  const abrirCrearEstacion = () => {
    setEstacionEditando(null);
    setError('');
    setAviso('');
    setFormAbierto(true);
  };

  const abrirEditarEstacion = (estacion) => {
    setEstacionEditando(estacion);
    setError('');
    setAviso('');
    setFormAbierto(true);
  };

  const cerrarFormulario = () => {
    setFormAbierto(false);
    setEstacionEditando(null);
  };

  const estacionGuardada = async () => {
    setFormAbierto(false);
    setEstacionEditando(null);
    setAviso('Estación guardada correctamente.');
    await cargar();
  };

  const tarjetas = [
    { label: 'Estaciones', valor: resumen.total },
    {
      label: 'Capacidad total',
      valor:
        resumen.capacidadTotal === null
          ? '—'
          : resumen.capacidadTotal.toLocaleString('es-CL'),
    },
    {
      label: 'Ocupación total',
      valor:
        resumen.ocupacionTotal === null
          ? '—'
          : `${resumen.ocupacionTotal.toLocaleString('es-CL')}${
              resumen.porcentaje !== null
                ? ` (${resumen.porcentaje}%)`
                : ''
            }`,
    },
    {
      label: 'Vehículos disponibles',
      valor: resumen.disponibles,
    },
  ];

  return (
    <Box>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: 2,
          mb: 2,
          flexWrap: 'wrap',
        }}
      >
        <Typography variant="h4" sx={{ fontWeight: 700 }}>
          Estaciones
        </Typography>

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={abrirCrearEstacion}
        >
          Nueva estación
        </Button>
      </Box>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Capacidad, ocupación y disponibilidad por estación.
      </Typography>

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
        {tarjetas.map(({ label, valor }) => (
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
          </Paper>
        ))}
      </Box>

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
            size="small"
            label="Buscar"
            placeholder="Nombre o dirección"
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
            sx={{ minWidth: 260, flexGrow: 1 }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              },
            }}
          />

          <TextField
            select
            size="small"
            label="Estado"
            value={filtroActiva}
            onChange={(event) => setFiltroActiva(event.target.value)}
            sx={{ minWidth: 170 }}
          >
            {FILTROS_ACTIVA.map((filtro) => (
              <MenuItem key={filtro.value} value={filtro.value}>
                {filtro.label}
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

          {hayFiltros && <Button onClick={limpiarFiltros}>Limpiar</Button>}
        </Box>
      </Paper>

      {cargando ? (
        <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table sx={{ minWidth: 950 }}>
            <TableHead>
              <TableRow>
                <TableCell>Estación</TableCell>
                <TableCell>Dirección</TableCell>
                <TableCell>Capacidad</TableCell>
                <TableCell>Ocupación</TableCell>
                <TableCell>Disponibles</TableCell>
                <TableCell>Batería promedio</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>

            <TableBody>
              {filtradas.map((estacion) => (
                <TableRow key={estacion.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>
                      {estacion.nombre}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      ID {estacion.id}
                    </Typography>
                  </TableCell>

                  <TableCell>{estacion.direccion}</TableCell>

                  <TableCell>
                    {estacion.capacidad === null
                      ? '—'
                      : estacion.capacidad}
                  </TableCell>

                  <TableCell>
                    <Ocupacion estacion={estacion} />
                  </TableCell>

                  <TableCell>
                    <Disponibles estacion={estacion} />
                  </TableCell>

                  <TableCell>
                    <Bateria valor={estacion.bateriaPromedio} />
                  </TableCell>

                  <TableCell>
                    <EstadoEstacion activa={estacion.activa} />
                  </TableCell>

                  <TableCell align="right">
                    <Button
                      size="small"
                      startIcon={<EditIcon />}
                      onClick={() => abrirEditarEstacion(estacion)}
                    >
                      Editar
                    </Button>
                  </TableCell>
                </TableRow>
              ))}

              {filtradas.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 5 }}>
                    <Typography color="text.secondary">
                      {estaciones.length === 0
                        ? 'No hay estaciones registradas.'
                        : 'No hay estaciones que coincidan con los filtros.'}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <EstacionFormDialog
        abierto={formAbierto}
        estacion={estacionEditando}
        onCerrar={cerrarFormulario}
        onGuardado={estacionGuardada}
      />
    </Box>
  );
}