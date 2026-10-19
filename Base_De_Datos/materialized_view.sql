
/*
- Vista materializada 
 Objeto de base de datos que guarda físicamente en disco el resultado de una consulta SQL, 
 permitiendo consultar datos precalculados de forma mucho más rápida que realizando consultas.

-- Características principales:
Ejecuta la consulta en el momento de su creación.
Guarda físicamente el resultado en disco como si fuera una tabla física.
No se actualiza sola cuando cambian las tablas de origen; requiere una orden explícita de actualización (REFRESH).

*/

-- Demanda por Estación y Franja Horaria
-- Vista materializada
-- CTE para calcular la cantidad de viajes iniciados y finalizados por estación y franja horaria.
CREATE MATERIALIZED VIEW mv_demanda_estacion_horaria AS
WITH viajes_iniciados AS (
    SELECT
        origen_id AS id_estacion,
        EXTRACT(HOUR FROM fecha_inicio)::INT AS franja_horaria,
        COUNT(*) AS cantidad_viajes
    FROM viajes
    GROUP BY origen_id, EXTRACT(HOUR FROM fecha_inicio)::INT
),
viajes_finalizados AS (
    SELECT
        destino_id AS id_estacion,
        EXTRACT(HOUR FROM fecha_fin)::INT AS franja_horaria,
        COUNT(*) AS cantidad_viajes
    FROM viajes
    GROUP BY destino_id, EXTRACT(HOUR FROM fecha_fin)::INT
)
-- Selecciona todas las estaciones y combina con las franjas horarias, uniendo los resultados de los CTEs.
SELECT
    e.id_estacion,
    e.nombre_estacion,
    h.hora AS franja_horaria,
    COALESCE(vi.cantidad_viajes, 0) AS viajes_iniciados,
    COALESCE(vf.cantidad_viajes, 0) AS viajes_finalizados
FROM estaciones e
CROSS JOIN generate_series(0, 23) AS h(hora)
LEFT JOIN viajes_iniciados   vi ON e.id_estacion = vi.id_estacion AND h.hora = vi.franja_horaria
LEFT JOIN viajes_finalizados vf ON e.id_estacion = vf.id_estacion AND h.hora = vf.franja_horaria
WITH DATA;

-- Índice único (necesario para REFRESH CONCURRENTLY)
CREATE UNIQUE INDEX idx_mv_demanda_estacion_hora
ON mv_demanda_estacion_horaria (id_estacion, franja_horaria);

-- Función de refresco
CREATE OR REPLACE FUNCTION fn_refrescar_demanda_estaciones()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_demanda_estacion_horaria;
END;
$$ LANGUAGE plpgsql;

-- Refresco cada hora (requiere pg_cron)
-- Instalar la dependencia más adelante
CREATE EXTENSION IF NOT EXISTS pg_cron;

SELECT cron.schedule(
    'refrescar_demanda_estaciones',
    '0 * * * *',
    $$SELECT fn_refrescar_demanda_estaciones();$$
);


