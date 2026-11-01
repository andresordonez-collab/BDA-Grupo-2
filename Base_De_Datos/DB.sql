-- ==================================================
-- CREACIÓN DE TABLAS
-- ==================================================
-- Define la estructura principal del sistema.

-- Almacena los roles disponibles del sistema.
CREATE TABLE roles(
	id_rol BIGSERIAL PRIMARY KEY, 
	nombre_rol VARCHAR(50) UNIQUE NOT NULL CHECK (nombre_rol IN ('Operador', 'Usuario'))
);

-- Registra a los usuarios y el rol asignado a cada uno.
CREATE TABLE usuarios(
	id_usuario BIGSERIAL PRIMARY KEY,
	id_rol BIGINT NOT NULL,
	nombre_usuario VARCHAR(100) NOT NULL,
	email VARCHAR(100) NOT NULL UNIQUE,
	password_hash VARCHAR(200) NOT NULL,
	usuario_activo BOOLEAN NOT NULL DEFAULT TRUE,
	fecha_registro TIMESTAMP NOT NULL DEFAULT NOW(),
	FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

-- Representa la billetera de cada usuario y su saldo disponible.
CREATE TABLE billeteras(
	id_billetera BIGSERIAL PRIMARY KEY,
	id_usuario BIGINT UNIQUE NOT NULL,
	saldo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
	FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- Contiene las estaciones, su ubicación, capacidad y estado.
CREATE TABLE estaciones(
	id_estacion BIGSERIAL PRIMARY KEY,
	nombre_estacion VARCHAR(100) UNIQUE NOT NULL,
	direccion VARCHAR(200) NOT NULL,
	latitud DECIMAL(9,6) NOT NULL,
	longitud DECIMAL(9,6) NOT NULL,
	capacidad INT NOT NULL CHECK (capacidad > 0),
	estacion_activa BOOLEAN NOT NULL DEFAULT TRUE
);

-- Registra el traslado de vehículos entre estaciones.
CREATE TABLE ordenes_rebalanceo(
	id_orden BIGSERIAL PRIMARY KEY,
	id_estacion_origen BIGINT NOT NULL,
	id_estacion_destino BIGINT NOT NULL CHECK (id_estacion_origen <> id_estacion_destino),
	id_operador BIGINT NOT NULL,
	cantidad_vehiculos INT NOT NULL CHECK (cantidad_vehiculos > 0),
	fecha_orden TIMESTAMP NOT NULL DEFAULT NOW(),
	estado_orden VARCHAR(20) NOT NULL CHECK (estado_orden IN ('Completada', 'Cancelada')),
	FOREIGN KEY (id_estacion_origen) REFERENCES estaciones(id_estacion),
	FOREIGN KEY (id_estacion_destino) REFERENCES estaciones(id_estacion),
	FOREIGN KEY (id_operador) REFERENCES usuarios(id_usuario)
);

-- Define los tipos de vehículos permitidos.
CREATE TABLE tipos_vehiculos(
	id_tipo_vehiculo BIGSERIAL PRIMARY KEY,
	nombre_tipo_vehiculo VARCHAR(20) UNIQUE NOT NULL CHECK (nombre_tipo_vehiculo IN ('Bicicleta', 'Scooter'))
);

-- Registra los vehículos, su tipo, ubicación, batería y estado.
CREATE TABLE vehiculos(
	id_vehiculo BIGSERIAL PRIMARY KEY,
	id_tipo_vehiculo BIGINT NOT NULL,
	id_estacion BIGINT,
	codigo_vehiculo VARCHAR(30) UNIQUE NOT NULL,
	nivel_bateria INT CHECK (nivel_bateria BETWEEN 0 AND 100),
	fecha_alta TIMESTAMP NOT NULL DEFAULT NOW(),
	estado_vehiculo VARCHAR(20) NOT NULL CHECK (estado_vehiculo IN ('Disponible', 'En uso', 'En mantención', 'Dado de baja')),
	FOREIGN KEY (id_tipo_vehiculo) REFERENCES tipos_vehiculos(id_tipo_vehiculo),
	FOREIGN KEY (id_estacion) REFERENCES estaciones(id_estacion)
);

-- Guarda el historial de cambios de estado de los vehículos.
CREATE TABLE auditorias_vehiculos(
	id_auditoria BIGSERIAL PRIMARY KEY,
	id_vehiculo BIGINT NOT NULL,
	id_operador BIGINT NOT NULL,
	estado_anterior VARCHAR(20) NOT NULL CHECK (estado_anterior IN ('Disponible', 'En uso', 'En mantención', 'Dado de baja')),
	estado_nuevo VARCHAR(20) NOT NULL CHECK (estado_nuevo IN ('Disponible', 'En uso', 'En mantención', 'Dado de baja')),
	fecha_auditoria TIMESTAMP NOT NULL DEFAULT NOW(),
	motivo TEXT,
	FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo),
	FOREIGN KEY (id_operador) REFERENCES usuarios(id_usuario)
);

-- Relaciona las órdenes de rebalanceo con los vehículos trasladados.
CREATE TABLE detalles_rebalanceo(
	id_orden BIGINT NOT NULL,
	id_vehiculo BIGINT NOT NULL,
	PRIMARY KEY (id_orden, id_vehiculo),
	FOREIGN KEY (id_orden) REFERENCES ordenes_rebalanceo(id_orden),
	FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo)
);

-- Define los valores de cobro según el tipo de vehículo.
CREATE TABLE tarifas(
	id_tarifa BIGSERIAL PRIMARY KEY,
	id_tipo_vehiculo BIGINT NOT NULL,
	valor_desbloqueo DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (valor_desbloqueo >= 0),
	valor_minuto DECIMAL(10,2) NOT NULL CHECK (valor_minuto >= 0),
	vigente_desde TIMESTAMP NOT NULL DEFAULT NOW(),
	activa BOOLEAN NOT NULL DEFAULT TRUE,
	FOREIGN KEY (id_tipo_vehiculo) REFERENCES tipos_vehiculos(id_tipo_vehiculo)
);

-- Registra los viajes realizados por los usuarios.
CREATE TABLE viajes(
	id_viaje BIGSERIAL PRIMARY KEY,
	id_usuario BIGINT NOT NULL,
	id_vehiculo BIGINT NOT NULL,
	id_origen BIGINT NOT NULL,
	id_destino BIGINT,
	id_tarifa BIGINT NOT NULL,
	fecha_inicio TIMESTAMP NOT NULL DEFAULT NOW(),
	fecha_fin TIMESTAMP CHECK (fecha_fin >= fecha_inicio),
	minutos_viaje INT CHECK (minutos_viaje >= 0),
	costo DECIMAL(12,2) CHECK (costo >= 0),
	estado_viaje VARCHAR(20) NOT NULL CHECK (estado_viaje IN ('Activo', 'Finalizado')),
	FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
	FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo),
	FOREIGN KEY (id_origen) REFERENCES estaciones(id_estacion),
	FOREIGN KEY (id_destino) REFERENCES estaciones(id_estacion),
	FOREIGN KEY (id_tarifa) REFERENCES tarifas(id_tarifa)
);

-- Registra recargas, cobros y ajustes de las billeteras.
CREATE TABLE movimientos_billeteras(
	id_movimiento BIGSERIAL PRIMARY KEY,
	id_billetera BIGINT NOT NULL,
	id_viaje BIGINT,
	tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('Recarga', 'Cobro_viaje', 'Ajuste')),
	monto DECIMAL(12,2) NOT NULL CHECK (monto >= 0),
	fecha_movimiento TIMESTAMP NOT NULL DEFAULT NOW(),
	FOREIGN KEY (id_billetera) REFERENCES billeteras(id_billetera),
	FOREIGN KEY (id_viaje) REFERENCES viajes(id_viaje)
);

-- ==================================================
-- ÍNDICES Y TRIGGERS
-- ==================================================
-- Optimiza consultas frecuentes y aplica validaciones automáticas.

-- Índices.

--Indexar el estado de los vehículos.

CREATE INDEX idx_vehiculos_estado ON vehiculos(estado_vehiculo);

-- Indexar los id_usuarios en la tabla viajes.

CREATE INDEX idx_viajes_usuario ON viajes(id_usuario);

-- Indexar la fecha de inicio del viaje.

CREATE INDEX idx_viajes_fecha_inicio ON viajes(fecha_inicio);

-- Trigger 1: Impedir el inicio de un viaje si el usuario tiene saldo negativo, 
-- ya posee otro viaje activo o si la batería del vehículo es inferior al 20%.

CREATE OR REPLACE FUNCTION validar_inicio_viaje()
RETURNS TRIGGER
AS
$BODY$
DECLARE
    v_saldo DECIMAL(12,2);
    v_bateria INT;
    v_tipo VARCHAR(20);
BEGIN
    -- 1. Obtener saldo del usuario
    SELECT saldo INTO v_saldo
    FROM billeteras
    WHERE id_usuario = NEW.id_usuario;

    -- 2. Obtener batería y tipo de vehículo en una sola consulta (JOIN antes de WHERE)
    SELECT v.nivel_bateria, tv.nombre_tipo_vehiculo 
    INTO v_bateria, v_tipo
    FROM vehiculos v
    JOIN tipos_vehiculos tv ON v.id_tipo_vehiculo = tv.id_tipo_vehiculo
    WHERE v.id_vehiculo = NEW.id_vehiculo;

    -- Validación A: Saldo negativo
    IF v_saldo < 0 THEN
        RAISE EXCEPTION 'No puede iniciar el viaje: tiene saldo negativo ($%).', v_saldo;
    END IF;

    -- Validación B: Batería inferior al 20% en Scooters
    IF v_tipo = 'Scooter' AND (v_bateria IS NULL OR v_bateria < 20) THEN
        RAISE EXCEPTION 'Nivel de batería insuficiente, no se puede iniciar el viaje.';
    END IF;

    -- Validación C: Viaje activo existente
    IF EXISTS (
        SELECT 1
        FROM viajes
        WHERE id_usuario = NEW.id_usuario AND estado_viaje = 'Activo'
    ) THEN
        RAISE EXCEPTION 'El usuario ya tiene un viaje en curso.';
    END IF;

    RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER trigger_validar_inicio_viaje
BEFORE INSERT ON viajes
FOR EACH ROW
WHEN (NEW.estado_viaje = 'Activo')
EXECUTE FUNCTION validar_inicio_viaje();

-- trigger 2:  Auditar cada cambio de estado de un vehículo a "En mantención" o "Dado de  baja", 
-- guardando el operador responsable, la fecha y el motivo.

CREATE OR REPLACE FUNCTION auditar_cambio_estado_vehiculo()
RETURNS TRIGGER
AS
$BODY$
DECLARE
    v_id_operador BIGINT;
    v_motivo TEXT;
BEGIN
    -- Validar que el vehículo cambie a un estado que requiere auditoría.
    IF NEW.estado_vehiculo IN ('En mantención', 'Dado de baja') AND NEW.estado_vehiculo IS DISTINCT FROM OLD.estado_vehiculo 
    THEN
        -- 1. Obtener el operador y el motivo enviados por el backend.
        v_id_operador := current_setting('app.id_operador')::BIGINT;
        v_motivo := current_setting('app.motivo', true);

        -- 2. Registrar el cambio de estado en la tabla de auditoría.
        INSERT INTO auditorias_vehiculos (
            id_vehiculo, 
            id_operador, 
            estado_anterior, 
            estado_nuevo, 
            fecha_auditoria,
            motivo
        )
        VALUES (
            NEW.id_vehiculo, 
            v_id_operador, 
            OLD.estado_vehiculo, 
            NEW.estado_vehiculo, 
            NOW(),
            v_motivo
        );
    END IF;

    -- Permitir que la actualización del vehículo continúe.
    RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql;

-- Ejecutar la auditoría después de actualizar el estado del vehículo.
CREATE TRIGGER trigger_auditar_cambio_estado_vehiculo
AFTER UPDATE OF estado_vehiculo ON vehiculos
FOR EACH ROW
EXECUTE FUNCTION auditar_cambio_estado_vehiculo();

-- ==================================================
-- PROCEDIMIENTO ALMACENADO 1: FINALIZAR VIAJE
-- ==================================================
-- Finaliza un viaje, calcula su costo, actualiza saldos y libera el vehículo.

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

-- ==================================================
-- PROCEDIMIENTO ALMACENADO 2: REBALANCEAR FLOTA
-- ==================================================
-- Traslada vehículos entre estaciones validando capacidad y operador.

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

-- ==================================================
-- VISTA MATERIALIZADA
-- ==================================================
-- Resume la demanda de viajes por estación y franja horaria.

-- Demanda por Estación y Franja Horaria
-- Vista materializada
-- CTE para calcular la cantidad de viajes iniciados y finalizados por estación y franja horaria.
CREATE MATERIALIZED VIEW mv_demanda_estacion_horaria AS
WITH viajes_iniciados AS (
    SELECT
        id_origen AS id_estacion,
        EXTRACT(HOUR FROM fecha_inicio)::INT AS franja_horaria,
        COUNT(*) AS cantidad_viajes
    FROM viajes
    GROUP BY id_origen, EXTRACT(HOUR FROM fecha_inicio)::INT
),
viajes_finalizados AS (
    SELECT
        id_destino AS id_estacion,
        EXTRACT(HOUR FROM fecha_fin)::INT AS franja_horaria,
        COUNT(*) AS cantidad_viajes
    FROM viajes
    GROUP BY id_destino, EXTRACT(HOUR FROM fecha_fin)::INT
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

-- Programar el refresco automático de la vista materializada
CREATE EXTENSION IF NOT EXISTS pg_cron;

SELECT cron.schedule(
    'refrescar_demanda_estaciones',
    '0 * * * *',
    $$SELECT fn_refrescar_demanda_estaciones();$$
);

-- Resumen mensual de un usuario: viajes finalizados que iniciaron en el mes,
-- minutos totales y gasto. Si no hay viajes, devuelve ceros.
CREATE OR REPLACE FUNCTION fn_resumen_mensual_usuario(
    p_id_usuario BIGINT,
    p_anio INT,
    p_mes INT
)
RETURNS TABLE (
    cantidad_viajes BIGINT,
    minutos_totales BIGINT,
    gasto_total NUMERIC
)
LANGUAGE sql STABLE AS $$
    SELECT
        COUNT(*),
        COALESCE(SUM(minutos_viaje), 0),
        COALESCE(SUM(costo), 0)
    FROM viajes
    WHERE id_usuario = p_id_usuario
      AND estado_viaje = 'Finalizado'
      AND fecha_inicio >= make_date(p_anio, p_mes, 1)
      AND fecha_inicio <  make_date(p_anio, p_mes, 1) + INTERVAL '1 month';
$$;

-- Inicia un viaje: valida al usuario y al vehículo, crea el viaje con la tarifa vigente
-- y saca el vehículo de la estación, todo en una sola transacción. Devuelve el id del viaje.
-- Las reglas de saldo, batería y viaje activo las valida trigger_validar_inicio_viaje.
CREATE OR REPLACE FUNCTION fn_iniciar_viaje(p_id_usuario BIGINT, p_id_vehiculo BIGINT)
RETURNS BIGINT
LANGUAGE plpgsql AS $$
DECLARE
    v_id_tipo_vehiculo BIGINT;
    v_id_estacion BIGINT;
    v_estado_vehiculo VARCHAR(20);
    v_id_tarifa BIGINT;
    v_id_viaje BIGINT;
BEGIN
    -- 1. Bloquear al usuario activo: serializa los inicios de un mismo usuario
    --    y evita que dos viajes simultáneos pasen la validación de viaje activo.
    PERFORM 1
    FROM usuarios
    WHERE id_usuario = p_id_usuario AND usuario_activo = TRUE
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Error: El usuario % no existe o está inactivo.', p_id_usuario;
    END IF;

    -- 2. Bloquear el vehículo: evita que dos usuarios tomen el mismo.
    SELECT id_tipo_vehiculo, id_estacion, estado_vehiculo
    INTO v_id_tipo_vehiculo, v_id_estacion, v_estado_vehiculo
    FROM vehiculos
    WHERE id_vehiculo = p_id_vehiculo
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Error: El vehículo % no existe.', p_id_vehiculo;
    END IF;

    IF v_estado_vehiculo <> 'Disponible' OR v_id_estacion IS NULL THEN
        RAISE EXCEPTION 'Error: El vehículo % no está disponible.', p_id_vehiculo;
    END IF;

    -- 3. Tarifa activa más reciente para el tipo de vehículo.
    SELECT id_tarifa
    INTO v_id_tarifa
    FROM tarifas
    WHERE id_tipo_vehiculo = v_id_tipo_vehiculo AND activa = TRUE
    ORDER BY vigente_desde DESC
    LIMIT 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Error: No hay una tarifa activa para el vehículo %.', p_id_vehiculo;
    END IF;

    -- 4. Crear el viaje (aquí se dispara el trigger de validación).
    INSERT INTO viajes (id_usuario, id_vehiculo, id_origen, id_tarifa, fecha_inicio, estado_viaje)
    VALUES (p_id_usuario, p_id_vehiculo, v_id_estacion, v_id_tarifa, NOW(), 'Activo')
    RETURNING id_viaje INTO v_id_viaje;

    -- 5. Sacar el vehículo de la estación y marcarlo en uso.
    UPDATE vehiculos
    SET estado_vehiculo = 'En uso',
        id_estacion = NULL
    WHERE id_vehiculo = p_id_vehiculo;

    RETURN v_id_viaje;
END;
$$;

-- ==================================================
-- POBLACIÓN DE TABLAS
-- ==================================================
-- Inserta datos de prueba y sincroniza las secuencias de identificadores.

BEGIN;
INSERT INTO roles (id_rol, nombre_rol) VALUES
    (1, 'Operador'),
    (2, 'Usuario');

-- Autenticación de los 10 usuarios de prueba: contraseña 123456.
-- Cada usuario tiene su propio hash BCrypt con sal aleatoria (costo 12).
-- Credenciales destinadas únicamente a pruebas locales del proyecto.
-- Joaquín Díaz permanece inactivo: su login debe devolver 401 aunque la contraseña coincida.
INSERT INTO usuarios (id_usuario, id_rol, nombre_usuario, email, password_hash, usuario_activo, fecha_registro) VALUES
    (1, 1, 'Camila Torres', 'camila.operadora@example.com', '$2b$12$.AOSg8WLAsw10VVQlQulEOAekY/IzNxg2z0IeaPrSAfFTjSZx05U2', TRUE, '2026-10-01 08:00:00'),
    (2, 1, 'Diego Rojas', 'diego.operador@example.com', '$2b$12$VieRQxDOuBcFC3GI5Ay53OsZT6YSS4H4j.p3ONlHBzdXyNr9bVGy6', TRUE, '2026-10-01 08:05:00'),
    (3, 2, 'Sofía Pérez', 'sofia.perez@example.com', '$2b$12$baopN/Pu4zyXGHoM/6SZaeO0cmtYx3s0boft/HsXGo7pZL02q5z6G', TRUE, '2026-10-02 09:00:00'),
    (4, 2, 'Matías González', 'matias.gonzalez@example.com', '$2b$12$jQp3vrzXcJyMoEC7pixOje8MSWY0ddARFxhsgf/8teiDuKaEF9eHG', TRUE, '2026-10-02 09:15:00'),
    (5, 2, 'Valentina Soto', 'valentina.soto@example.com', '$2b$12$0vlpz5edE7z47XUbf8jjBO09a6Z9UUGXDpiY.zx42rzYSUbVmWpPO', TRUE, '2026-10-03 10:00:00'),
    (6, 2, 'Benjamín Muñoz', 'benjamin.munoz@example.com', '$2b$12$cS9Z8Ti2RmcNT3wXYECg3O9HYS03TozEm9YGdiuVf27T6xbJlcyGm', TRUE, '2026-10-03 10:30:00'),
    (7, 2, 'Isidora Silva', 'isidora.silva@example.com', '$2b$12$lTkMNblRagVvPqrLrG/DFum80Xy1/nL0.LpNzXl7gLoC2ZUBJ1ceK', TRUE, '2026-10-04 11:00:00'),
    (8, 2, 'Vicente Araya', 'vicente.araya@example.com', '$2b$12$zpQ7OpTbo2M6R9ME7YsuxeZLBUH90JWWKbqcYIvyLOQ4EPfs2Ukm2', TRUE, '2026-10-04 11:20:00'),
    (9, 2, 'Catalina Reyes', 'catalina.reyes@example.com', '$2b$12$ft39IST4RYIUgHizYdVx0eo5IUrSs2UuwBBZBqkSIftMXFL3jJlPq', TRUE, '2026-10-05 12:00:00'),
    (10, 2, 'Joaquín Díaz', 'joaquin.diaz@example.com', '$2b$12$ZtAafdcmu6A9mXy1p3xCIe5TvxQQciroFDz5yCTcN6kH6CjakmROi', FALSE, '2026-10-05 12:30:00');

INSERT INTO billeteras (id_billetera, id_usuario, saldo) VALUES
    (1, 3, 17900.00),
    (2, 4, 14160.00),
    (3, 5, 18000.00),
    (4, 6, 18300.00),
    (5, 7, 18560.00),
    (6, 8, 16700.00),
    (7, 9, -340.00),
    (8, 10, 15900.00);

INSERT INTO estaciones (id_estacion, nombre_estacion, direccion, latitud, longitud, capacidad, estacion_activa) VALUES
    (1, 'Plaza de Armas', 'Plaza de Armas 100, Santiago', -33.437200, -70.650600, 20, TRUE),
    (2, 'Universidad de Chile', 'Alameda 1058, Santiago', -33.443000, -70.651800, 15, TRUE),
    (3, 'Parque Forestal', 'Ismael Valdés Vergara 500, Santiago', -33.434800, -70.643200, 18, TRUE),
    (4, 'Baquedano', 'Providencia 100, Providencia', -33.437100, -70.634400, 25, TRUE),
    (5, 'Los Leones', 'Nueva Providencia 2200, Providencia', -33.420700, -70.609500, 20, TRUE),
    (6, 'Parque Bustamante', 'General Bustamante 120, Providencia', -33.442700, -70.631500, 12, TRUE);

INSERT INTO tipos_vehiculos (id_tipo_vehiculo, nombre_tipo_vehiculo) VALUES
    (1, 'Bicicleta'),
    (2, 'Scooter');

INSERT INTO vehiculos
    (id_vehiculo, id_tipo_vehiculo, id_estacion, codigo_vehiculo, nivel_bateria, fecha_alta, estado_vehiculo)
VALUES
    (1, 1, 4, 'BICI-001', NULL, '2026-10-01 08:00:00', 'Disponible'),
    (2, 1, 5, 'BICI-002', NULL, '2026-10-01 08:00:00', 'Disponible'),
    (3, 1, 6, 'BICI-003', NULL, '2026-10-01 08:00:00', 'Disponible'),
    (4, 1, 4, 'BICI-004', NULL, '2026-10-01 08:00:00', 'Disponible'),
    (5, 1, 1, 'BICI-005', NULL, '2026-10-01 08:00:00', 'Disponible'),
    (6, 1, NULL, 'BICI-006', NULL, '2026-10-01 08:00:00', 'En uso'),
    (7, 1, 3, 'BICI-007', NULL, '2026-10-01 08:00:00', 'En mantención'),
    (8, 1, NULL, 'BICI-008', NULL, '2026-10-01 08:00:00', 'Dado de baja'),
    (9, 2, 4, 'SCOOT-001', 75, '2026-10-01 09:00:00', 'Disponible'),
    (10, 2, 5, 'SCOOT-002', 62, '2026-10-01 09:00:00', 'Disponible'),
    (11, 2, 1, 'SCOOT-003', 48, '2026-10-01 09:00:00', 'Disponible'),
    (12, 2, 5, 'SCOOT-004', 15, '2026-10-01 09:00:00', 'Disponible'),
    (13, 2, 2, 'SCOOT-005', 55, '2026-10-01 09:00:00', 'Disponible'),
    (14, 2, NULL, 'SCOOT-006', 80, '2026-10-01 09:00:00', 'En uso'),
    (15, 2, 2, 'SCOOT-007', 100, '2026-10-01 09:00:00', 'Disponible'),
    (16, 2, 6, 'SCOOT-008', 95, '2026-10-01 09:00:00', 'Disponible');

INSERT INTO ordenes_rebalanceo
    (id_orden, id_estacion_origen, id_estacion_destino, id_operador, cantidad_vehiculos, fecha_orden, estado_orden)
VALUES
    (1, 2, 4, 1, 2, '2026-10-19 10:00:00', 'Completada'),
    (2, 3, 5, 2, 2, '2026-10-19 11:00:00', 'Completada'),
    (3, 1, 6, 1, 1, '2026-10-19 11:30:00', 'Cancelada');

INSERT INTO detalles_rebalanceo (id_orden, id_vehiculo) VALUES
    (1, 1),
    (1, 4),
    (2, 2),
    (2, 12),
    (3, 5);

INSERT INTO auditorias_vehiculos
    (id_auditoria, id_vehiculo, id_operador, estado_anterior, estado_nuevo, fecha_auditoria, motivo)
VALUES
    (1, 1, 1, 'Disponible', 'En mantención', '2026-10-13 10:00:00', 'Mantención preventiva programada de rodamientos.'),
    (2, 16, 2, 'Disponible', 'En mantención', '2026-10-18 09:00:00', 'Revisión del sistema eléctrico y recarga profunda de batería.'),
    (3, 7, 1, 'Disponible', 'En mantención', '2026-10-18 14:00:00', 'Ajuste de frenos y cambio de cadena.'),
    (4, 8, 2, 'Disponible', 'Dado de baja', '2026-10-18 15:00:00', 'Daño estructural en el marco; vehículo retirado del servicio.');

INSERT INTO tarifas
    (id_tarifa, id_tipo_vehiculo, valor_desbloqueo, valor_minuto, vigente_desde, activa)
VALUES
    (1, 1, 0.00, 100.00, '2026-10-01 00:00:00', FALSE),
    (2, 2, 500.00, 150.00, '2026-10-01 00:00:00', FALSE),
    (3, 1, 0.00, 120.00, '2026-10-17 00:00:00', TRUE),
    (4, 2, 600.00, 180.00, '2026-10-17 00:00:00', TRUE);

INSERT INTO viajes
    (id_viaje, id_usuario, id_vehiculo, id_origen, id_destino, id_tarifa,
     fecha_inicio, fecha_fin, minutos_viaje, costo, estado_viaje)
VALUES
    (1, 3, 1, 1, 2, 1, '2026-10-15 09:00:00', '2026-10-15 09:15:00', 15, 1500.00, 'Finalizado'),
    (2, 4, 9, 3, 4, 2, '2026-10-15 10:00:00', '2026-10-15 10:10:00', 10, 2000.00, 'Finalizado'),
    (3, 5, 2, 2, 3, 1, '2026-10-16 09:00:00', '2026-10-16 09:20:00', 20, 2000.00, 'Finalizado'),
    (4, 6, 10, 4, 5, 2, '2026-10-16 10:00:00', '2026-10-16 10:08:00', 8, 1700.00, 'Finalizado'),
    (5, 7, 3, 1, 6, 3, '2026-10-17 09:00:00', '2026-10-17 09:12:00', 12, 1440.00, 'Finalizado'),
    (6, 8, 11, 2, 1, 4, '2026-10-17 10:00:00', '2026-10-17 10:15:00', 15, 3300.00, 'Finalizado'),
    (7, 9, 4, 3, 2, 3, '2026-10-18 09:00:00', '2026-10-18 09:07:00', 7, 840.00, 'Finalizado'),
    (8, 10, 12, 5, 3, 4, '2026-10-18 10:00:00', '2026-10-18 10:25:00', 25, 5100.00, 'Finalizado'),
    (9, 3, 5, 4, 1, 3, '2026-10-19 08:00:00', '2026-10-19 08:05:00', 5, 600.00, 'Finalizado'),
    (10, 4, 13, 6, 2, 4, '2026-10-19 08:30:00', '2026-10-19 08:48:00', 18, 3840.00, 'Finalizado'),
    (11, 5, 6, 1, NULL, 3, '2026-10-19 09:00:00', NULL, NULL, NULL, 'Activo'),
    (12, 6, 14, 4, NULL, 4, '2026-10-19 09:10:00', NULL, NULL, NULL, 'Activo');

INSERT INTO movimientos_billeteras
    (id_movimiento, id_billetera, id_viaje, tipo_movimiento, monto, fecha_movimiento)
VALUES
    (1, 1, NULL, 'Recarga', 20000.00, '2026-10-14 08:00:00'),
    (2, 2, NULL, 'Recarga', 20000.00, '2026-10-14 08:10:00'),
    (3, 3, NULL, 'Recarga', 20000.00, '2026-10-14 08:20:00'),
    (4, 4, NULL, 'Recarga', 20000.00, '2026-10-14 08:30:00'),
    (5, 5, NULL, 'Recarga', 20000.00, '2026-10-14 08:40:00'),
    (6, 6, NULL, 'Recarga', 20000.00, '2026-10-14 08:50:00'),
    (7, 7, NULL, 'Recarga', 500.00,   '2026-10-14 09:00:00'),
    (8, 8, NULL, 'Recarga', 20000.00, '2026-10-14 09:10:00'),
    (9, 1, 1, 'Cobro_viaje', 1500.00, '2026-10-15 09:15:00'),
    (10, 2, 2, 'Cobro_viaje', 2000.00, '2026-10-15 10:10:00'),
    (11, 3, 3, 'Cobro_viaje', 2000.00, '2026-10-16 09:20:00'),
    (12, 4, 4, 'Cobro_viaje', 1700.00, '2026-10-16 10:08:00'),
    (13, 5, 5, 'Cobro_viaje', 1440.00, '2026-10-17 09:12:00'),
    (14, 6, 6, 'Cobro_viaje', 3300.00, '2026-10-17 10:15:00'),
    (15, 7, 7, 'Cobro_viaje', 840.00,  '2026-10-18 09:07:00'),
    (16, 8, 8, 'Cobro_viaje', 5100.00, '2026-10-18 10:25:00'),
    (17, 1, 9, 'Cobro_viaje', 600.00,  '2026-10-19 08:05:00'),
    (18, 2, 10, 'Cobro_viaje', 3840.00, '2026-10-19 08:48:00'),
    (19, 8, NULL, 'Ajuste', 1000.00, '2026-10-19 10:05:00');

SELECT setval(pg_get_serial_sequence('roles', 'id_rol'), MAX(id_rol)) FROM roles;
SELECT setval(pg_get_serial_sequence('usuarios', 'id_usuario'), MAX(id_usuario)) FROM usuarios;
SELECT setval(pg_get_serial_sequence('billeteras', 'id_billetera'), MAX(id_billetera)) FROM billeteras;
SELECT setval(pg_get_serial_sequence('estaciones', 'id_estacion'), MAX(id_estacion)) FROM estaciones;
SELECT setval(pg_get_serial_sequence('tipos_vehiculos', 'id_tipo_vehiculo'), MAX(id_tipo_vehiculo)) FROM tipos_vehiculos;
SELECT setval(pg_get_serial_sequence('vehiculos', 'id_vehiculo'), MAX(id_vehiculo)) FROM vehiculos;
SELECT setval(pg_get_serial_sequence('ordenes_rebalanceo', 'id_orden'), MAX(id_orden)) FROM ordenes_rebalanceo;
SELECT setval(pg_get_serial_sequence('auditorias_vehiculos', 'id_auditoria'), MAX(id_auditoria)) FROM auditorias_vehiculos;
SELECT setval(pg_get_serial_sequence('tarifas', 'id_tarifa'), MAX(id_tarifa)) FROM tarifas;
SELECT setval(pg_get_serial_sequence('viajes', 'id_viaje'), MAX(id_viaje)) FROM viajes;
SELECT setval(pg_get_serial_sequence('movimientos_billeteras', 'id_movimiento'), MAX(id_movimiento)) FROM movimientos_billeteras;

-- Refrescar la vista materializada para reflejar inmediatamente los viajes insertados
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_demanda_estacion_horaria;

COMMIT;