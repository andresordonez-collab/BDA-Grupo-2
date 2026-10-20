CREATE OR REPLACE PROCEDURE finalizar_viaje(p_id_viaje BIGINT, p_id_estacion_destino BIGINT)

	
LANGUAGE plpgsql AS $$
DECLARE
	-- Declaramos variables para guardar informacion
	var_minutos_uso INT;
	var_id_usuario BIGINT;
	var_id_vehiculo BIGINT;
	var_fecha_inicio TIMESTAMP;
	var_valor_minuto DECIMAL (10,2);
	var_costo_final DECIMAL (10,2);
	var_id_billetera BIGINT;
	
	var_hora_inicio INT;
    var_minuto_inicio INT;
    var_hora_actual INT;
    var_minuto_actual INT;

	
BEGIN
	-- Almacenamos los datos necesarios en las variables declaradas anteriormente
	SELECT fecha_inicio, id_usuario, id_vehiculo
	INTO var_fecha_inicio, var_id_usuario, var_id_vehiculo
	FROM viajes
	-- Seleccionamos solo los viajes los cuales tienen el estado 'Activo' es decir los que aun no terminan su trayecto
	WHERE id_viaje = p_id_viaje AND estado_viaje = 'Activo';
	
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'Error: El viaje % no existe o ya está Finalizado.', p_id_viaje;
    END IF;
	
	-- Calculamos los minutos en uso del vehiculo tras su trayecto, tomamos los minutos de inicio en el cual se efectuo el viaje
	
	-- Extraemos la hora y minuto de el atributo fecha_inicio de viajes
	SELECT EXTRACT(HOUR FROM var_fecha_inicio), EXTRACT(MINUTE FROM var_fecha_inicio)
    INTO var_hora_inicio, var_minuto_inicio;
	
	-- Extraemos la hora y minuto actual 
	SELECT EXTRACT(HOUR FROM NOW()), EXTRACT(MINUTE FROM NOW())
    INTO var_hora_actual, var_minuto_actual;
    
    -- Si la hora actual es menor a la inical, (caso en que sea media noche) le sumamos 24 hrs (un dia)
    IF var_hora_actual < var_hora_inicio THEN
    	var_hora_actual := var_hora_actual + 24;
    END IF;
	
	-- Calculamos el costo según la tarifa y los minutos utilizados
	var_minutos_uso := ((var_hora_actual - var_hora_inicio) * 60) + (var_minuto_actual - var_minuto_inicio);
	
	
	-- Obtener el valor por minuto del tipo de vehículo utilizado
    SELECT tarifas.valor_minuto
    INTO var_valor_minuto
    FROM vehiculos
    JOIN tarifas ON vehiculos.id_tipo_vehiculo = tarifas.id_tipo_vehiculo
    WHERE vehiculos.id_vehiculo = var_id_vehiculo AND tarifas.activa = TRUE;
    
    -- Calculamos el costo total
    var_costo_final := var_minutos_uso * var_valor_minuto;
    
    -- Actualizamos la tabla de Viajes
    UPDATE viajes
    SET fecha_fin = NOW(),
    	minutos_viaje = var_minutos_uso,
    	costo = var_costo_final,
    	id_destino = p_id_estacion_destino,
    	estado_viaje = 'Finalizado'
    WHERE id_viaje = p_id_viaje;
    
    -- Guardamos el id de la billetera asociado al usuario correspondiente
    SELECT id_billetera 
    INTO var_id_billetera
    FROM billeteras 
    WHERE id_usuario = var_id_usuario;
    
    -- Descontamos el dinero de la billetera
    UPDATE billeteras
    SET saldo = saldo - var_costo_final
    WHERE id_usuario = var_id_usuario;
    
    -- Registrar el cobro en el historial de movimientos de la billetera
	INSERT INTO movimientos_billeteras (id_billetera, id_viaje, tipo_movimiento, monto, fecha_movimiento)
    VALUES (var_id_billetera, p_id_viaje, 'Cobro_viaje', var_costo_final, NOW());
    
    
    -- Actualizar el vehículo para anclarlo en la estación de destino
    UPDATE vehiculos
    SET id_estacion = p_id_estacion_destino,
        estado_vehiculo = 'Disponible'
    WHERE id_vehiculo = var_id_vehiculo;
    
END;
$$;