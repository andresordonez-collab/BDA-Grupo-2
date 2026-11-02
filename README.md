# Sistema de Arriendo de Bicicletas y Scooters Compartidos

Proyecto del **Grupo 2 — Taller de Bases de Datos, Universidad de Santiago de Chile**. Permite administrar estaciones, vehículos, usuarios, billeteras, tarifas y viajes.

## 1. Arquitectura

El sistema utiliza una arquitectura multicapa:

| Capa | Responsabilidad |
|---|---|
| Frontend React | Interfaces de Usuario y Operador; consume la API con Axios |
| Controllers y DTO | Reciben solicitudes HTTP y entregan respuestas JSON |
| Services | Validaciones, permisos y coordinación de transacciones |
| Repositories | Consultas SQL con JdbcTemplate y RowMapper, sin ORM |
| PostgreSQL | Persistencia, funciones, procedimientos, triggers y reportes |

Docker Compose ejecuta tres servicios: `frontend`, `backend` y `postgres-db`. Vite reenvía `/api` a `backend:8080`; el backend se conecta a `postgres-db:5432`. Los datos se conservan en el volumen `pgdata`.

La autenticación utiliza JWT en la cookie HttpOnly `access_token`. El filtro valida el token y consulta el estado y rol vigentes del usuario. El rol **Usuario** opera sus propios viajes; **Operador** administra el sistema.

## 2. Tecnologías

- **Backend:** Java 21, Spring Boot 4.0.8, Spring Security, Spring JDBC, JJWT y BCrypt.
- **Frontend:** React 19, React Router, Material UI, Axios, Vite 8 y Node.js 22.
- **Base de datos:** PostgreSQL 16, PL/pgSQL y pg_cron.
- **Herramientas:** Docker Compose, Maven Wrapper, npm y Postman.

## 3. Instalación y despliegue

### Requisitos

Docker con Compose v2 y Git. Durante la primera construcción se necesita Internet para descargar imágenes y dependencias. Java, Node.js y PostgreSQL se ejecutan dentro de los contenedores.

### Advertencia:

Debe tener en su computador los puertos 8080, 5173 y 5432 libres.

### Paso 1: descargar el proyecto

```bash
git clone https://github.com/andresordonez-collab/BDA-Grupo-2.git
cd BDA-Grupo-2
```

Si recibiste un ZIP, descomprímelo y abre la terminal en la carpeta que contiene `docker-compose.yaml`.

### Paso 2: configurar las variables

```bash
cp .env.example .env
```

Revisa los valores de la siguiente sección. En PowerShell utiliza `Copy-Item .env.example .env`.

### Paso 3: construir e iniciar

```bash
docker compose up -d --build
```

PostgreSQL carga `Base_De_Datos/DB.sql` al inicializar un volumen vacío. El script crea las tablas, relaciones, lógica de negocio, vista materializada y datos de prueba. El backend se compila dentro de Docker y espera a que PostgreSQL esté saludable.

### Paso 4: verificar el arranque

```bash
docker compose ps
docker compose logs --tail=100 backend
```

Espera a que Spring Boot termine de iniciar. Con los puertos predeterminados:

- **Frontend:** http://localhost:5173
- **API:** http://localhost:8080/api
- **PostgreSQL:** localhost:5432

### Paso 5: ingresar

| Rol | Correo | Contraseña |
|---|---|---|
| Operador | `diego.operador@example.com` | `123456` |
| Usuario | `sofia.perez@example.com` | `123456` |

Para detener los servicios conservando los datos usa `docker compose down`. Para incorporar cambios de código ejecuta nuevamente `docker compose up -d --build`.

El script SQL solo se carga automáticamente cuando el volumen está vacío; reconstruir las imágenes no reinicializa una BD existente. El despliegue descrito es local, con HTTP y Vite en modo desarrollo.

## 4. Variables de entorno

Se definen en el archivo `.env` de la raíz:

| Variable | Valor de ejemplo | Descripción |
|---|---|---|
| `DB_NAME` | `DBScootersAndBicycles` | Nombre de la base de datos |
| `DB_USERNAME` | `chuy` | Usuario PostgreSQL |
| `DB_PASSWORD` | `123456` | Contraseña PostgreSQL de demostración |
| `DB_PORT` | `5432` | Puerto publicado de PostgreSQL |
| `BACKEND_PORT` | `8080` | Puerto publicado de la API |
| `FRONTEND_PORT` | `5173` | Puerto publicado del frontend |
| `JWT_SECRET` | Clave larga incluida en `.env.example` | Clave de firma JWT, mínimo 32 bytes |
| `JWT_EXPIRATION_MS` | `86400000` | Vigencia del JWT en milisegundos; 24 horas |

Compose fija internamente `DB_HOST=postgres-db` y `DB_PORT=5432`. La plantilla también contiene `VITE_API_URL` y `CORS_ALLOWED_ORIGINS`, pero el cliente actual utiliza rutas relativas y el proxy de Vite, no esas variables. Para probar el sistema real, deja `VITE_USE_MOCKS` sin definir o en `false` en el frontend.

La cookie usa `Secure=false` por defecto para HTTP local. `DB_PASSWORD` no es la contraseña de login de los usuarios de la aplicación.

## 5. API: las diez funcionalidades

Las rutas se expresan respecto a `http://localhost:8080`. Las tareas de triggers e índices se ejecutan en PostgreSQL y no necesitan un endpoint propio.

| Tarea | Implementación y rutas principales | Acceso |
|---|---|---|
| **1. CRUD de entidades** | Rutas administrativas indicadas en la tabla siguiente | Operador; lecturas compartidas de estaciones, tarifas y tipos para cualquier sesión |
| **2. JWT y roles** | `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`; `POST /api/viajes` inicia un viaje y `GET /api/viajes/mis-viajes` consulta el historial propio | Login/logout públicos; operaciones de viaje para Usuario |
| **3. Finalizar viaje** | `POST /api/viajes/{id}/finalizar` llama a `finalizar_viaje`: calcula minutos/costo, descuenta saldo y ancla el vehículo en una transacción | Usuario propietario |
| **4. Rebalanceo** | `POST /api/rebalanceos` llama a `rebalancear_flota`: traslada vehículos del tipo solicitado y registra orden y detalles | Operador |
| **5. Validar inicio** | Trigger `validar_inicio_viaje`: rechaza saldo negativo, otro viaje activo o Scooter con batería nula/inferior al 20 % | Automático al crear un viaje Activo |
| **6. Auditar estados** | `PATCH /api/vehiculos/{id}/estado` prepara operador/motivo para el trigger; `GET /api/auditorias-vehiculos` consulta los registros | Operador |
| **7. Demanda por estación/hora** | `GET /api/reportes/demanda-estacion-horaria`, sobre `mv_demanda_estacion_horaria`; pg_cron ejecuta el refresco al minuto cero de cada hora | Operador |
| **8. Índices** | Índices sobre `vehiculos.estado_vehiculo`, `viajes.id_usuario` y `viajes.fecha_inicio` | Se crean con `DB.sql` |
| **9. Disponibilidad** | `GET /api/estaciones/disponibilidad`: cantidades por tipo y batería promedio de scooters; `GET /api/estaciones/{id}/vehiculos-disponibles` | Autenticado |
| **10. Resumen mensual** | `GET /api/viajes/resumen-mensual?anio=2026&mes=10`; cálculo en `fn_resumen_mensual_usuario`. Operador: `GET /api/usuarios/{id}/resumen-mensual` | Usuario propio u Operador en su ruta administrativa |

El resumen cuenta viajes Finalizados cuya fecha de inicio pertenece al mes consultado. Si se omiten año o mes se usa el componente actual del backend. El reporte de demanda acumula viajes por hora del día y se actualiza con el refresco, no en cada petición.

### Rutas CRUD

En las rutas estándar: **GET** lista, **POST** crea, y **GET/PUT/DELETE `/{id}`** consulta, actualiza o elimina un registro.

| Entidad | Ruta base |
|---|---|
| Roles | `/api/roles` |
| Tipos de vehículos | `/api/tipos-vehiculos` |
| Usuarios | `/api/usuarios` |
| Estaciones | `/api/estaciones` |
| Vehículos | `/api/vehiculos` |
| Tarifas | `/api/tarifas` |
| Billeteras | `/api/billeteras` |
| Movimientos | `/api/movimientos-billeteras` |
| Órdenes de rebalanceo | `/api/ordenes-rebalanceo` |
| Auditorías | `/api/auditorias-vehiculos` |

**Viajes:** `GET /api/viajes`, `GET/PUT/DELETE /api/viajes/{id}` y `POST /api/viajes/administracion`. El `POST /api/viajes` sin sufijo es el inicio de viaje del Usuario, no el alta administrativa.

**Detalles:** `GET/POST /api/detalles-rebalanceo`, `GET /api/detalles-rebalanceo/orden/{idOrden}` y `GET/DELETE /api/detalles-rebalanceo/orden/{idOrden}/vehiculo/{idVehiculo}`. No tienen PUT: solo contienen la clave primaria compuesta.

**Billetera propia:** `GET /api/billeteras/mi-billetera?limite=20`, para Usuario, con saldo y movimientos recientes.

POST devuelve **201**, GET/PUT/PATCH **200** y DELETE **204**. Finalización, rebalanceo y logout devuelven **204**. Los errores habituales son **400** por datos inválidos, **401** por autenticación, **403** por permisos, **404** por registro inexistente y **409** por restricciones o rechazos de negocio.

## 6. Ejemplos JSON

En Postman usa **Body → raw → JSON**, conserva la cookie `access_token` y utiliza siempre el mismo host. No se necesita Bearer Token. Los IDs de ejemplo deben reemplazarse por registros existentes.

### Autenticación

`POST /api/auth/login`:

```json
{
  "email": "diego.operador@example.com",
  "password": "123456"
}
```

Devuelve el perfil y el JWT mediante `Set-Cookie`. El frontend también puede cifrar la contraseña con RSA-OAEP/SHA-256 usando `GET /api/auth/clave-publica` y enviar `passwordCifrada` en lugar de `password`. No se deben enviar ambas.

### Crear usuario — Operador

`POST /api/usuarios`:

```json
{
  "idRol": 2,
  "nombreUsuario": "Usuario Demo",
  "email": "demo@example.com",
  "passwordHash": "123456",
  "usuarioActivo": true
}
```

El servicio codifica la contraseña con BCrypt y no la devuelve. El trigger crea automáticamente la billetera del rol Usuario con saldo cero.

### Crear vehículo — Operador

`POST /api/vehiculos`:

```json
{
  "idTipoVehiculo": 2,
  "idEstacion": 1,
  "codigoVehiculo": "DEMO-SCOOTER-001",
  "nivelBateria": 100,
  "estadoVehiculo": "Disponible"
}
```

Un vehículo debe tener estación, salvo cuando está En uso. Las bicicletas requieren batería null; los scooters requieren un valor entre 0 y 100.

### Iniciar y finalizar viaje — Usuario

Inicia sesión con un Usuario y envía `POST /api/viajes`:

```json
{"idVehiculo": 1}
```

Respuesta de ejemplo:

```json
{"idViaje": 9}
```

Usa el ID real devuelto en `POST /api/viajes/{id}/finalizar`:

```json
{"idEstacionDestino": 2}
```

El usuario debe tener billetera, saldo no negativo y ningún otro viaje Activo; el vehículo debe estar disponible y anclado, con tarifa activa. El destino debe tener cupo. Para verificar el resultado, consulta el historial, la billetera propia y el resumen mensual.

### Rebalancear — Operador

`POST /api/rebalanceos`:

```json
{
  "idEstacionOrigen": 1,
  "idEstacionDestino": 2,
  "cantidadVehiculos": 1,
  "idTipoVehiculo": 1
}
```

El operador se obtiene del JWT. Debe haber vehículos disponibles del tipo indicado y capacidad en el destino.

### Cambiar estado — Operador

`PATCH /api/vehiculos/{id}/estado`:

```json
{
  "estadoVehiculo": "En mantención",
  "motivo": "Revisión de frenos"
}
```

El motivo es obligatorio para mantención o baja. El operador se obtiene de la sesión y el trigger registra la auditoría. El PATCH no permite asignar En uso ni modificar el estado de un vehículo que está en viaje.

Los CRUD administrativos modifican registros directamente: crear una orden no ejecuta el rebalanceo, finalizar un viaje mediante PUT no cobra y crear un movimiento no actualiza automáticamente el saldo. Para esas acciones utiliza los endpoints de negocio descritos arriba.
