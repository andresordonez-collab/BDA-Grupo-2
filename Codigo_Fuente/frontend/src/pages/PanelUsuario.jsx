import {useEffect, useState} from "react";
import {GetEstacionesDisponibles} from "../services/api";
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Box,
  Button,
  Typography,
  Alert,
  LinearProgress,
  TextField,
  InputAdornment,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';
import './PanelUsuario.css';
import { getErrorMessage } from '../http-common';


function PanelUsuario() {
    // Estado para almacenar las estaciones disponibles, estados de carga y errores
    const [estacionesDisponibles, setEstacionesDisponibles] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState(null);
    const [busqueda, setBusqueda] = useState('');

    // Funcion que usa el backend para obtener las estaciones disponibles
    const cargarEstacionesDisponibles = async () => {
        try {
            setCargando(true);
            setError(null);
            const data = await GetEstacionesDisponibles();
            setEstacionesDisponibles(data);
        }

        catch (err) {
            setError(getErrorMessage(err));
        }

        finally {
            setCargando(false);
        }
    };

    useEffect(() => {
        cargarEstacionesDisponibles();
    }, []);

    const estacionesFiltradas = estacionesDisponibles.filter((estacion) =>
        estacion.nombreEstacion
        .toLowerCase()
        .includes(busqueda.trim().toLowerCase())
    );

    const totalBicicletas = estacionesDisponibles.reduce((total, estacion) => total + estacion.cantidadBicicletas, 0);

    const totalScooters = estacionesDisponibles.reduce((total, estacion) => total + estacion.cantidadScooters,0);

    const resumen = [
    {
        titulo: 'Estaciones con disponibilidad',
        valor: estacionesDisponibles.length,
    },
    {
        titulo: 'Bicicletas disponibles',
        valor: totalBicicletas,
    },
    {
        titulo: 'Scooters disponibles',
        valor: totalScooters,
    },
    ];


    return (
        <div className = "panel-usuario" style={{ padding: '2rem', fontFamily: 'sans-serif' }}>


        <header className="panel-usuario__encabezado">
        <p className="panel-usuario__marca">
            Bici & Scoot · Portal de usuario
        </p>

        <h1>Encuentra tu próxima estación</h1>

        <p className="panel-usuario__descripcion">
            Consulta las bicicletas y scooters disponibles para tu próximo viaje.
        </p>
        </header>

        {!cargando && !error && (
        <section
            className="panel-usuario__resumen"
            aria-label="Resumen de disponibilidad"
        >
            {resumen.map((dato) => (
            <Paper
                key={dato.titulo}
                variant="outlined"
                className="panel-usuario__tarjeta"
            >
                <p className="panel-usuario__etiqueta">{dato.titulo}</p>
                <p className="panel-usuario__valor">{dato.valor}</p>
            </Paper>
            ))}
        </section>
        )}
            
 
        <Box className="panel-usuario__acciones" sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <TextField
                className="panel-usuario__busqueda"
                label="Buscar estación"
                placeholder="Por ejemplo: Plaza de Armas"
                type="search"
                value={busqueda}
                onChange={(event) => setBusqueda(event.target.value)}
                slotProps={{
                input: {
                    startAdornment: (
                    <InputAdornment position="start">
                        <SearchIcon color="action" />
                    </InputAdornment>
                    ),
                },
                }}
            />
        <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={cargarEstacionesDisponibles}
            disabled={cargando}
        >
            {cargando ? 'Actualizando…' : 'Actualizar'}
        </Button>
        </Box>

        {cargando && ( 
            <Box role="status" sx={{ my: 3 }}>
                <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 1 }}
                >
                Cargando estaciones…
                </Typography>

                <LinearProgress aria-label="Cargando estaciones" />
            </Box>
        )}

            {error && (
            <Alert severity="error" sx={{ my: 2 }}>
                {error}
            </Alert>
            )}

        {!cargando && !error && estacionesFiltradas.length === 0 && (
            <Alert severity="info" sx={{ mt: 2 }}>
                {busqueda.trim()
                ? 'No se encontraron estaciones con ese nombre.'
                : 'No hay estaciones disponibles en este momento.'}
            </Alert>
        )}  
        {!cargando && !error && estacionesFiltradas.length > 0 && (
            <TableContainer component={Paper} className="panel-usuario__tabla" sx={{ mt: 2 }}>
                <Table aria-label="Disponibilidad de estaciones">
                    <TableHead sx={{ bgcolor: 'action.hover' }}>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Estación</TableCell>
                            <TableCell align="right">Bicicletas</TableCell>
                            <TableCell align="right">Scooters</TableCell>
                            <TableCell align="right">Batería promedio (%)</TableCell>
                        </TableRow>
                    </TableHead>

                    <TableBody>
                        {estacionesFiltradas.map((estacion) => (
                            <TableRow key={estacion.idEstacion} hover>
                                <TableCell>{estacion.idEstacion}</TableCell>
                                <TableCell>{estacion.nombreEstacion}</TableCell>
                                <TableCell align="right">
                                    {estacion.cantidadBicicletas}
                                </TableCell>
                                <TableCell align="right">
                                    {estacion.cantidadScooters}
                                </TableCell>
                                <TableCell align="right">
                                    {estacion.bateriaPromedio ?? '—'}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        )}
    </div>
    );
}

export default PanelUsuario;