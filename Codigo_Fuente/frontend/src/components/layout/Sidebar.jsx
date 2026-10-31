import { useLocation, useNavigate } from 'react-router-dom';
import {
  Box,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import { DRAWER_WIDTH, MENU_BY_ROLE } from './menuItems';

export default function Sidebar({ role }) {
  const navigate = useNavigate();
  const location = useLocation();
  const items = MENU_BY_ROLE[role] ?? [];

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: DRAWER_WIDTH,
          boxSizing: 'border-box',
          display: 'flex',
          flexDirection: 'column',
          borderRight: '1px solid',
          borderColor: 'divider',
        },
      }}
    >
      <Toolbar sx={{ px: 2 }}>
        <Typography variant="h6" noWrap sx={{ fontWeight: 700 }}>
          Bici & Scoot
        </Typography>
      </Toolbar>

      <Divider />

      <List sx={{ px: 1, py: 1 }}>
        {items.map((item) => {
          const Icono = item.icon;

          return (
            <ListItemButton
              key={item.path}
              selected={location.pathname === item.path}
              onClick={() => navigate(item.path)}
              sx={{ borderRadius: 2, mb: 0.5 }}
            >
              {Icono && (
                <ListItemIcon sx={{ minWidth: 38 }}>
                  <Icono fontSize="small" />
                </ListItemIcon>
              )}
              <ListItemText primary={item.label} />
            </ListItemButton>
          );
        })}
      </List>

      <Box sx={{ mt: 'auto', p: 2 }}>
        <Typography variant="caption" color="text.secondary">
          BDA · Grupo 2
        </Typography>
      </Box>
    </Drawer>
  );
}
