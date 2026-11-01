import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Chip, LinearProgress,
  MenuItem, Paper, Stack, TextField, Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import viajeService from '../../services/viajeService';
import estacionService from '../../services/estacionService';
import { getErrorMessage } from '../../http-common';
import { formatearFechaHora } from '../../utils/formato';

export default function MiViajePage() {
  const [viajeActivo, setViajeActivo] = useState(null);
  const [estaciones, setEstaciones] = useState([]);
  const [idVehiculo, setIdVehiculo] = useState('');
  const [idDestino, setIdDestino] = useState('');
  const [cargando, setCargando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [errorCarga, setErrorCarga] = useState('');
  const [errorAccion, setErrorAccion] = useState('');
  const [exito, setExito] = useState('');
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    async function cargar() {
      setCargando(true);
      setErrorCarga('');

      try {
        const [viajes, respuestaEstaciones] = await Promise.all([
          viajeService.getHistorial(50, controller.signal),
          estacionService.getAll(controller.signal),
        ]);

        if (controller.signal.aborted) return;

        setViajeActivo(
          viajes.find((viaje) => viaje.estadoViaje === 'Activo') ?? null,
        );
        setEstaciones(
          respuestaEstaciones.data.filter(
            (estacion) => estacion.estacionActiva,
          ),
        );
      } catch (err) {
        if (!controller.signal.aborted) {
          setErrorCarga(getErrorMessage(err));
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

  async function manejarViaje(event) {
    event.preventDefault();
    if (enviando || cargando || errorCarga) return;

    const identificador = Number(viajeActivo ? idDestino : idVehiculo);

    if (!Number.isSafeInteger(identificador) || identificador <= 0) {
      setErrorAccion(
        viajeActivo
          ? 'Selecciona una estación de destino.'
          : 'Ingresa un identificador de vehículo entero y positivo.',
      );
      return;
    }

    setEnviando(true);
    setErrorAccion('');
    setExito('');

    try {
      if (viajeActivo) {
        await viajeService.finalizar(viajeActivo.idViaje, identificador);
        setExito(`El viaje #${viajeActivo.idViaje} finalizó correctamente.`);
      } else {
        const resultado = await viajeService.iniciar(identificador);
        setExito(`El viaje #${resultado.idViaje} comenzó correctamente.`);
      }

      setIdVehiculo('');
      setIdDestino('');
    } catch (err) {
      setErrorAccion(getErrorMessage(err));
    } finally {
      // Volvemos a consultar el estado real antes de permitir otra operación.
      setCargando(true);
      setEnviando(false);
      setRevision((valor) => valor + 1);
    }
  }

  return (
    <Box sx={{ maxWidth: 800 }}>
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
            Mi viaje
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Comienza tu recorrido o registra tu estación de llegada.
          </Typography>
        </Box>

        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          disabled={cargando || enviando}
          onClick={() => {
            setErrorAccion('');
            setExito('');
            setRevision((valor) => valor + 1);
          }}
        >
          Actualizar
        </Button>
      </Box>

      <Stack spacing={2}>
        {exito && <Alert severity="success">{exito}</Alert>}
        {errorCarga && <Alert severity="error">{errorCarga}</Alert>}
        {errorAccion && <Alert severity="error">{errorAccion}</Alert>}
        {cargando && <LinearProgress aria-label="Consultando tu viaje" />}

        {!cargando && !errorCarga && (
          <Paper variant="outlined" sx={{ p: 3, borderRadius: 3 }}>
            <Box component="form" onSubmit={manejarViaje} aria-busy={enviando}>
              <Stack spacing={3}>
                {viajeActivo ? (
                  <>
                    <Box>
                      <Chip label="Viaje en curso" color="primary" />
                      <Typography variant="h5" sx={{ mt: 2, fontWeight: 700 }}>
                        Viaje #{viajeActivo.idViaje}
                      </Typography>
                      <Typography sx={{ mt: 1 }}>
                        {viajeActivo.tipoVehiculo}: {viajeActivo.codigoVehiculo}
                      </Typography>
                      <Typography color="text.secondary">
                        Origen: {viajeActivo.estacionOrigen}
                      </Typography>
                      <Typography color="text.secondary">
                        Inicio: {formatearFechaHora(viajeActivo.fechaInicio)}
                      </Typography>
                    </Box>

                    <TextField
                      select
                      label="Estación de destino"
                      value={idDestino}
                      onChange={(event) => setIdDestino(event.target.value)}
                      disabled={enviando || estaciones.length === 0}
                      required
                      fullWidth
                    >
                      {estaciones.map((estacion) => (
                        <MenuItem
                          key={estacion.idEstacion}
                          value={String(estacion.idEstacion)}
                        >
                          {estacion.nombreEstacion}
                        </MenuItem>
                      ))}
                    </TextField>

                    {estaciones.length === 0 && (
                      <Alert severity="info">
                        No hay estaciones activas para finalizar el viaje.
                      </Alert>
                    )}
                  </>
                ) : (
                  <>
                    <Typography variant="h5" sx={{ fontWeight: 700 }}>
                      Comienza un nuevo recorrido
                    </Typography>
                    <TextField
                      label="ID del vehículo"
                      type="number"
                      value={idVehiculo}
                      onChange={(event) => setIdVehiculo(event.target.value)}
                      helperText="Ingresa el identificador del vehículo que utilizarás."
                      slotProps={{ htmlInput: { min: 1, step: 1 } }}
                      disabled={enviando}
                      required
                      fullWidth
                    />
                  </>
                )}

                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={
                    enviando ||
                    (viajeActivo ? !idDestino : !idVehiculo.trim())
                  }
                >
                  {enviando
                    ? 'Procesando…'
                    : viajeActivo
                      ? 'Finalizar viaje'
                      : 'Iniciar viaje'}
                </Button>
              </Stack>
            </Box>
          </Paper>
        )}
      </Stack>
    </Box>
  );
}