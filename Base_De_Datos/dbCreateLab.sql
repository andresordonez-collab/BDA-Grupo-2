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


-- Guarda los tokens de sesión asociados a los usuarios.
CREATE TABLE refresh_tokens(
	id_refresh_token BIGSERIAL PRIMARY KEY, 
	id_usuario BIGINT NOT NULL, 
	token_hash VARCHAR(255) UNIQUE NOT NULL, 
	expira_en TIMESTAMP NOT NULL, 
	revocado BOOLEAN NOT NULL DEFAULT FALSE,
	FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
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

SELECT * FROM usuarios
