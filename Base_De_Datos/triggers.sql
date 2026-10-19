CREATE OR REPLACE FUNCTION validar_inicio_viaje()
RETURNS TRIGGER
AS
$BODY$

DECLARE
    v_saldo DECIMAL(12,2);
    v_bateria INT;

BEGIN
    SELECT saldo INTO v_saldo
    FROM billeteras
    WHERE id_usuario = NEW.id_usuario;

    SELECT nivel_bateria INTO v_bateria
    FROM vehiculos
    WHERE id_vehiculo = NEW.id_vehiculo;

    IF v_saldo < 0 THEN
        RAISE EXCEPTION 'Saldo negativo, no se puede iniciar el viaje.';
    END IF;

    IF v_bateria < 20 THEN
        RAISE EXCEPTION 'Nivel de batería insuficiente, no se puede iniciar el viaje.';
    END IF;

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
EXECUTE FUNCTION validar_inicio_viaje();