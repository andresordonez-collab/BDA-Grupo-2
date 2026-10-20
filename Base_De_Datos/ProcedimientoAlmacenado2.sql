CREATE OR REPLACE PROCEDURE rebalancear_flota(
    p_id_estacion_origen BIGINT,
    p_id_estacion_destino BIGINT,
    p_id_operador BIGINT,
    p_cantidad_vehiculos INT
)
LANGUAGE plpgsql AS $$
DECLARE
    var_capacidad_destino INT;
    var_ocupacion_destino BIGINT;
    var_vehiculos BIGINT[];
    var_id_orden BIGINT;
BEGIN
    -- REPEATABLE READ conserva una ocupación anterior incluso después de esperar bloqueos.
    IF current_setting('transaction_isolation') = 'repeatable read' THEN
        RAISE EXCEPTION 'rebalancear_flota no admite REPEATABLE READ; utilice READ COMMITTED o SERIALIZABLE.';
    END IF;

    IF p_cantidad_vehiculos IS NULL OR p_cantidad_vehiculos <= 0 THEN
        RAISE EXCEPTION 'La cantidad de vehículos debe ser mayor que cero.';
    END IF;

    IF p_id_estacion_origen IS NULL OR p_id_estacion_destino IS NULL THEN
        RAISE EXCEPTION 'Debe indicar las estaciones de origen y destino.';
    END IF;

    IF p_id_estacion_origen = p_id_estacion_destino THEN
        RAISE EXCEPTION 'Las estaciones de origen y destino deben ser diferentes.';
    END IF;

    -- Bloquear primero las estaciones en orden ascendente para reducir interbloqueos.
    PERFORM id_estacion
    FROM estaciones
    WHERE id_estacion IN (p_id_estacion_origen, p_id_estacion_destino)
    ORDER BY id_estacion
    FOR UPDATE;

    PERFORM 1
    FROM estaciones
    WHERE id_estacion = p_id_estacion_origen AND estacion_activa = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La estación de origen % no existe o está inactiva.', p_id_estacion_origen;
    END IF;

    SELECT capacidad INTO var_capacidad_destino
    FROM estaciones
    WHERE id_estacion = p_id_estacion_destino AND estacion_activa = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La estación de destino % no existe o está inactiva.', p_id_estacion_destino;
    END IF;

    -- Mantener vigentes el estado y el rol del operador durante la operación.
    PERFORM 1
    FROM usuarios u
    JOIN roles r ON r.id_rol = u.id_rol
    WHERE u.id_usuario = p_id_operador
      AND u.usuario_activo = TRUE
      AND r.nombre_rol = 'Operador'
    FOR SHARE OF u, r;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'El usuario % no existe, está inactivo o no tiene el rol Operador.', p_id_operador;
    END IF;

    -- Seleccionar y bloquear hasta N disponibles, siempre por ID ascendente.
    SELECT ARRAY(
        SELECT id_vehiculo
        FROM vehiculos
        WHERE id_estacion = p_id_estacion_origen
          AND estado_vehiculo = 'Disponible'
        ORDER BY id_vehiculo
        LIMIT p_cantidad_vehiculos
        FOR UPDATE
    ) INTO var_vehiculos;

    IF cardinality(var_vehiculos) < p_cantidad_vehiculos THEN
        RAISE EXCEPTION 'La estación de origen % tiene solo % vehículos disponibles; se solicitaron %.',
            p_id_estacion_origen, cardinality(var_vehiculos), p_cantidad_vehiculos;
    END IF;

    -- Todo vehículo asociado al destino ocupa espacio, cualquiera sea su estado.
    SELECT COUNT(*) INTO var_ocupacion_destino
    FROM vehiculos
    WHERE id_estacion = p_id_estacion_destino;

    IF var_ocupacion_destino + p_cantidad_vehiculos > var_capacidad_destino THEN
        RAISE EXCEPTION 'La estación de destino % no tiene capacidad para recibir % vehículos (ocupación: %, capacidad: %).',
            p_id_estacion_destino, p_cantidad_vehiculos, var_ocupacion_destino, var_capacidad_destino;
    END IF;

    -- Trasladar exactamente los vehículos seleccionados, conservando su estado.
    UPDATE vehiculos
    SET id_estacion = p_id_estacion_destino
    WHERE id_vehiculo = ANY(var_vehiculos);

    INSERT INTO ordenes_rebalanceo (
        id_estacion_origen, id_estacion_destino, id_operador,
        cantidad_vehiculos, estado_orden
    )
    VALUES (
        p_id_estacion_origen, p_id_estacion_destino, p_id_operador,
        p_cantidad_vehiculos, 'Completada'
    )
    RETURNING id_orden INTO var_id_orden;

    INSERT INTO detalles_rebalanceo (id_orden, id_vehiculo)
    SELECT var_id_orden, unnest(var_vehiculos);

    -- Sin capturar excepciones: cualquier error revierte toda la llamada.
    -- Los demás movimientos deben bloquear las estaciones antes que los vehículos.
END;
$$;
