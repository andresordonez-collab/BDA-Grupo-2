import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Snackbar } from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';

export default function BotonSalir({
  onLogout,
  variant = 'text',
  color = 'inherit',
}) {
  const navigate = useNavigate();
  const [saliendo, setSaliendo] = useState(false);
  const [error, setError] = useState('');

  async function manejarSalida() {
    if (saliendo || !onLogout) return;

    setSaliendo(true);
    setError('');

    try {
      await onLogout();
      navigate('/', { replace: true });
    } catch {
      setError('No pudimos cerrar sesión. Intenta nuevamente.');
    } finally {
      setSaliendo(false);
    }
  }

  return (
    <>
      <Button
        variant={variant}
        color={color}
        startIcon={<LogoutIcon />}
        onClick={manejarSalida}
        disabled={saliendo || !onLogout}
      >
        {saliendo ? 'Saliendo…' : 'Salir'}
      </Button>

      <Snackbar
        open={Boolean(error)}
        autoHideDuration={6000}
        onClose={() => setError('')}
      >
        <Alert severity="error" onClose={() => setError('')}>
          {error}
        </Alert>
      </Snackbar>
    </>
  );
}