import { AppBar, Avatar, Box, Button, Toolbar, Typography } from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';

export default function AppHeader({ role, usuario, onLogout }) {
  const nombre = usuario?.nombreUsuario ?? 'Operador';
  const inicial = nombre.charAt(0).toUpperCase();

  return (
    <AppBar
      position="fixed"
      sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
    >
      <Toolbar sx={{ gap: 2 }}>
        <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
          Panel de operación
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Avatar
            sx={{
              width: 32,
              height: 32,
              bgcolor: 'secondary.main',
              color: 'secondary.contrastText',
              fontSize: 14,
              fontWeight: 700,
            }}
          >
            {inicial}
          </Avatar>

          <Box sx={{ display: { xs: 'none', sm: 'block' }, textAlign: 'right' }}>
            <Typography variant="body2" sx={{ lineHeight: 1.2 }}>
              {nombre}
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.8 }}>
              {role}
            </Typography>
          </Box>
        </Box>

        <Button
          color="inherit"
          startIcon={<LogoutIcon />}
          onClick={() => onLogout?.()}
          disabled={!onLogout}
        >
          Salir
        </Button>
      </Toolbar>
    </AppBar>
  );
}
