import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import LoginIcon from '@mui/icons-material/Login';
import authService from '../services/authService';

export default function Login({ onLogin }) {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  async function manejarIngreso(event) {
    event.preventDefault();
    if (cargando) return;

    setError('');
    setCargando(true);

    try {
      const usuario = await authService.login(email.trim(), password);
      const rol = typeof usuario?.rol === 'string'
        ? usuario.rol.toLowerCase()
        : '';

      if (!usuario?.idUsuario || !['usuario', 'operador'].includes(rol)) {
        setError('No pudimos identificar tu perfil. Intenta nuevamente.');
        return;
      }

      onLogin?.(usuario);
      navigate(rol === 'operador' ? '/operador/flota' : '/usuario', {
        replace: true,
      });
    } catch (err) {
      setError(err.response?.status === 401
        ? 'El correo o la contraseña no son correctos.'
        : 'No pudimos iniciar sesión. Intenta nuevamente en unos momentos.');
    } finally {
      setCargando(false);
    }
  }

  return (
    <Box
      component="main"
      sx={{
        minHeight: '100dvh',
        display: 'grid',
        placeItems: 'center',
        p: { xs: 2, sm: 3 },
        bgcolor: 'background.default',
      }}
    >
      <Paper
        variant="outlined"
        sx={{ width: '100%', maxWidth: 440, p: { xs: 3, sm: 4 }, borderRadius: 3 }}
      >
        <Stack spacing={3}>
          <Box>
            <Stack direction="row" spacing={1} sx={{ mb: 3, alignItems: 'center' }}>
              <DirectionsBikeIcon color="primary" fontSize="large" />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Bici & Scoot
              </Typography>
            </Stack>

            <Typography component="h1" variant="h4" sx={{ fontWeight: 700 }}>
              Bienvenido
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              Acceso a tu cuenta.
            </Typography>
          </Box>

          <Box component="form" onSubmit={manejarIngreso} aria-busy={cargando}>
            <Stack spacing={2.5}>
              {error && <Alert severity="error">{error}</Alert>}

              <TextField
                id="email"
                label="Correo electrónico"
                type="email"
                autoComplete="username"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                disabled={cargando}
                required
                fullWidth
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <EmailOutlinedIcon fontSize="small" />
                      </InputAdornment>
                    ),
                  },
                }}
              />

              <TextField
                id="password"
                label="Contraseña"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                disabled={cargando}
                required
                fullWidth
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <LockOutlinedIcon fontSize="small" />
                      </InputAdornment>
                    ),
                  },
                }}
              />

              <Button
                type="submit"
                variant="contained"
                size="large"
                startIcon={<LoginIcon />}
                disabled={cargando}
                fullWidth
              >
                {cargando ? 'Ingresando…' : 'Ingresar'}
              </Button>
            </Stack>
          </Box>

          <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'center' }}>
            Movilidad compartida · BDA Grupo 2
          </Typography>
        </Stack>
      </Paper>
    </Box>
  );
}
