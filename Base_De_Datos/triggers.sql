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