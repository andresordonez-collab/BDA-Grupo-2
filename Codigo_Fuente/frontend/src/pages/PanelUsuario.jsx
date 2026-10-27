import {useEffect, useState} from "react";
import {GetEstacionesDisponibles} from "../services/api";

function PanelUsuario() {
    // Estado para almacenar las estaciones disponibles, estados de carga y errores
    const [estacionesDisponibles, setEstacionesDisponibles] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState(null);

    // Funcion que usa el backend para obtener las estaciones disponibles
    const cargarEstacionesDisponibles = async () => {
        try {
            setCargando(true);
            const data = await GetEstacionesDisponibles();
            setEstacionesDisponibles(data);
            setError(null);
        }

        catch (err) {
            console.error("Error al conectar con Spring Boot:", err);
            setError("No se pudo cargar la estaciones disponibles.");
        }

        finally {
            setCargando(false);
        }
    };

    useEffect(() => {
        cargarEstacionesDisponibles();
    }, []);

    return (
        <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
        <h1>Disponibilidad de Estaciones</h1>
        <button onClick={cargarEstacionesDisponibles} style={{ marginBottom: '1rem', padding: '0.5rem 1rem', cursor: 'pointer' }}>
            Actualizar disponibilidad
        </button>

        {cargando && <p>Cargando datos desde el servidor...</p>}
        {error && <p style={{ color: 'red', fontWeight: 'bold' }}>{error}</p>}

        {!cargando && !error && (
            <table border="1" cellPadding="10" style={{ borderCollapse: 'collapse', width: '100%', textAlign: 'left' }}>
            <thead style={{ backgroundColor: '#f2f2f2' }}>
                <tr>
                <th>ID</th>
                <th>Estación</th>
                <th>Dirección</th>
                <th>Capacidad Máxima</th>
                <th>Vehículos Disponibles</th>
                </tr>
            </thead>
            <tbody>
                {estacionesDisponibles.map((estacion) => (
                <tr key={estacion.id_estacion || estacion.idEstacion}>
                    <td>{estacion.id_estacion ?? estacion.idEstacion}</td>
                    <td>{estacion.nombre_estacion ?? estacion.nombreEstacion}</td>
                    <td>{estacion.direccion}</td>
                    <td>{estacion.capacidad}</td>
                    <td>
                    {estacion.vehiculos_disponibles ?? estacion.vehiculosDisponibles ?? estacion.disponibles ?? 0}
                    </td>
                </tr>
                ))}
            </tbody>
        </table>
        )}
    </div>
    );
}

export default PanelUsuario;