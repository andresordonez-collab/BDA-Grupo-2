import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
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
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import auditoriaService from '../../services/auditoriaService';
import { getErrorMessage } from '../../http-common';
import { formatearFechaHora } from '../../utils/formato.js';

const ESTADOS = ['Disponible', 'En uso', 'En mantención', 'Dado de baja'];

const COLOR_ESTADO = {
  Disponible: 'success',
  'En uso': 'info',
  'En mantención': 'warning',
  'Dado de baja': 'error',
};

function normalizarAuditoria(item) {
  const id = item.idAuditoria ?? item.id_auditoria;
  const idVehiculo = item.idVehiculo ?? item.id_vehiculo;
  const idOperador = item.idOperador ?? item.id_operador;

  return {
    id,
    idVehiculo,
    codigoVehiculo:
      item.codigoVehiculo ??
      item.codigo_vehiculo ??
      `Vehículo #${idVehiculo}`,
    idOperador,
    operador:
      item.nombreOperador ??
      item.nombre_operador ??
      item.operador ??
      `Operador #${idOperador}`,
    estadoAnterior:
      item.estadoAnterior ?? item.estado_anterior ?? '—',
    estadoNuevo: item.estadoNuevo ?? item.estado_nuevo ?? '—',
    fecha: item.fechaAuditoria ?? item.fecha_auditoria ?? null,
    motivo: item.motivo ?? '',
  };
}

function CambioEstado({ anterior, nuevo }) {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 0.75,
        flexWrap: 'wrap',
      }}
    >
      <Chip
        size="small"
        variant="outlined"
        label={anterior}
        color={COLOR_ESTADO[anterior] ?? 'default'}
      />

      <ArrowForwardIcon
        sx={{ fontSize: 16, color: 'text.secondary' }}
      />

      <Chip
        size="small"
        label={nuevo}
        color={COLOR_ESTADO[nuevo] ?? 'default'}
      />
    </Box>
  );
}

export default function AuditoriaPage() {
  const [auditorias, setAuditorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [fechaDesde, setFechaDesde] = useState('');
  const [fechaHasta, setFechaHasta] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');

    try {
      const { data } = await auditoriaService.getAll();

      setAuditorias(
        Array.isArray(data) ? data.map(normalizarAuditoria) : []
      );
    } catch (err) {
      if (err?.response?.status === 404) {
        setError(
          'Los registros de auditoría no están disponibles por el momento.'
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

  const filtradas = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    const desde = fechaDesde
      ? new Date(`${fechaDesde}T00:00:00`)
      : null;
    const hasta = fechaHasta
      ? new Date(`${fechaHasta}T23:59:59.999`)
      : null;

    return [...auditorias]
      .filter((auditoria) => {
        const coincideTexto =
          !texto ||
          auditoria.codigoVehiculo.toLowerCase().includes(texto) ||
          auditoria.operador.toLowerCase().includes(texto) ||
          auditoria.motivo.toLowerCase().includes(texto);

        const coincideEstado =
          !filtroEstado || auditoria.estadoNuevo === filtroEstado;

        const fecha = auditoria.fecha
          ? new Date(auditoria.fecha)
          : null;

        const coincideDesde =
          !desde || (fecha && fecha >= desde);

        const coincideHasta =
          !hasta || (fecha && fecha <= hasta);

        return (
          coincideTexto &&
          coincideEstado &&
          coincideDesde &&
          coincideHasta
        );
      })
      .sort((a, b) => {
        const fechaA = a.fecha ? new Date(a.fecha).getTime() : 0;
        const fechaB = b.fecha ? new Date(b.fecha).getTime() : 0;
        return fechaB - fechaA;
      });
  }, [auditorias, busqueda, filtroEstado, fechaDesde, fechaHasta]);

  const resumen = useMemo(
    () => ({
      total: auditorias.length,
      mantenimiento: auditorias.filter(
        (auditoria) => auditoria.estadoNuevo === 'En mantención'
      ).length,
      bajas: auditorias.filter(
        (auditoria) => auditoria.estadoNuevo === 'Dado de baja'
      ).length,
      operadores: new Set(
        auditorias
          .map((auditoria) => auditoria.idOperador)
          .filter((id) => id !== null && id !== undefined)
      ).size,
    }),
    [auditorias]
  );

  const hayFiltros = Boolean(
    busqueda || filtroEstado || fechaDesde || fechaHasta
  );

  const limpiarFiltros = () => {
    setBusqueda('');
    setFiltroEstado('');
    setFechaDesde('');
    setFechaHasta('');
  };

  const tarjetas = [
    { label: 'Registros', valor: resumen.total },
    { label: 'Mantenciones', valor: resumen.mantenimiento },
    { label: 'Bajas', valor: resumen.bajas },
    { label: 'Operadores', valor: resumen.operadores },
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 700 }} gutterBottom>
        Auditoría
      </Typography>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Historial de cambios de estado sensibles de los vehículos.
      </Typography>

      <Alert severity="info" sx={{ mb: 2 }}>
        Solo se auditan los cambios a “En mantención” y “Dado de baja”.
      </Alert>

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
            placeholder="Vehículo, operador o motivo"
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
            label="Estado nuevo"
            value={filtroEstado}
            onChange={(event) => setFiltroEstado(event.target.value)}
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="">Todos</MenuItem>
            {ESTADOS.map((estado) => (
              <MenuItem key={estado} value={estado}>
                {estado}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            type="date"
            size="small"
            label="Desde"
            value={fechaDesde}
            onChange={(event) => setFechaDesde(event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
            sx={{ minWidth: 160 }}
          />

          <TextField
            type="date"
            size="small"
            label="Hasta"
            value={fechaHasta}
            onChange={(event) => setFechaHasta(event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
            sx={{ minWidth: 160 }}
          />

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
          <Table sx={{ minWidth: 1000 }}>
            <TableHead>
              <TableRow>
                <TableCell>Fecha</TableCell>
                <TableCell>Vehículo</TableCell>
                <TableCell>Cambio de estado</TableCell>
                <TableCell>Operador</TableCell>
                <TableCell>Motivo</TableCell>
              </TableRow>
            </TableHead>

            <TableBody>
              {filtradas.map((auditoria) => (
                <TableRow key={auditoria.id} hover>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                    {formatearFechaHora(auditoria.fecha)}
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>
                      {auditoria.codigoVehiculo}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      ID {auditoria.idVehiculo}
                    </Typography>
                  </TableCell>

                  <TableCell>
                    <CambioEstado
                      anterior={auditoria.estadoAnterior}
                      nuevo={auditoria.estadoNuevo}
                    />
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2">
                      {auditoria.operador}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      ID {auditoria.idOperador}
                    </Typography>
                  </TableCell>

                  <TableCell sx={{ maxWidth: 360, whiteSpace: 'normal' }}>
                    {auditoria.motivo || '—'}
                  </TableCell>
                </TableRow>
              ))}

              {filtradas.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 5 }}>
                    <Typography color="text.secondary">
                      {auditorias.length === 0
                        ? 'No hay registros de auditoría.'
                        : 'No hay registros que coincidan con los filtros.'}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}