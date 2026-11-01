import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Alert, Box, Button, Dialog, DialogActions,
  DialogContent, DialogContentText, DialogTitle,
  LinearProgress, Paper, Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import billeteraService from '../../services/billeteraDemoService';
import { getErrorMessage } from '../../http-common';

const moneda = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 2,
});

function mensajeError(error) {
  return error?.isAxiosError
    ? getErrorMessage(error)
    : error?.message || 'No pudimos completar la simulación.';
}

export default function BilleteraPage() {
  const { usuario } = useOutletContext();
  const idUsuario = usuario?.idUsuario;

  const [billetera, setBilletera] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [revision, setRevision] = useState(0);
  const [confirmando, setConfirmando] = useState(false);
  const [pagando, setPagando] = useState(false);

  const deuda = Math.max(0, -Number(billetera?.saldo ?? 0));

  useEffect(() => {
    let activo = true;

    async function cargar() {
      setCargando(true);
      setError('');

      try {
        const data = await billeteraService.getMiBilletera(idUsuario);
        if (activo) {
          setBilletera(data);
        }
      } catch (err) {
        if (activo) {
          setBilletera(null);
          setError(mensajeError(err));
        }
      } finally {
        if (activo) {
          setCargando(false);
        }
      }
    }

    cargar();
    return () => {
      activo = false;
    };
  }, [idUsuario, revision]);

  async function pagarDeuda() {
    if (pagando || cargando || deuda <= 0) return;

    setPagando(true);
    setError('');
    setExito('');

    try {
      const resultado = await billeteraService.pagarDeudaSimulada(idUsuario);
      setBilletera(resultado.billetera);
      setConfirmando(false);
      setExito(
        `Pago simulado de ${moneda.format(resultado.monto)} realizado. No hubo un cobro real.`,
      );
    } catch (err) {
      setConfirmando(false);
      setError(mensajeError(err));
    } finally {
      setPagando(false);
    }
  }

  return (
    <Box sx={{ maxWidth: 760 }}>
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
            Mi billetera
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Consulta tu saldo y tus deudas pendientes.
          </Typography>
        </Box>

        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          disabled={cargando || pagando || confirmando}
          onClick={() => setRevision((valor) => valor + 1)}
        >
          Actualizar
        </Button>
      </Box>

      {cargando && <LinearProgress aria-label="Cargando billetera" />}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {exito && <Alert severity="success" sx={{ mb: 2 }}>{exito}</Alert>}

      {!cargando && billetera && (
        <>
          <Alert severity="info" sx={{ mb: 3 }}>
            Modo demostración: saldo y pagos ficticios.
            La demostración se reinicia al recargar el navegador.
          </Alert>

          <Paper
            variant="outlined"
            sx={{
              p: { xs: 3, sm: 4 },
              borderRadius: 4,
              borderColor: '#c9e3da',
              background: 'linear-gradient(135deg, #ffffff, #e8f5ef)',
            }}
          >
            <Typography color="text.secondary">
              Saldo de demostración
            </Typography>
            <Typography
              variant="h3"
              sx={{
                mt: 2,
                fontWeight: 700,
                color: billetera.saldo < 0 ? 'error.main' : 'primary.main',
                overflowWrap: 'anywhere',
              }}
            >
              {moneda.format(billetera.saldo)}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              Pesos chilenos · CLP
            </Typography>
          </Paper>

          <Paper variant="outlined" sx={{ p: 3, mt: 3, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Deuda pendiente
            </Typography>
            <Typography
              variant="h4"
              sx={{
                my: 2,
                fontWeight: 700,
                color: deuda > 0 ? 'error.main' : 'primary.main',
              }}
            >
              {moneda.format(deuda)}
            </Typography>

            {deuda === 0 && (
              <Typography color="text.secondary" sx={{ mb: 2 }}>
                No tienes deudas pendientes en esta demostración.
              </Typography>
            )}

            <Button
              variant="contained"
              disabled={pagando || deuda === 0}
              onClick={() => setConfirmando(true)}
            >
              Simular pago de deuda
            </Button>
          </Paper>
        </>
      )}

      <Dialog
        open={confirmando}
        onClose={() => {
          if (!pagando) setConfirmando(false);
        }}
        aria-labelledby="confirmar-pago-titulo"
      >
        <DialogTitle id="confirmar-pago-titulo">
          Confirmar pago simulado
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            Se simulará un aporte de {moneda.format(deuda)} para cubrir
            tu saldo negativo. No se realizará ningún cobro real.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button disabled={pagando} onClick={() => setConfirmando(false)}>
            Cancelar
          </Button>
          <Button
            variant="contained"
            disabled={pagando || deuda === 0}
            onClick={pagarDeuda}
          >
            {pagando ? 'Procesando…' : 'Confirmar pago simulado'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}