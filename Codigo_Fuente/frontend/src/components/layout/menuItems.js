import DashboardIcon from '@mui/icons-material/Dashboard';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import PlaceIcon from '@mui/icons-material/Place';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import HistoryIcon from '@mui/icons-material/History';
import InsightsIcon from '@mui/icons-material/Insights';

export const DRAWER_WIDTH = 240;

export const MENU_BY_ROLE = {
  Operador: [
    { label: 'Flota', path: '/operador/flota', icon: DirectionsBikeIcon },
    { label: 'Estaciones', path: '/operador/estaciones', icon: PlaceIcon },
    { label: 'Rebalanceo', path: '/operador/rebalanceo', icon: SwapHorizIcon },
    { label: 'Auditoría', path: '/operador/auditoria', icon: HistoryIcon },
    { label: 'Demanda', path: '/operador/demanda', icon: InsightsIcon },
  ],

  // Tu compañero puede completar este menú cuando migre el panel de usuario.
  Usuario: [
    { label: 'Resumen', path: '/usuario', icon: DashboardIcon },
  ],
};
