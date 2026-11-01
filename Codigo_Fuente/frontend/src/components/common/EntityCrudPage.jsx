import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
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
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { getErrorMessage } from '../../http-common';

function CampoFormulario({ field, form, onChange }) {
  const valor = form[field.name] ?? '';

  const comunes = {
    fullWidth: true,
    name: field.name,
    label: field.label,
    value: valor,
    onChange: (event) => onChange(event, field),
    required: field.required,
    helperText: field.helperText,
    sx: { mt: 1.5 },
  };

  if (field.type === 'checkbox') {
    return (
      <FormControlLabel
        sx={{ mt: 1 }}
        control={
          <Checkbox
            name={field.name}
            checked={Boolean(form[field.name])}
            onChange={(event) => onChange(event, field)}
          />
        }
        label={field.label}
      />
    );
  }

  if (field.type === 'select') {
    return (
      <TextField {...comunes} select>
        {(field.options ?? []).map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </TextField>
    );
  }

  if (field.type === 'number') {
    return (
      <TextField
        {...comunes}
        type="number"
        slotProps={{
          htmlInput: {
            min: field.min,
            step: field.step ?? 'any',
          },
        }}
      />
    );
  }

  if (field.type === 'datetime-local') {
    return (
      <TextField
        {...comunes}
        type="datetime-local"
        slotProps={{ inputLabel: { shrink: true } }}
      />
    );
  }

  if (field.type === 'password') {
    return (
      <TextField
        {...comunes}
        type="password"
        autoComplete="new-password"
      />
    );
  }

  return <TextField {...comunes} type={field.type ?? 'text'} />;
}

export default function EntityCrudPage({
  title,
  description,
  entityName = 'registro',
  createLabel = 'Nuevo registro',
  service,
  idField,
  columns = [],
  fields = [],
  initialValues = {},
  toForm = (item) => ({ ...item }),
  toPayload = (form) => form,
  validate,
  getLabel,
  canDelete = true,
}) {
  const [items, setItems] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');

  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [editando, setEditando] = useState(null);
  const [form, setForm] = useState(initialValues);
  const [errorFormulario, setErrorFormulario] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [aEliminar, setAEliminar] = useState(null);
  const [eliminando, setEliminando] = useState(false);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError('');

    try {
      const { data } = await service.getAll();
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setCargando(false);
    }
  }, [service]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const abrirCrear = () => {
    setEditando(null);
    setForm({ ...initialValues });
    setErrorFormulario('');
    setDialogoAbierto(true);
  };

  const abrirEditar = (item) => {
    setEditando(item);
    setForm(toForm(item));
    setErrorFormulario('');
    setDialogoAbierto(true);
  };

  const cerrarDialogo = () => {
    if (!guardando) {
      setDialogoAbierto(false);
    }
  };

  const cambiarCampo = (event) => {
    const { name, value, type, checked } = event.target;

    setForm((actual) => ({
      ...actual,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const guardar = async () => {
    const mensaje = validate?.(form, Boolean(editando)) ?? '';

    if (mensaje) {
      setErrorFormulario(mensaje);
      return;
    }

    setGuardando(true);
    setErrorFormulario('');

    try {
      const payload = toPayload(form, Boolean(editando));

      if (editando) {
        await service.update(editando[idField], payload);
        setExito(`${entityName} actualizado correctamente.`);
      } else {
        await service.create(payload);
        setExito(`${entityName} creado correctamente.`);
      }

      setDialogoAbierto(false);
      await cargar();
    } catch (err) {
      setErrorFormulario(getErrorMessage(err));
    } finally {
      setGuardando(false);
    }
  };

  const eliminar = async () => {
    if (!aEliminar) return;

    setEliminando(true);
    setError('');

    try {
      await service.remove(aEliminar[idField]);
      setExito(`${entityName} eliminado correctamente.`);
      setAEliminar(null);
      await cargar();
    } catch (err) {
      setError(getErrorMessage(err));
      setAEliminar(null);
    } finally {
      setEliminando(false);
    }
  };

  const camposVisibles = fields.filter((field) =>
    editando
      ? field.showOnEdit !== false
      : field.showOnCreate !== false
  );

  const etiqueta =
    getLabel?.(aEliminar) ?? aEliminar?.[idField] ?? '';

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
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            {title}
          </Typography>
          <Typography color="text.secondary">
            {description}
          </Typography>
        </Box>

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={abrirCrear}
        >
          {createLabel}
        </Button>
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

      {cargando ? (
        <Box sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table sx={{ minWidth: 700 }}>
            <TableHead>
              <TableRow>
                {columns.map((columna) => (
                  <TableCell
                    key={columna.field}
                    align={columna.align ?? 'left'}
                  >
                    {columna.label}
                  </TableCell>
                ))}
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>

            <TableBody>
              {items.map((item) => (
                <TableRow key={item[idField]} hover>
                  {columns.map((columna) => (
                    <TableCell
                      key={columna.field}
                      align={columna.align ?? 'left'}
                    >
                      {columna.render
                        ? columna.render(item)
                        : item[columna.field] ?? '—'}
                    </TableCell>
                  ))}

                  <TableCell align="right">
                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'flex-end',
                        gap: 0.5,
                        flexWrap: 'wrap',
                      }}
                    >
                      <Button
                        size="small"
                        startIcon={<EditIcon />}
                        onClick={() => abrirEditar(item)}
                      >
                        Editar
                      </Button>

                      {canDelete && (
                        <Button
                          size="small"
                          color="error"
                          startIcon={<DeleteIcon />}
                          onClick={() => setAEliminar(item)}
                        >
                          Eliminar
                        </Button>
                      )}
                    </Box>
                  </TableCell>
                </TableRow>
              ))}

              {items.length === 0 && (
                <TableRow>
                  <TableCell
                    colSpan={columns.length + 1}
                    align="center"
                    sx={{ py: 5 }}
                  >
                    <Typography color="text.secondary">
                      No hay registros.
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog
        open={dialogoAbierto}
        onClose={cerrarDialogo}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>
          {editando
            ? `Editar ${entityName}`
            : `Nuevo ${entityName}`}
        </DialogTitle>

        <DialogContent>
          {errorFormulario && (
            <Alert severity="error" sx={{ mt: 1 }}>
              {errorFormulario}
            </Alert>
          )}

          {camposVisibles.map((field) => (
            <CampoFormulario
              key={field.name}
              field={field}
              form={form}
              onChange={cambiarCampo}
            />
          ))}
        </DialogContent>

        <DialogActions>
          <Button onClick={cerrarDialogo} disabled={guardando}>
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

      <Dialog
        open={Boolean(aEliminar)}
        onClose={eliminando ? undefined : () => setAEliminar(null)}
      >
        <DialogTitle>Eliminar {entityName}</DialogTitle>

        <DialogContent>
          <Typography>
            ¿Seguro que quieres eliminar {etiqueta}?
          </Typography>
        </DialogContent>

        <DialogActions>
          <Button
            onClick={() => setAEliminar(null)}
            disabled={eliminando}
          >
            Cancelar
          </Button>
          <Button
            color="error"
            variant="contained"
            onClick={eliminar}
            disabled={eliminando}
          >
            {eliminando ? 'Eliminando…' : 'Eliminar'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
