CREATE OR REPLACE PROCEDURE finalizar_viaje(p_id_viaje BIGINT, p_id_estacion_destino BIGINT)
LANGUAGE plpgsql AS $$
DECLARE
    var_minutos_uso INT;
    var_id_usuario BIGINT;
    var_id_vehiculo BIGINT;
    var_id_tarifa BIGINT;
    var_fecha_inicio TIMESTAMP;
    var_fecha_fin TIMESTAMP := NOW();
    var_valor_desbloqueo DECIMAL(10,2);
    var_valor_minuto DECIMAL(10,2);
    var_costo_final DECIMAL(12,2);
    var_id_billetera BIGINT;
    var_capacidad_destino INT;
    var_ocupacion_destino BIGINT;
BEGIN
    -- 1. Bloquear y validar la estación de destino.
    SELECT capacidad INTO var_capacidad_destino
    FROM estaciones
    WHERE id_estacion = p_id_estacion_destino AND estacion_activa = TRUE
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Error: La estación de destino % no existe o está inactiva.', p_id_estacion_destino;
    END IF;

    -- 2. Obtener y bloquear el viaje activo
    SELECT fecha_inicio, id_usuario, id_vehiculo, id_tarifa
    INTO var_fecha_inicio, var_id_usuario, var_id_vehiculo, var_id_tarifa
    FROM viajes
    WHERE id_viaje = p_id_viaje AND estado_viaje = 'Activo'
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Error: El viaje % no existe o ya está Finalizado.', p_id_viaje;
    END IF;

    -- 3. Verificar que la estación de destino tenga cupo disponible
    SELECT COUNT(*) INTO var_ocupacion_destino
    FROM vehiculos
    WHERE id_estacion = p_id_estacion_destino;

    IF var_ocupacion_destino + 1 > var_capacidad_destino THEN
        RAISE EXCEPTION 'Error: La estación de destino % está llena (ocupación: %, capacidad: %).',
            p_id_estacion_destino, var_ocupacion_destino, var_capacidad_destino;
    END IF;

    -- 4. Calcular minutos exactos de uso (mínimo 1 minuto)
    var_minutos_uso := GREATEST(1, CEIL(EXTRACT(EPOCH FROM (var_fecha_fin - var_fecha_inicio)) / 60.0))::INT;

    -- 5. Obtener valores de la tarifa asociada al viaje
    SELECT valor_desbloqueo, valor_minuto
    INTO var_valor_desbloqueo, var_valor_minuto
    FROM tarifas
    WHERE id_tarifa = var_id_tarifa;

    var_costo_final := var_valor_desbloqueo + (var_minutos_uso * var_valor_minuto);

    -- 6. Actualizar el viaje
    UPDATE viajes
    SET fecha_fin = var_fecha_fin,
        minutos_viaje = var_minutos_uso,
        costo = var_costo_final,
        id_destino = p_id_estacion_destino,
        estado_viaje = 'Finalizado'
    WHERE id_viaje = p_id_viaje;

    -- 7. Descontar saldo de la billetera y registrar movimiento
    UPDATE billeteras
    SET saldo = saldo - var_costo_final
    WHERE id_usuario = var_id_usuario
    RETURNING id_billetera INTO var_id_billetera;

    INSERT INTO movimientos_billeteras (id_billetera, id_viaje, tipo_movimiento, monto, fecha_movimiento)
    VALUES (var_id_billetera, p_id_viaje, 'Cobro_viaje', var_costo_final, var_fecha_fin);

    -- 8. Anclar el vehículo en la estación de destino
    UPDATE vehiculos
    SET id_estacion = p_id_estacion_destino,
        estado_vehiculo = 'Disponible'
    WHERE id_vehiculo = var_id_vehiculo;
END;
$$;