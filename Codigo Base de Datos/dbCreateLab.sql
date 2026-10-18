CREATE TABLE roles(id_rol BIGSERIAL PRIMARY KEY, nombre_rol VARCHAR(50) UNIQUE NOT NULL);

CREATE TABLE usuarios(id_usuario BIGSERIAL PRIMARY KEY,
	id_rol BIGINT NOT NULL,
	nombre_usuario VARCHAR(100) NOT NULL,
	email VARCHAR(100) NOT NULL UNIQUE,
	password_hash VARCHAR(200) NOT NULL,
	usuario_activo BOOLEAN NOT NULL DEFAULT TRUE,
	fecha_registro TIMESTAMP NOT NULL DEFAULT NOW(),
	FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);


CREATE TABLE refresh_tokens(id_refresh_token BIGSERIAL PRIMARY KEY, 
	id_usuario BIGINT NOT NULL, 
	token_hash VARCHAR(255) UNIQUE NOT NULL, 
	expira_en TIMESTAMP NOT NULL, 
	revocado BOOLEAN NOT NULL DEFAULT FALSE,
	FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

