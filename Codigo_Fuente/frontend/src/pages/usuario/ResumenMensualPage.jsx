import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, LinearProgress, Paper, TextField, Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import viajeService from '../../services/viajeService';
import { getErrorMessage } from '../../http-common';

const moneda = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 2,
});

export default function ResumenMensualPage() {
  const [periodo, setPeriodo] = useState(() => {
    const hoy = new Date();
    return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}`;
  });
  const [resumen, setResumen] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    const [anio, mes] = periodo.split('-').map(Number);

    async function cargar() {
      setResumen(null);
      setError('');

      if (
        !/^\d{4}-\d{2}$/.test(periodo) ||
        anio < 2000 || anio > 2100 || mes < 1 || mes > 12
      ) {
        setError('Selecciona un mes válido entre los años 2000 y 2100.');
        setCargando(false);
        return;
      }

      setCargando(true);

      try {
        const data = await viajeService.getResumenMensual(
          anio, mes, controller.signal,
        );
        if (!controller.signal.aborted) {
          setResumen(data);
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
  }, [periodo, revision]);

  const tarjetas = resumen ? [
    { titulo: 'Viajes finalizados', valor: resumen.cantidadViajes },
    { titulo: 'Minutos recorridos', valor: resumen.minutosTotales },
    { titulo: 'Gasto total', valor: moneda.format(resumen.gastoTotal) },
  ] : [];

  return (
    <Box>
      <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
        Mi resumen mensual
      </Typography>
      <Typography color="text.secondary" sx={{ mt: 1, mb: 3 }}>
        Revisa los viajes finalizados que comenzaste durante el mes seleccionado.
      </Typography>

      <Paper
        variant="outlined"
        sx={{
          p: 2, mb: 3, borderRadius: 3,
          display: 'flex', gap: 2, flexWrap: 'wrap',
          alignItems: 'center',
        }}
      >
        <TextField
          label="Mes de consulta"
          type="month"
          value={periodo}
          onChange={(event) => setPeriodo(event.target.value)}
          slotProps={{
            inputLabel: { shrink: true },
            htmlInput: { min: '2000-01', max: '2100-12' },
          }}
        />
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          disabled={cargando || !periodo}
          onClick={() => setRevision((valor) => valor + 1)}
        >
          Actualizar
        </Button>
      </Paper>

      {cargando && <LinearProgress aria-label="Cargando resumen mensual" />}
      {error && <Alert severity="error">{error}</Alert>}

      {!cargando && !error && resumen && (
        <Box sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
          gap: 2,
        }}>
          {tarjetas.map((tarjeta) => (
            <Paper
              key={tarjeta.titulo}
              variant="outlined"
              sx={{
                p: 3, borderRadius: 3,
                borderTop: '4px solid',
                borderTopColor: 'primary.main',
              }}
            >
              <Typography color="text.secondary">
                {tarjeta.titulo}
              </Typography>
              <Typography
                variant="h4"
                sx={{ mt: 2, fontWeight: 700, color: 'primary.main' }}
              >
                {tarjeta.valor}
              </Typography>
            </Paper>
          ))}
        </Box>
      )}

      {!cargando && !error && resumen?.cantidadViajes === 0 && (
        <Alert severity="info" sx={{ mt: 3 }}>
          No tienes viajes finalizados para este período.
        </Alert>
      )}
    </Box>
  );
}