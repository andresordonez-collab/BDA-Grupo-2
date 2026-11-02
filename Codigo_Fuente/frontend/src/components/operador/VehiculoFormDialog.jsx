import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  TextField,
  Typography,
} from '@mui/material';
import vehiculoService from '../../services/vehiculoService';
import { getErrorMessage } from '../../http-common';

const VALORES_INICIALES = {
  idTipoVehiculo: '',
  idEstacion: '',
  codigoVehiculo: '',
  nivelBateria: '',
};

export default function VehiculoFormDialog({
  abierto,
  vehiculo,
  tipos,
  estaciones,
  onCerrar,
  onGuardado,
}) {
  const [form, setForm] = useState(VALORES_INICIALES);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!abierto) return;

    setForm({
      idTipoVehiculo:
        vehiculo?.idTipoVehiculo !== null &&
        vehiculo?.idTipoVehiculo !== undefined
          ? String(vehiculo.idTipoVehiculo)
          : '',
      idEstacion:
        vehiculo?.idEstacion !== null &&
        vehiculo?.idEstacion !== undefined
          ? String(vehiculo.idEstacion)
          : '',
      codigoVehiculo: vehiculo?.codigoVehiculo ?? '',
      nivelBateria:
        vehiculo?.nivelBateria !== null &&
        vehiculo?.nivelBateria !== undefined
          ? String(vehiculo.nivelBateria)
          : '',
    });
    setError('');
  }, [abierto, vehiculo]);

  const nombreTipo = (idTipo) =>
    tipos.find((tipo) => String(tipo.idTipoVehiculo) === String(idTipo))
      ?.nombreTipoVehiculo ?? '';

  const esBicicleta = nombreTipo(form.idTipoVehiculo) === 'Bicicleta';
  const esScooter = nombreTipo(form.idTipoVehiculo) === 'Scooter';

  const cambiar = (event) => {
    const { name, value } = event.target;

    setForm((actual) => {
      const siguiente = { ...actual, [name]: value };

      // Una bicicleta no tiene batería: al elegir ese tipo se limpia el campo.
      if (name === 'idTipoVehiculo' && nombreTipo(value) === 'Bicicleta') {
        siguiente.nivelBateria = '';
      }

      return siguiente;
    });
  };

  const guardar = async () => {
    if (!form.codigoVehiculo.trim()) {
      setError('El código del vehículo es obligatorio.');
      return;
    }

    if (!form.idTipoVehiculo) {
      setError('Selecciona un tipo de vehículo.');
      return;
    }

    if (esScooter && form.nivelBateria === '') {
      setError('Los scooters requieren un nivel de batería entre 0 y 100.');
      return;
    }

    if (
      form.nivelBateria !== '' &&
      (Number(form.nivelBateria) < 0 ||
        Number(form.nivelBateria) > 100)
    ) {
      setError('La batería debe estar entre 0 y 100.');
      return;
    }

    setGuardando(true);
    setError('');

    try {
      const payload = {
        idTipoVehiculo: Number(form.idTipoVehiculo),
        idEstacion: form.idEstacion
          ? Number(form.idEstacion)
          : null,
        codigoVehiculo: form.codigoVehiculo.trim(),
        nivelBateria:
          esBicicleta || form.nivelBateria === ''
            ? null
            : Number(form.nivelBateria),
        estadoVehiculo:
          vehiculo?.estadoVehiculo ?? 'Disponible',
      };

      if (vehiculo) {
        await vehiculoService.update(vehiculo.id, payload);
      } else {
        await vehiculoService.create(payload);
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
        {vehiculo ? 'Editar vehículo' : 'Nuevo vehículo'}
      </DialogTitle>

      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <TextField
          fullWidth
          label="Código del vehículo"
          name="codigoVehiculo"
          value={form.codigoVehiculo}
          onChange={cambiar}
          required
          sx={{ mt: 1 }}
        />

        <TextField
          select
          fullWidth
          label="Tipo de vehículo"
          name="idTipoVehiculo"
          value={form.idTipoVehiculo}
          onChange={cambiar}
          required
          sx={{ mt: 2 }}
        >
          {tipos.map((tipo) => (
            <MenuItem
              key={tipo.idTipoVehiculo}
              value={String(tipo.idTipoVehiculo)}
            >
              {tipo.nombreTipoVehiculo}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          select
          fullWidth
          label="Estación"
          name="idEstacion"
          value={form.idEstacion}
          onChange={cambiar}
          sx={{ mt: 2 }}
        >
          <MenuItem value="">Sin estación</MenuItem>
          {estaciones.map((estacion) => (
            <MenuItem
              key={estacion.idEstacion}
              value={String(estacion.idEstacion)}
            >
              {estacion.nombreEstacion}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          fullWidth
          type="number"
          label="Batería (%)"
          name="nivelBateria"
          value={form.nivelBateria}
          onChange={cambiar}
          disabled={esBicicleta}
          required={esScooter}
          helperText={
            esBicicleta
              ? 'Las bicicletas no tienen batería.'
              : 'Obligatoria para scooters, entre 0 y 100.'
          }
          slotProps={{
            htmlInput: {
              min: 0,
              max: 100,
              step: 1,
            },
          }}
          sx={{ mt: 2 }}
        />

        {vehiculo && (
          <Typography
            variant="caption"
            color="text.secondary"
            sx={{ display: 'block', mt: 2 }}
          >
            El estado se cambia con el botón “Cambiar estado” para conservar
            la auditoría.
          </Typography>
        )}
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
