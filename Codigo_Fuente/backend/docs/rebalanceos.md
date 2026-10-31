# Integración PA2: rebalancear_flota

`POST /api/rebalanceos`, `Content-Type: application/json`:

```json
{
  "idEstacionOrigen": 1,
  "idEstacionDestino": 2,
  "idOperador": 1,
  "cantidadVehiculos": 2
}
```

Los identificadores son `Long` (BIGINT) y la cantidad `Integer` (INT).
Se exige presencia de los cuatro campos y números JSON enteros dentro de rango
(sin coerción de texto o decimales); PostgreSQL conserva todas las reglas
sobre cantidad, estaciones, operador, disponibilidad y capacidad. No hay consultas
previas ni reglas de negocio duplicadas en Java.

El repositorio ejecuta `CALL rebalancear_flota(?, ?, ?, ?)` mediante un
PreparedStatement de JdbcTemplate con parámetros tipados. Una sola llamada ejecuta
los movimientos y la orden de forma atómica según el procedimiento oficial de
`Base_De_Datos/DB.sql`. No se aplica una transacción Java con aislamiento propio;
la configuración de la conexión debe permitir READ COMMITTED (predeterminado de
PostgreSQL) o SERIALIZABLE. REPEATABLE READ se considera un fallo interno.
El procedimiento no devuelve ID ni parámetros OUT: el endpoint no inventa un ID.

| HTTP | Resultado |
| --- | --- |
| 204 | Llamada completada; sin cuerpo |
| 400 | JSON ilegible, valores fuera de rango o campos ausentes/nulos |
| 409 | Rechazo de negocio conocido emitido por el procedimiento |
| 500 | Error interno, conectividad, aislamiento o SQL no reconocido |

Los errores tienen `codigo` y `mensaje`. No se expone el texto SQL, credenciales,
contexto PL/pgSQL ni stack traces al cliente. Los errores internos se registran en
el servidor. El advice se limita a RebalanceoController.

El SQL oficial emite P0001 para sus RAISE EXCEPTION, incluido el fallo de aislamiento.
El servicio reconoce SQLSTATE P0001 junto con los prefijos de mensajes de negocio
actuales. Estos prefijos solo clasifican errores ya emitidos; no validan datos.
Cambios futuros en los mensajes oficiales requieren revisar esta clasificación;
los mensajes desconocidos se tratan como errores internos.

## Integración JWT pendiente

`idOperador` es un dato proporcionado por el cliente y **no acredita identidad**.
La verificación de rol/actividad del procedimiento tampoco autentica al solicitante.
El compañero responsable de JWT debe proteger esta ruta, autorizar al principal y
obtener o contrastar idOperador con la identidad autenticada para evitar suplantación.
Esta implementación no altera Spring Security ni incorpora autenticación.
Con la seguridad automática actual de Spring, las peticiones pueden requerir
credenciales y CSRF antes de alcanzar el controlador; no se agrega permitAll ni
se desactiva CSRF. Los tests MockMvc standalone prueban el contrato MVC y el
servicio, sin filtros de seguridad, y no acreditan la integración JWT.

## Pruebas

Desde `Codigo_Fuente/backend`:

```bash
bash mvnw -B -Dtest=RebalanceoRepositoryTest,RebalanceoEndpointTest test
bash mvnw -B test
bash mvnw -B -DskipTests package
```

El test del repositorio usa JdbcTemplate real y mocks de JDBC para verificar el
CALL, orden/tipos de los cuatro parámetros, ejecución y cierre de recursos.
Los tests HTTP usan el servicio real y simulan respuestas del repositorio para
verificar 204/400/409/500 y la delegación de las reglas a PostgreSQL.
No requieren una base de datos. La prueba preexistente BackendApplicationTests
carga todo el contexto, pero no ejecuta PA2 ni comprueba conectividad. Para usar
el endpoint contra PostgreSQL se requieren las variables DB_HOST, DB_PORT,
DB_NAME, DB_USERNAME y DB_PASSWORD. La ejecución real de PA2 contra la base
oficial sigue siendo una comprobación de integración independiente.
