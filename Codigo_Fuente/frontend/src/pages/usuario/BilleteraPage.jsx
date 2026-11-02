import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, LinearProgress, Paper,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import billeteraService from '../../services/billeteraService';
import { getErrorMessage } from '../../http-common';
import { formatearFechaHora } from '../../utils/formato';

const moneda = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 2,
});

export default function BilleteraPage() {
  const [billetera, setBilletera] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);

  const deuda = Math.max(0, -Number(billetera?.saldo ?? 0));
  const movimientos = billetera?.movimientos ?? [];

  useEffect(() => {
    const controller = new AbortController();

    async function cargar() {
      setCargando(true);
      setError('');

      try {
        const data = await billeteraService.getMiBilletera(controller.signal);

        if (!controller.signal.aborted) {
          setBilletera(data);
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          setBilletera(null);
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
    <Box sx={{ maxWidth: 960 }}>
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
            Mi billetera
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Saldo y últimos movimientos.
          </Typography>
        </Box>

        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          disabled={cargando}
          onClick={() => setRevision((valor) => valor + 1)}
        >
          Actualizar
        </Button>
      </Box>

      {cargando && <LinearProgress aria-label="Cargando billetera" />}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {!cargando && billetera && (
        <>
          <Paper
            variant="outlined"
            sx={{
              p: { xs: 3, sm: 4 },
              borderRadius: 4,
              borderColor: '#c9e3da',
              background: 'linear-gradient(135deg, #ffffff, #e8f5ef)',
            }}
          >
            <Typography color="text.secondary">Saldo actual</Typography>
            <Typography
              variant="h3"
              sx={{
                mt: 2,
                fontWeight: 700,
                color: deuda > 0 ? 'error.main' : 'primary.main',
                overflowWrap: 'anywhere',
              }}
            >
              {moneda.format(billetera.saldo)}
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 2 }}>
              Pesos chilenos · CLP
            </Typography>
          </Paper>

          <Paper variant="outlined" sx={{ p: 3, mt: 3, borderRadius: 3 }}>
            <Typography variant="h6">Deuda pendiente</Typography>
            <Typography
              variant="h4"
              sx={{ my: 2, color: deuda > 0 ? 'error.main' : 'primary.main' }}
            >
              {moneda.format(deuda)}
            </Typography>

            {deuda > 0 ? (
              <>
                <Alert severity="info" sx={{ mb: 2 }}>
                  Pago de deudas no disponible.
                </Alert>
                <Button variant="contained" disabled>Pagar deuda</Button>
              </>
            ) : (
              <Typography color="text.secondary">
                No tienes deudas pendientes.
              </Typography>
            )}
          </Paper>

          <Typography variant="h6" sx={{ mt: 4, mb: 2 }}>
            Últimos movimientos
          </Typography>

          {movimientos.length === 0 ? (
            <Alert severity="info">Todavía no tienes movimientos registrados.</Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 3 }}>
              <Table aria-label="Movimientos de mi billetera" sx={{ minWidth: 600 }}>
                <TableHead sx={{ bgcolor: '#eaf5ef' }}>
                  <TableRow>
                    <TableCell>Fecha</TableCell>
                    <TableCell>Tipo</TableCell>
                    <TableCell align="right">Monto</TableCell>
                    <TableCell align="right">Viaje</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {movimientos.map((movimiento) => (
                    <TableRow key={movimiento.idMovimiento} hover>
                      <TableCell>{formatearFechaHora(movimiento.fechaMovimiento)}</TableCell>
                      <TableCell>{movimiento.tipoMovimiento?.replaceAll('_', ' ')}</TableCell>
                      <TableCell align="right">{moneda.format(movimiento.monto)}</TableCell>
                      <TableCell align="right">{movimiento.idViaje ?? '—'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </>
      )}
    </Box>
  );
}