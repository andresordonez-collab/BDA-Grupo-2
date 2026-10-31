# PA1: finalizar un viaje

`POST /api/viajes/{idViaje}/finalizar`, `Content-Type: application/json`:

```json
{"idEstacionDestino": 2}
```

Requiere cookie JWT `access_token` y autoridad `ROLE_Usuario`. El principal es
el Long establecido por JwtAuthenticationFilter; no se toma identidad del JSON.
Los identificadores deben ser positivos. El destino exige entero JSON BIGINT,
sin coerción de cadenas ni decimales.

| HTTP | Resultado |
| --- | --- |
| 204 | Finalizado, sin cuerpo |
| 400 | Identificadores o JSON inválidos |
| 401 | Sin JWT válido |
| 403 | Rol no autorizado |
| 404 | Viaje inexistente o ajeno, mismo mensaje |
| 409 | Destino inexistente o rechazo conocido de PA1 |
| 500 | Error SQL desconocido, integridad, conectividad o fallo inesperado |

GlobalExceptionHandler devuelve `timestamp`, `status`, `codigo`, `mensaje`.
Los rechazos de filtros de seguridad no garantizan ese cuerpo. Para esta ruta
no se devuelven detalles SQL ni trazas. Los rechazos P0001 conocidos se clasifican
por los prefijos actuales de PA1; revisar la clasificación si cambia el SQL.

## Autorización y transacción

El esquema permite modificar viajes.id_usuario: no hay inmutabilidad garantizada.
ViajeService usa @Transactional con READ_COMMITTED y JdbcTemplate comparte la
conexión de esa transacción:

1. Bloquear la estación destino con SELECT FOR UPDATE, sin validar capacidad
   ni actividad. Si no existe, rechazar sin bloquear el viaje.
2. Obtener id_usuario del viaje con SELECT FOR UPDATE, sin filtrar por propietario
   ni estado. Comparar con el principal y rechazar viajes ajenos o inexistentes.
3. Ejecutar CALL finalizar_viaje(?, ?) con dos parámetros BIGINT.

El orden estación -> viaje coincide con PA1. El bloqueo del viaje impide cambiar
su propietario o eliminarlo entre la comprobación y el CALL; READ COMMITTED
espera actualizaciones concurrentes y lee la fila actualizada. Los bloqueos duran
hasta commit/rollback. PA1 conserva las reglas de cálculo, cobro y actualización.
Invocar el servicio mediante el proxy Spring, sin una transacción externa con
otro aislamiento. No hay reintentos automáticos de interbloqueos.

Estación inexistente produce 409 antes de verificar propiedad; con estación
existente, viajes inexistentes y ajenos producen el mismo 404.

## Pruebas sin PostgreSQL

Desde Codigo_Fuente/backend:

```bash
bash mvnw -B '-Dtest=Viaje*Test,Rebalanceo*Test,JwtAuthenticationFilterTest' test
bash mvnw -B -DskipTests package
```

ViajeControllerTest usa la cadena real de seguridad, JWT de prueba, servicio real
y repositorio simulado. ViajeServiceTest comprueba propiedad, orden y errores.
ViajeRepositoryTest verifica SQL y parámetros con JDBC simulado.
ViajeTransactionTest usa el proxy transaccional real y un gestor simulado para
comprobar aislamiento, orden, commit y rollback. No prueba bloqueos PostgreSQL.

Pendiente en PostgreSQL desechable: cambios en viajes, billeteras,
movimientos_billeteras y vehiculos; actualización concurrente de propietario;
doble finalización; último cupo; PA1 contra PA2 y reversión completa ante errores.
Usar rollback para una conexión e instancia desechable para pruebas concurrentes
con commits. Las secuencias no se revierten con rollback.

Se conserva el comportamiento SQL respecto a saldo negativo, tarifas editables,
hora de finalización e idempotencia. CSRF y usuarios desactivados tras emitir JWT
siguen siendo decisiones pendientes de la seguridad general existente.
