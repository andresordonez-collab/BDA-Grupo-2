import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, LinearProgress, Paper, Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import tarifaService from '../../services/tarifaUsuarioService';
import { getErrorMessage } from '../../http-common';
import { formatearFechaHora } from '../../utils/formato';

const moneda = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 2,
});

export default function TarifasPage() {
  const [tarifas, setTarifas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    async function cargar() {
      setCargando(true);
      setError('');

      try {
        const data = await tarifaService.getActivas(controller.signal);
        if (!controller.signal.aborted) {
          setTarifas(data);
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
            Tarifas
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Desbloqueo y precio por minuto por vehículo.
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

      {cargando && <LinearProgress aria-label="Cargando tarifas" />}
      {error && <Alert severity="error">{error}</Alert>}

      {!cargando && !error && tarifas.length === 0 && (
        <Alert severity="info">
          No hay tarifas activas disponibles.
        </Alert>
      )}

      {!cargando && !error && (
        <Box sx={{
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
          gap: 3,
        }}>
          {tarifas.map((tarifa) => (
            <Paper
              key={tarifa.idTarifa}
              variant="outlined"
              sx={{
                p: 3,
                borderRadius: 3,
                borderTop: '4px solid',
                borderTopColor: 'primary.main',
              }}
            >
              <Typography variant="h5" sx={{ fontWeight: 700 }}>
                {tarifa.nombreTipoVehiculo}
              </Typography>

              <Box sx={{
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
                gap: 3,
                my: 3,
              }}>
                <Box>
                  <Typography color="text.secondary">
                    Desbloqueo
                  </Typography>
                  <Typography variant="h4" color="primary" sx={{ fontWeight: 700 }}>
                    {moneda.format(tarifa.valorDesbloqueo)}
                  </Typography>
                </Box>
                <Box>
                  <Typography color="text.secondary">
                    Por minuto
                  </Typography>
                  <Typography variant="h4" color="primary" sx={{ fontWeight: 700 }}>
                    {moneda.format(tarifa.valorMinuto)}
                  </Typography>
                </Box>
              </Box>

              <Typography variant="body2" color="text.secondary">
                Vigencia desde: {formatearFechaHora(tarifa.vigenteDesde)}
              </Typography>
            </Paper>
          ))}
        </Box>
      )}
    </Box>
  );
}