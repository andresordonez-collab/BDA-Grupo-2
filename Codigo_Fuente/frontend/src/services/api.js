import axios from 'axios';

// Comunicamos el frontend (React) con el backend 
const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

export const GetEstacionesDisponibles = async () => {
    const respuesta = await api.get('/estaciones/disponibilidad');
    return respuesta.data;
};

export default api;