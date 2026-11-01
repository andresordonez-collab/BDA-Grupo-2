import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Box, Button, Typography } from '@mui/material';
import { useAuth } from '../auth/AuthContext';

export default function Unauthorized() {
  const navigate = useNavigate();
  const { usuario } = useAuth();

  useEffect(() => {
    if (usuario) {
      navigate('/', { replace: true });
    }
  }, [usuario, navigate]);

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        p: 3,
      }}
    >
      <Box sx={{ textAlign: 'center', maxWidth: 460 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          No tienes permiso para acceder a esta sección.
        </Alert>
        <Typography color="text.secondary" sx={{ mb: 2 }}>
          Tu sesión está iniciada con un rol distinto al requerido.
        </Typography>
        <Button variant="contained" onClick={() => navigate('/')}>
          Volver al inicio
        </Button>
      </Box>
    </Box>
  );
}
