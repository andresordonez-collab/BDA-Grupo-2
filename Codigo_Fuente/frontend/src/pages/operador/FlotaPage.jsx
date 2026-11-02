import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  InputAdornment,
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
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import vehiculoService from '../../services/vehiculoService';
import tipoVehiculoService from '../../services/tipoVehiculoService';
import estacionService from '../../services/estacionService';
import VehiculoFormDialog from '../../components/operador/VehiculoFormDialog';
import { getErrorMessage } from '../../http-common';

const ESTADOS = ['Disponible', 'En uso', 'En mantención', 'Dado de baja'];

const ESTADOS_ASIGNABLES = ['Disponible', 'En mantención', 'Dado de baja'];

const COLOR_ESTADO = {
  Disponible: 'success',
  'En uso': 'info',
  'En mantención': 'warning',
  'Dado de baja': 'error',
};

function normalizarVehiculo(vehiculo) {
  const id = vehiculo.idVehiculo ?? vehiculo.id_vehiculo;

  return {
    id,
    codigo: vehiculo.codigoVehiculo ?? vehiculo.codigo_vehiculo ?? `#${id}`,
    codigoVehiculo:
      vehiculo.codigoVehiculo ?? vehiculo.codigo_vehiculo ?? '',
    idTipoVehiculo:
      vehiculo.idTipoVehiculo ?? vehiculo.id_tipo_vehiculo ?? null,
    nivelBateria:
      vehiculo.nivelBateria ?? vehiculo.nivel_bateria ?? null,
    estadoVehiculo:
      vehiculo.estadoVehiculo ?? vehiculo.estado_vehiculo ?? 'Disponible',
    tipo:
      vehiculo.nombreTipoVehiculo ??
      vehiculo.tipoVehiculo ??
      vehiculo.tipo ??
      '—',
    estado: vehiculo.estadoVehiculo ?? vehiculo.estado_vehiculo ?? '—',
    bateria: vehiculo.nivelBateria ?? vehiculo.nivel_bateria ?? null,
    idEstacion: vehiculo.idEstacion ?? vehiculo.id_estacion ?? null,
    estacion:
      vehiculo.nombreEstacion ??
      vehiculo.nombre_estacion ??
      ((vehiculo.estadoVehiculo ?? vehiculo.estado_vehiculo) === 'En uso'
        ? 'En viaje'
        : 'Sin estación'),
  };
}

function EstadoChip({ estado }) {
  return (
    <Chip
      size="small"
      label={estado}
      color={COLOR_ESTADO[estado] ?? 'default'}
      variant={estado === 'Disponible' ? 'filled' : 'outlined'}
    />
  );
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
      label={`${nivel}%`}
    />
  );
}

export default function FlotaPage() {
  const [vehiculos, setVehiculos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');

  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [filtroTipo, setFiltroTipo] = useState('');
  const [filtroEstacion, setFiltroEstacion] = useState('');

  const [vehiculoDialogo, setVehiculoDialogo] = useState(null);
  const [nuevoEstado, setNuevoEstado] = useState('En mantención');
  const [motivo, setMotivo] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [formAbierto, setFormAbierto] = useState(false);
  const [tiposCatalogo, setTiposCatalogo] = useState([]);
  const [estacionesCatalogo, setEstacionesCatalogo] = useState([]);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');

    try {
      const { data } = await vehiculoService.getAll();
      setVehiculos(Array.isArray(data) ? data.map(normalizarVehiculo) : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  useEffect(() => {
    let activo = true;

    Promise.allSettled([
      tipoVehiculoService.getAll(),
      estacionService.getAll(),
    ]).then(([resTipos, resEstaciones]) => {
      if (!activo) return;

      if (resTipos.status === 'fulfilled') {
        const data = resTipos.value.data;
        setTiposCatalogo(Array.isArray(data) ? data : []);
      }

      if (resEstaciones.status === 'fulfilled') {
        const data = resEstaciones.value.data;
        setEstacionesCatalogo(Array.isArray(data) ? data : []);
      }
    });

    return () => {
      activo = false;
    };
  }, []);

  const estaciones = useMemo(() => {
    const mapa = new Map();

    vehiculos.forEach((vehiculo) => {
      if (vehiculo.idEstacion !== null && vehiculo.idEstacion !== undefined) {
        mapa.set(String(vehiculo.idEstacion), vehiculo.estacion);
      }
    });

    return Array.from(mapa, ([id, nombre]) => ({ id, nombre })).sort((a, b) =>
      a.nombre.localeCompare(b.nombre)
    );
  }, [vehiculos]);

  const tipos = useMemo(
    () =>
      [...new Set(vehiculos.map((vehiculo) => vehiculo.tipo).filter(Boolean))].sort(),
    [vehiculos]
  );

  const filtrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();

    return vehiculos.filter((vehiculo) => {
      const coincideTexto =
        !texto ||
        vehiculo.codigo.toLowerCase().includes(texto) ||
        vehiculo.estacion.toLowerCase().includes(texto);
      const coincideEstado = !filtroEstado || vehiculo.estado === filtroEstado;
      const coincideTipo = !filtroTipo || vehiculo.tipo === filtroTipo;
      const coincideEstacion =
        !filtroEstacion || String(vehiculo.idEstacion) === filtroEstacion;

      return (
        coincideTexto &&
        coincideEstado &&
        coincideTipo &&
        coincideEstacion
      );
    });
  }, [vehiculos, busqueda, filtroEstado, filtroTipo, filtroEstacion]);

  const resumen = useMemo(
    () => ({
      total: vehiculos.length,
      disponibles: vehiculos.filter(
        (vehiculo) => vehiculo.estado === 'Disponible'
      ).length,
      enUso: vehiculos.filter((vehiculo) => vehiculo.estado === 'En uso')
        .length,
      mantenimiento: vehiculos.filter(
        (vehiculo) =>
          vehiculo.estado === 'En mantención' ||
          vehiculo.estado === 'Dado de baja'
      ).length,
    }),
    [vehiculos]
  );

  const hayFiltros = Boolean(
    busqueda || filtroEstado || filtroTipo || filtroEstacion
  );

  const limpiarFiltros = () => {
    setBusqueda('');
    setFiltroEstado('');
    setFiltroTipo('');
    setFiltroEstacion('');
  };

  const abrirDialogo = (vehiculo) => {
    setVehiculoDialogo(vehiculo);
    setNuevoEstado(
      vehiculo.estado === 'En mantención' ? 'Disponible' : 'En mantención'
    );
    setMotivo('');
    setError('');
    setExito('');
  };

  const cerrarDialogo = () => {
    if (!guardando) {
      setVehiculoDialogo(null);
    }
  };

  const guardarEstado = async () => {
    if (!vehiculoDialogo) return;

    const requiereMotivo = ['En mantención', 'Dado de baja'].includes(
      nuevoEstado
    );

    if (requiereMotivo && !motivo.trim()) {
      setError('Debes indicar un motivo para mantención o baja.');
      return;
    }

    setGuardando(true);
    setError('');
    setExito('');

    try {
      await vehiculoService.cambiarEstado(
        vehiculoDialogo.id,
        nuevoEstado,
        motivo.trim()
      );

      setExito(
        `Vehículo ${vehiculoDialogo.codigo} actualizado a "${nuevoEstado}".`
      );
      setVehiculoDialogo(null);
      await cargar();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setGuardando(false);
    }
  };

  const abrirCrearVehiculo = () => {
    setError('');
    setExito('');
    setFormAbierto(true);
  };

  const cerrarFormulario = () => {
    setFormAbierto(false);
  };

  const vehiculoGuardado = async () => {
    setFormAbierto(false);
    setExito('Vehículo guardado correctamente.');
    await cargar();
  };

  const tarjetas = [
    { label: 'Total', valor: resumen.total },
    { label: 'Disponibles', valor: resumen.disponibles },
    { label: 'En uso', valor: resumen.enUso },
    { label: 'Mantención / baja', valor: resumen.mantenimiento },
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
          Flota
        </Typography>

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={abrirCrearVehiculo}
        >
          Nuevo vehículo
        </Button>
      </Box>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Vehículos con su estación, batería y estado operativo.
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
            placeholder="Código o estación"
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
            sx={{ minWidth: 240, flexGrow: 1 }}
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
            value={filtroEstado}
            onChange={(event) => setFiltroEstado(event.target.value)}
            sx={{ minWidth: 170 }}
          >
            <MenuItem value="">Todos</MenuItem>
            {ESTADOS.map((estado) => (
              <MenuItem key={estado} value={estado}>
                {estado}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            select
            size="small"
            label="Tipo"
            value={filtroTipo}
            onChange={(event) => setFiltroTipo(event.target.value)}
            sx={{ minWidth: 150 }}
          >
            <MenuItem value="">Todos</MenuItem>
            {tipos.map((tipo) => (
              <MenuItem key={tipo} value={tipo}>
                {tipo}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            select
            size="small"
            label="Estación"
            value={filtroEstacion}
            onChange={(event) => setFiltroEstacion(event.target.value)}
            sx={{ minWidth: 200 }}
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

          {hayFiltros && <Button onClick={limpiarFiltros}>Limpiar</Button>}
        </Box>
      </Paper>

      {cargando ? (
        <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table sx={{ minWidth: 800 }}>
            <TableHead>
              <TableRow>
                <TableCell>Código</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell>Estación</TableCell>
                <TableCell>Batería</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>

            <TableBody>
              {filtrados.map((vehiculo) => (
                <TableRow key={vehiculo.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>
                      {vehiculo.codigo}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      ID {vehiculo.id}
                    </Typography>
                  </TableCell>

                  <TableCell>{vehiculo.tipo}</TableCell>
                  <TableCell>{vehiculo.estacion}</TableCell>
                  <TableCell>
                    <Bateria valor={vehiculo.bateria} />
                  </TableCell>
                  <TableCell>
                    <EstadoChip estado={vehiculo.estado} />
                  </TableCell>
                  <TableCell align="right">
                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'flex-end',
                        gap: 0.5,
                        flexWrap: 'wrap',
                      }}
                    >
                      <Tooltip
                        title={
                          vehiculo.estado === 'En uso'
                            ? 'Vehículo en viaje.'
                            : ''
                        }
                      >
                        <span>
                          <Button
                            size="small"
                            startIcon={<EditIcon />}
                            disabled={vehiculo.estado === 'En uso'}
                            onClick={() => abrirDialogo(vehiculo)}
                          >
                            Cambiar estado
                          </Button>
                        </span>
                      </Tooltip>
                    </Box>
                  </TableCell>
                </TableRow>
              ))}

              {filtrados.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 5 }}>
                    <Typography color="text.secondary">
                      {vehiculos.length === 0
                        ? 'No hay vehículos registrados.'
                        : 'No hay vehículos que coincidan con los filtros.'}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog
        open={Boolean(vehiculoDialogo)}
        onClose={cerrarDialogo}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>Cambiar estado del vehículo</DialogTitle>

        <DialogContent>
          {vehiculoDialogo && (
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {vehiculoDialogo.codigo} · {vehiculoDialogo.estacion}
            </Typography>
          )}

          <TextField
            select
            fullWidth
            label="Nuevo estado"
            value={nuevoEstado}
            onChange={(event) => setNuevoEstado(event.target.value)}
            sx={{ mt: 1 }}
          >
            {ESTADOS_ASIGNABLES.map((estado) => (
              <MenuItem key={estado} value={estado}>
                {estado}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            fullWidth
            multiline
            minRows={3}
            label="Motivo"
            value={motivo}
            onChange={(event) => setMotivo(event.target.value)}
            sx={{ mt: 2 }}
          />
        </DialogContent>

        <DialogActions>
          <Button onClick={cerrarDialogo} disabled={guardando}>
            Cancelar
          </Button>
          <Button
            variant="contained"
            onClick={guardarEstado}
            disabled={guardando}
          >
            {guardando ? 'Guardando…' : 'Guardar cambio'}
          </Button>
        </DialogActions>
      </Dialog>

      <VehiculoFormDialog
        abierto={formAbierto}
        vehiculo={null}
        tipos={tiposCatalogo}
        estaciones={estacionesCatalogo}
        onCerrar={cerrarFormulario}
        onGuardado={vehiculoGuardado}
      />
    </Box>
  );
}
