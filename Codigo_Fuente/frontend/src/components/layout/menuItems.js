import DashboardIcon from '@mui/icons-material/Dashboard';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import PlaceIcon from '@mui/icons-material/Place';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import HistoryIcon from '@mui/icons-material/History';
import InsightsIcon from '@mui/icons-material/Insights';
import PriceChangeIcon from '@mui/icons-material/PriceChange';
import PeopleIcon from '@mui/icons-material/People';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';

export const DRAWER_WIDTH = 240;

export const MENU_BY_ROLE = {
  Operador: [
    { label: 'Flota', path: '/operador/flota', icon: DirectionsBikeIcon },
    { label: 'Estaciones', path: '/operador/estaciones', icon: PlaceIcon },
    { label: 'Rebalanceo', path: '/operador/rebalanceo', icon: SwapHorizIcon },
    { label: 'Auditoría', path: '/operador/auditoria', icon: HistoryIcon },
    { label: 'Demanda', path: '/operador/demanda', icon: InsightsIcon },
    { label: 'Tarifas', path: '/operador/tarifas', icon: PriceChangeIcon },
    { label: 'Usuarios', path: '/operador/usuarios', icon: PeopleIcon },
    {
      label: 'Billeteras',
      path: '/operador/billeteras',
      icon: AccountBalanceWalletIcon,
    },
  ],

  Usuario: [
    { label: 'Estaciones', path: '/usuario', icon: PlaceIcon },
    { label: 'Mi viaje', path: '/usuario/mi-viaje', icon: DirectionsBikeIcon },
    { label: 'Billetera', path: '/usuario/billetera', icon: AccountBalanceWalletIcon },
    { label: 'Tarifas', path: '/usuario/tarifas', icon: PriceChangeIcon },
    { label: 'Historial', path: '/usuario/historial', icon: HistoryIcon },
    { label: 'Resumen mensual', path: '/usuario/resumen-mensual', icon: DashboardIcon },
  ],
};
