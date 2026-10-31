import { Box, Toolbar } from '@mui/material';
import { Outlet } from 'react-router-dom';
import AppHeader from './AppHeader';
import Sidebar from './Sidebar';
import { DRAWER_WIDTH } from './menuItems';

export default function Layout({ role, usuario, onLogout }) {
  return (
    <Box
      sx={{
        display: 'flex',
        minHeight: '100vh',
        bgcolor: 'background.default',
      }}
    >
      <AppHeader role={role} usuario={usuario} onLogout={onLogout} />
      <Sidebar role={role} />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          width: `calc(100% - ${DRAWER_WIDTH}px)`,
          p: 3,
        }}
      >
        <Toolbar />

        <Box sx={{ maxWidth: 1400, mx: 'auto' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
