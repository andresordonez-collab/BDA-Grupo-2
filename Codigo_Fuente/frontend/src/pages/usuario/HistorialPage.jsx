import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, LinearProgress, Paper,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import viajeService from '../../services/viajeService';
import { getErrorMessage } from '../../http-common';
import { formatearFechaHora } from '../../utils/formato';

const moneda = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 2,
});

const columnas = ['Viaje', 'Vehículo', 'Origen', 'Destino', 'Inicio', 'Fin', 'Minutos', 'Costo', 'Estado'];

export default function HistorialPage() {
  const [viajes, setViajes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    async function cargar() {
      setCargando(true);
      setError('');

      try {
        const data = await viajeService.getHistorial(50, controller.signal);
        if (!controller.signal.aborted) {
          setViajes(data);
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(getErrorMessage(err));
        }
      } finally {
        if (!controller.signal.aborted) {
          setCargando(false);
        }
      }
    }

    cargar();
    return () => controller.abort();
  }, [revision]);

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
            Mi historial
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Consulta tus últimos 50 viajes y su estado.
          </Typography>
        </Box>

        <Button
          startIcon={<RefreshIcon />}
          variant="outlined"
          disabled={cargando}
          onClick={() => setRevision((valor) => valor + 1)}
        >
          Actualizar
        </Button>
      </Box>

      {cargando && <LinearProgress aria-label="Cargando historial" />}
      {error && <Alert severity="error">{error}</Alert>}

      {!cargando && !error && viajes.length === 0 && (
        <Alert severity="info">Todavía no tienes viajes registrados.</Alert>
      )}

      {!cargando && !error && viajes.length > 0 && (
        <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 3 }}>
          <Table sx={{ minWidth: 1050 }} aria-label="Historial de viajes">
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                {columnas.map((columna) => (
                  <TableCell key={columna} sx={{ fontWeight: 700 }}>
                    {columna}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>

            <TableBody>
              {viajes.map((viaje) => (
                <TableRow key={viaje.idViaje} hover>
                  <TableCell>#{viaje.idViaje}</TableCell>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      {viaje.codigoVehiculo}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {viaje.tipoVehiculo}
                    </Typography>
                  </TableCell>
                  <TableCell>{viaje.estacionOrigen}</TableCell>
                  <TableCell>{viaje.estacionDestino ?? '—'}</TableCell>
                  <TableCell>{formatearFechaHora(viaje.fechaInicio)}</TableCell>
                  <TableCell>{formatearFechaHora(viaje.fechaFin)}</TableCell>
                  <TableCell>{viaje.minutosViaje ?? '—'}</TableCell>
                  <TableCell>
                    {viaje.costo == null ? '—' : moneda.format(viaje.costo)}
                  </TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={viaje.estadoViaje}
                      color={
                        viaje.estadoViaje === 'Activo'
                          ? 'primary'
                          : viaje.estadoViaje === 'Finalizado'
                            ? 'success'
                            : 'default'
                      }
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}