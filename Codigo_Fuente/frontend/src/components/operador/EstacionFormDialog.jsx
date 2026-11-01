import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  TextField,
} from '@mui/material';
import estacionService from '../../services/estacionService';
import { getErrorMessage } from '../../http-common';

const VALORES_INICIALES = {
  nombreEstacion: '',
  direccion: '',
  latitud: '-33.450000',
  longitud: '-70.650000',
  capacidad: '10',
  estacionActiva: true,
};

export default function EstacionFormDialog({
  abierto,
  estacion,
  onCerrar,
  onGuardado,
}) {
  const [form, setForm] = useState(VALORES_INICIALES);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!abierto) return;

    setForm({
      nombreEstacion: estacion?.nombre ?? '',
      direccion: estacion?.direccion ?? '',
      latitud:
        estacion?.latitud !== null && estacion?.latitud !== undefined
          ? String(estacion.latitud)
          : '-33.450000',
      longitud:
        estacion?.longitud !== null && estacion?.longitud !== undefined
          ? String(estacion.longitud)
          : '-70.650000',
      capacidad:
        estacion?.capacidad !== null && estacion?.capacidad !== undefined
          ? String(estacion.capacidad)
          : '10',
      estacionActiva: estacion?.activa ?? true,
    });
    setError('');
  }, [abierto, estacion]);

  const cambiar = (event) => {
    const { name, value, type, checked } = event.target;

    setForm((actual) => ({
      ...actual,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const guardar = async () => {
    if (!form.nombreEstacion.trim()) {
      setError('El nombre de la estación es obligatorio.');
      return;
    }

    if (!form.direccion.trim()) {
      setError('La dirección es obligatoria.');
      return;
    }

    if (!Number.isFinite(Number(form.latitud))) {
      setError('La latitud debe ser un número.');
      return;
    }

    if (!Number.isFinite(Number(form.longitud))) {
      setError('La longitud debe ser un número.');
      return;
    }

    if (!Number.isInteger(Number(form.capacidad)) || Number(form.capacidad) <= 0) {
      setError('La capacidad debe ser un entero mayor que cero.');
      return;
    }

    setGuardando(true);
    setError('');

    try {
      const payload = {
        nombreEstacion: form.nombreEstacion.trim(),
        direccion: form.direccion.trim(),
        latitud: Number(form.latitud),
        longitud: Number(form.longitud),
        capacidad: Number(form.capacidad),
        estacionActiva: Boolean(form.estacionActiva),
      };

      if (estacion) {
        await estacionService.update(estacion.id, payload);
      } else {
        await estacionService.create(payload);
      }

      onGuardado?.();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Dialog
      open={abierto}
      onClose={guardando ? undefined : onCerrar}
      fullWidth
      maxWidth="sm"
    >
      <DialogTitle>
        {estacion ? 'Editar estación' : 'Nueva estación'}
      </DialogTitle>

      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <TextField
          fullWidth
          label="Nombre de la estación"
          name="nombreEstacion"
          value={form.nombreEstacion}
          onChange={cambiar}
          required
          sx={{ mt: 1 }}
        />

        <TextField
          fullWidth
          label="Dirección"
          name="direccion"
          value={form.direccion}
          onChange={cambiar}
          required
          sx={{ mt: 2 }}
        />

        <TextField
          fullWidth
          type="number"
          label="Latitud"
          name="latitud"
          value={form.latitud}
          onChange={cambiar}
          slotProps={{ htmlInput: { step: 'any' } }}
          sx={{ mt: 2 }}
        />

        <TextField
          fullWidth
          type="number"
          label="Longitud"
          name="longitud"
          value={form.longitud}
          onChange={cambiar}
          slotProps={{ htmlInput: { step: 'any' } }}
          sx={{ mt: 2 }}
        />

        <TextField
          fullWidth
          type="number"
          label="Capacidad"
          name="capacidad"
          value={form.capacidad}
          onChange={cambiar}
          required
          slotProps={{ htmlInput: { min: 1, step: 1 } }}
          sx={{ mt: 2 }}
        />

        <FormControlLabel
          sx={{ mt: 2 }}
          control={
            <Checkbox
              name="estacionActiva"
              checked={Boolean(form.estacionActiva)}
              onChange={cambiar}
            />
          }
          label="Estación activa"
        />
      </DialogContent>

      <DialogActions>
        <Button onClick={onCerrar} disabled={guardando}>
          Cancelar
        </Button>
        <Button
          variant="contained"
          onClick={guardar}
          disabled={guardando}
        >
          {guardando ? 'Guardando…' : 'Guardar'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
