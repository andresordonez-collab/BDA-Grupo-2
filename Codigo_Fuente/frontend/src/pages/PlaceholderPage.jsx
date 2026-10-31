import { Box, Button, Paper, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';

export default function PlaceholderPage({ title, description }) {
  const navigate = useNavigate();

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 700 }} gutterBottom>
        {title}
      </Typography>

      <Paper variant="outlined" sx={{ p: 5, textAlign: 'center' }}>
        <Typography color="text.secondary">
          {description ?? 'Este módulo se construirá en el siguiente paso.'}
        </Typography>

        <Button
          variant="outlined"
          sx={{ mt: 2 }}
          onClick={() => navigate('/operador/flota')}
        >
          Volver a flota
        </Button>
      </Paper>
    </Box>
  );
}
