import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export default function Unauthorized() {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        p: 3,
      }}
    >
      <Box sx={{ textAlign: 'center' }}>
        <Typography variant="h4" gutterBottom>
          Acceso no autorizado
        </Typography>

        <Typography color="text.secondary" sx={{ mb: 2 }}>
          No tienes permisos para acceder a esta sección.
        </Typography>

        <Button variant="contained" onClick={() => navigate('/')}>
          Volver al inicio
        </Button>
      </Box>
    </Box>
  );
}
