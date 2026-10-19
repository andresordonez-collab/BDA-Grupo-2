-- Datos ficticios para el Laboratorio 1 de Bases de Datos Avanzadas (PostgreSQL).
-- Compatible con el dbCreateLab.sql.
-- Ejecutar sobre tablas vacías, después de dbCreateLab1.sql.


INSERT INTO roles (id_rol, nombre_rol) VALUES
    (1, 'Operador'),
    (2, 'Usuario');

INSERT INTO usuarios (id_usuario, id_rol, nombre_usuario, email, password_hash, usuario_activo, fecha_registro) VALUES
    (1, 1, 'Camila Torres', 'camila.operadora@example.com', 'DUMMY_HASH_OPERADOR_001', TRUE, '2026-10-01 08:00:00'),
    (2, 1, 'Diego Rojas', 'diego.operador@example.com', 'DUMMY_HASH_OPERADOR_002', TRUE, '2026-10-01 08:05:00'),
    (3, 2, 'Sofía Pérez', 'sofia.perez@example.com', 'DUMMY_HASH_USUARIO_003', TRUE, '2026-10-02 09:00:00'),
    (4, 2, 'Matías González', 'matias.gonzalez@example.com', 'DUMMY_HASH_USUARIO_004', TRUE, '2026-10-02 09:15:00'),
    (5, 2, 'Valentina Soto', 'valentina.soto@example.com', 'DUMMY_HASH_USUARIO_005', TRUE, '2026-10-03 10:00:00'),
    (6, 2, 'Benjamín Muñoz', 'benjamin.munoz@example.com', 'DUMMY_HASH_USUARIO_006', TRUE, '2026-10-03 10:30:00'),
    (7, 2, 'Isidora Silva', 'isidora.silva@example.com', 'DUMMY_HASH_USUARIO_007', TRUE, '2026-10-04 11:00:00'),
    (8, 2, 'Vicente Araya', 'vicente.araya@example.com', 'DUMMY_HASH_USUARIO_008', TRUE, '2026-10-04 11:20:00'),
    (9, 2, 'Catalina Reyes', 'catalina.reyes@example.com', 'DUMMY_HASH_USUARIO_009', TRUE, '2026-10-05 12:00:00'),
    (10, 2, 'Joaquín Díaz', 'joaquin.diaz@example.com', 'DUMMY_HASH_USUARIO_010', FALSE, '2026-10-05 12:30:00');

-- Tokens vigentes, revocados y vencidos según la fecha de referencia.
INSERT INTO refresh_tokens (id_refresh_token, id_usuario, token_hash, expira_en, revocado) VALUES
    (1, 1, 'DUMMY_TOKEN_HASH_001', '2026-10-26 08:00:00', FALSE),
    (2, 3, 'DUMMY_TOKEN_HASH_003', '2026-10-25 09:00:00', FALSE),
    (3, 4, 'DUMMY_TOKEN_HASH_004', '2026-10-25 10:00:00', FALSE),
    (4, 5, 'DUMMY_TOKEN_HASH_005', '2026-10-26 09:00:00', FALSE),
    (5, 9, 'DUMMY_TOKEN_HASH_009', '2026-10-18 12:00:00', FALSE),
    (6, 10, 'DUMMY_TOKEN_HASH_010', '2026-10-25 12:30:00', TRUE);


INSERT INTO billeteras (id_billetera, id_usuario, saldo) VALUES
    (1, 3, 17900.00),
    (2, 4, 14160.00),
    (3, 5, 18000.00),
    (4, 6, 18300.00),
    (5, 7, 18560.00),
    (6, 8, 16700.00),
    (7, 9, 19660.00),
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
    (12, 2, 5, 'SCOOT-004', 35, '2026-10-01 09:00:00', 'Disponible'),
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
    (1, 1, 1, 'En mantención', 'Disponible', '2026-10-14 08:00:00', 'Mantención preventiva terminada.'),
    (2, 7, 1, 'Disponible', 'En mantención', '2026-10-18 14:00:00', 'Ajuste de frenos y cambio de cadena.'),
    (3, 8, 2, 'Disponible', 'Dado de baja', '2026-10-18 15:00:00', 'Daño estructural; vehículo retirado del servicio.'),
    (4, 16, 2, 'En mantención', 'Disponible', '2026-10-19 07:30:00', 'Revisión eléctrica terminada y batería cargada.'),
    (5, 6, 1, 'Disponible', 'En uso', '2026-10-19 09:00:00', 'Inicio de viaje supervisado por operador.'),
    (6, 14, 2, 'Disponible', 'En uso', '2026-10-19 09:10:00', 'Inicio de viaje supervisado por operador.');

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
    (7, 7, NULL, 'Recarga', 20000.00, '2026-10-14 09:00:00'),
    (8, 8, NULL, 'Recarga', 20000.00, '2026-10-14 09:10:00'),
    (9, 1, 1, 'Cobro_viaje', 1500.00, '2026-10-15 09:15:00'),
    (10, 2, 2, 'Cobro_viaje', 2000.00, '2026-10-15 10:10:00'),
    (11, 3, 3, 'Cobro_viaje', 2000.00, '2026-10-16 09:20:00'),
    (12, 4, 4, 'Cobro_viaje', 1700.00, '2026-10-16 10:08:00'),
    (13, 5, 5, 'Cobro_viaje', 1440.00, '2026-10-17 09:12:00'),
    (14, 6, 6, 'Cobro_viaje', 3300.00, '2026-10-17 10:15:00'),
    (15, 7, 7, 'Cobro_viaje', 840.00, '2026-10-18 09:07:00'),
    (16, 8, 8, 'Cobro_viaje', 5100.00, '2026-10-18 10:25:00'),
    (17, 1, 9, 'Cobro_viaje', 600.00, '2026-10-19 08:05:00'),
    (18, 2, 10, 'Cobro_viaje', 3840.00, '2026-10-19 08:48:00'),
    (19, 7, NULL, 'Ajuste', 500.00, '2026-10-19 10:00:00'),
    (20, 8, NULL, 'Ajuste', 1000.00, '2026-10-19 10:05:00');


-- Sincronización de secuencias BIGSERIAL al utilizar IDs explícitos.
SELECT setval(pg_get_serial_sequence('roles', 'id_rol'), MAX(id_rol)) FROM roles;
SELECT setval(pg_get_serial_sequence('usuarios', 'id_usuario'), MAX(id_usuario)) FROM usuarios;
SELECT setval(pg_get_serial_sequence('refresh_tokens', 'id_refresh_token'), MAX(id_refresh_token)) FROM refresh_tokens;
SELECT setval(pg_get_serial_sequence('billeteras', 'id_billetera'), MAX(id_billetera)) FROM billeteras;
SELECT setval(pg_get_serial_sequence('estaciones', 'id_estacion'), MAX(id_estacion)) FROM estaciones;
SELECT setval(pg_get_serial_sequence('tipos_vehiculos', 'id_tipo_vehiculo'), MAX(id_tipo_vehiculo)) FROM tipos_vehiculos;
SELECT setval(pg_get_serial_sequence('vehiculos', 'id_vehiculo'), MAX(id_vehiculo)) FROM vehiculos;
SELECT setval(pg_get_serial_sequence('ordenes_rebalanceo', 'id_orden'), MAX(id_orden)) FROM ordenes_rebalanceo;
SELECT setval(pg_get_serial_sequence('auditorias_vehiculos', 'id_auditoria'), MAX(id_auditoria)) FROM auditorias_vehiculos;
SELECT setval(pg_get_serial_sequence('tarifas', 'id_tarifa'), MAX(id_tarifa)) FROM tarifas;
SELECT setval(pg_get_serial_sequence('viajes', 'id_viaje'), MAX(id_viaje)) FROM viajes;
SELECT setval(pg_get_serial_sequence('movimientos_billeteras', 'id_movimiento'), MAX(id_movimiento)) FROM movimientos_billeteras;

COMMIT;

