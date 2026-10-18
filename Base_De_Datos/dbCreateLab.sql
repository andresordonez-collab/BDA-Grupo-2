CREATE TABLE roles(
	id_rol BIGSERIAL PRIMARY KEY, 
	nombre_rol VARCHAR(50) UNIQUE NOT NULL
);

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


CREATE TABLE refresh_tokens(
	id_refresh_token BIGSERIAL PRIMARY KEY, 
	id_usuario BIGINT NOT NULL, 
	token_hash VARCHAR(255) UNIQUE NOT NULL, 
	expira_en TIMESTAMP NOT NULL, 
	revocado BOOLEAN NOT NULL DEFAULT FALSE,
	FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE billeteras(
	id_billetera BIGSERIAL PRIMARY KEY,
	id_usuario BIGINT UNIQUE NOT NULL,
	saldo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
	FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE viajes(
	id_viaje BIGSERIAL PRIMARY KEY,
	id_usuario BIGINT NOT NULL,
	id_vehiculo BIGINT NOT NULL,
	id_origen BIGINT NOT NULL,
	id_destino BIGINT,
	id_tarifa BIGINT NOT NULL,
	fecha_inicio TIMESTAMP NOT NULL DEFAULT NOW(),
	fecha_fin TIMESTAMP CHECK (fecha_fin >= fecha_inicio),
	minutos_viaje INT,
	costo DECIMAL(12,2),
	estado_viaje VARCHAR(20) NOT NULL CHECK (estado_viaje IN ('activo', 'finalizado')),
);

CREATE TABLE movimientos_billeteras(
	id_movimiento BIGSERIAL PRIMARY KEY,
	id_billetera BIGINT NOT NULL,
	id_viaje BIGINT NOT NULL,
	tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('recarga', 'cobro_viaje', 'ajuste')),
	monto DECIMAL(12,2) NOT NULL,
	fecha_movimiento TIMESTAMP NOT NULL DEFAULT NOW(),
	FOREIGN KEY (id_billetera) REFERENCES billeteras(id_billetera),
	FOREIGN KEY (id_viaje) REFERENCES viajes(id_viaje)
);

