# Funcionamiento del Switch de Pagos Masivos Banco BanQuito

## 1. Propósito del sistema

El Switch de Pagos Masivos Banco BanQuito permite recibir lotes de pagos enviados por empresas cliente, validar su estructura, procesar cada instrucción de pago línea por línea, calcular comisiones, liquidar contablemente el servicio, generar reportes de cierre y registrar notificaciones a beneficiarios.

El procesamiento implementado actualmente es intrabancario, sincrónico y unitario. Esto significa que el backend procesa una línea de pago a la vez, continúa con la siguiente aunque una falle y no utiliza microservicios ni colas externas.

## 2. Arquitectura general

El backend está organizado como un monolito modular bajo el paquete base `com.banquito.switchpagos`. Cada módulo concentra una responsabilidad funcional o transversal.

| Módulo | Responsabilidad |
|---|---|
| `common` | Centraliza excepciones, respuestas estándar de error y manejo global de errores. |
| `config` | Reserva de configuración técnica general del proyecto. |
| `catalogo` | Administra catálogos transversales, especialmente `TIPO_SERVICIO`. |
| `parametro` | Administra configuración operativa persistida en `PARAMETRO_SWITCH`, como IVA, hora de corte y ventana de duplicidad. |
| `auditoria` | Registra acciones relevantes en `BITACORA_AUDITORIA_SWITCH`. |
| `lote` | Administra el ciclo de vida del lote, estados, historial y cola interna persistida. |
| `archivo` | Lee archivos `CSV/TXT`, calcula `hashArchivo`, parsea cabecera/detalle/pie y valida estructura. |
| `procesamiento` | Administra líneas de pago, validación de límites y procesamiento financiero línea por línea. |
| `integracioncore` | Simula la comunicación con el Core Bancario para saldo, cuentas y movimientos. |
| `tarifaje` | Calcula tarifas, comisión, IVA y registra liquidación contable. |
| `reporte` | Genera reporte de novedades, comprobante de liquidación y notificaciones a beneficiarios. |

El Core Bancario no forma parte de esta base de datos. El Switch guarda únicamente referencias lógicas, como números de cuenta y UUIDs de transacciones.

## 3. Estructura del archivo de entrada

El backend soporta actualmente archivos `.csv` y `.txt` con registros separados por coma. El formato documentado es:

```text
H,ruc,tipoServicio,fechaHoraGeneracion,cuentaMatriz,totalRegistros,montoTotal
D,secuencial,identificacion,nombre,cuentaDestino,monto,concepto,correo
T,hashPieControl,totalRegistros,montoTotal
```

Ejemplo completo:

```text
H,1790012345001,NOM,2026-04-20T10:30:00-05:00,0010001234567890,3,1800.00
D,1,0912345678,Juan Perez,0020009876543210,1000.00,Sueldo Abril,juan.perez@correo.com
D,2,0922222222,Maria Lopez,0020009876543220,500.00,Sueldo Abril,maria.lopez@correo.com
D,3,0933333333,Carlos Ruiz,0020009876543230,300.00,Sueldo Abril,carlos.ruiz@correo.com
T,ABC123HASH,3,1800.00
```

`H` es la cabecera del archivo. Contiene RUC de empresa, tipo de servicio, fecha de generación, cuenta matriz, total de registros y monto total declarado.

`D` es una línea de detalle. Contiene secuencial, identificación del beneficiario, nombre, cuenta destino, monto, concepto y correo de notificación.

`T` es el pie de control. Contiene `hashPieControl`, total de registros y monto total de control.

`hashArchivo` se calcula sobre el archivo recibido completo. `hashPieControl` se lee desde la línea `T`. La detección de duplicidad usa `rucEmpresa + nombreArchivo + hashArchivo + fechaRecepcion` dentro de la ventana configurada en parámetros.

## 4. Estados del lote

| Estado | Uso |
|---|---|
| `RECIBIDO` | Lote recibido dentro de horario hábil y pendiente de validación. |
| `VALIDANDO` | Lote en validación estructural. |
| `VALIDADO` | Archivo estructuralmente correcto y listo para procesamiento financiero. |
| `RECHAZADO` | Lote rechazado por error global de archivo o validación estructural. |
| `ENCOLADO` | Lote recibido fuera de horario, fin de semana o fecha no hábil simulada. |
| `PROCESANDO` | Lote en procesamiento financiero línea por línea. |
| `PROCESADO_PARCIAL` | Procesamiento terminado con al menos una línea rechazada/fallida, o sin éxito total. |
| `PROCESADO_TOTAL` | Todas las líneas procesables fueron exitosas. |
| `CERRADO` | Liquidación contable completada; reportes disponibles. |
| `ANULADO` | Lote anulado antes de afectación financiera. |

Flujo esperado:

```text
RECIBIDO / ENCOLADO
→ VALIDANDO
→ VALIDADO
→ PROCESANDO
→ PROCESADO_TOTAL o PROCESADO_PARCIAL
→ CERRADO
```

Flujos alternos:

- `RECHAZADO` por error global de archivo.
- `ANULADO` antes de afectación financiera.

## 5. Estados de línea

| Estado | Uso |
|---|---|
| `PENDIENTE` | Línea parseada y aún no procesada financieramente. |
| `VALIDADA` | Línea pasó validaciones previas de procesamiento. |
| `ENVIADA_CORE` | Línea enviada al Core Bancario simulado para débito/crédito. |
| `EXITOSA` | Débito y crédito simulados se completaron correctamente. |
| `RECHAZADA` | Falló por una regla de negocio esperada. |
| `FALLIDA` | Falló por un error técnico o inesperado. |
| `REVERSADA` | Estado reservado para reversos posteriores. |

`RECHAZADA` representa una condición de negocio controlada, por ejemplo cuenta destino bloqueada. `FALLIDA` representa una falla técnica o inesperada. En ambos casos el lote continúa procesando las siguientes líneas.

## 6. Flujo funcional completo

| Paso | Endpoint | Módulo principal | Estado antes | Estado después | Persistencia principal |
|---|---|---|---|---|---|
| 1. Consultar horarios de corte | `GET /api/v1/pagos-masivos/horarios-corte` | `parametro` | No aplica | No aplica | Lee `PARAMETRO_SWITCH`. |
| 2. Consultar tarifas | `GET /api/v1/pagos-masivos/tarifas` | `tarifaje` | No aplica | No aplica | Lee `TARIFA_SERVICIO`. |
| 3. Cargar lote | `POST /api/v1/pagos-masivos/lotes` | `lote` / `archivo` | No existe | `RECIBIDO` o `ENCOLADO` | Guarda `LOTE_PAGO`, `LINEA_PAGO`, historial y cola si aplica. |
| 4. Validar lote | `POST /api/v1/pagos-masivos/lotes/{uuidLote}/validar` | `lote` | `RECIBIDO` o `ENCOLADO` | `VALIDADO` o `RECHAZADO` | Actualiza lote, totales, motivo de rechazo e historial. |
| 5. Consultar líneas | `GET /api/v1/pagos-masivos/lotes/{uuidLote}/lineas` | `lote` / `procesamiento` | Cualquier estado existente | Sin cambio | Lee `LINEA_PAGO`. |
| 6. Procesar lote | `POST /api/v1/pagos-masivos/lotes/{uuidLote}/procesar` | `procesamiento` | `VALIDADO` | `PROCESADO_TOTAL` o `PROCESADO_PARCIAL` | Actualiza líneas, UUIDs Core simulados, lote e historial. |
| 7. Liquidar comisión e IVA | `POST /api/v1/pagos-masivos/lotes/{uuidLote}/liquidar` | `tarifaje` | `PROCESADO_TOTAL` o `PROCESADO_PARCIAL` | `CERRADO` | Guarda `LIQUIDACION_SERVICIO` y `DETALLE_LIQUIDACION`. |
| 8. Consultar novedades | `GET /api/v1/pagos-masivos/lotes/{uuidLote}/novedades` | `reporte` | `CERRADO` | Sin cambio | Guarda o lee `REPORTE_CIERRE`; crea notificaciones si faltan. |
| 9. Consultar comprobante | `GET /api/v1/pagos-masivos/lotes/{uuidLote}/comprobante` | `reporte` | `CERRADO` | Sin cambio | Guarda o lee `REPORTE_CIERRE`. |

## 7. Reglas de validación estructural

La validación estructural no llama al Core Bancario.

- Cabecera: debe existir un registro `H` con RUC, tipo de servicio, fecha, cuenta matriz, total declarado y monto declarado.
- Pie: debe existir un registro `T` con `hashPieControl`, total de registros y monto total.
- Secuenciales: las líneas `D` deben iniciar en `1` y ser consecutivas.
- Total de registros: el total declarado en cabecera y pie debe coincidir con la cantidad de líneas de detalle.
- Monto total: el monto declarado en cabecera y pie debe coincidir con la sumatoria de los detalles.
- Tipo de servicio: debe existir y estar activo.
- Duplicidad: se valida por RUC, nombre de archivo, `hashArchivo` y ventana de días configurada.

## 8. Reglas de procesamiento línea por línea

- Solo se procesan lotes en estado `VALIDADO`.
- Las líneas se procesan en orden de secuencial.
- Solo se procesan líneas `PENDIENTE` o `VALIDADA`.
- Se valida límite vigente activo por tipo de servicio.
- Se consulta saldo disponible en el Core simulado.
- Se valida cuenta destino en el Core simulado.
- Se ejecutan débito y crédito simulados.
- Una línea fallida no aborta el lote.

Reglas del Core simulado:

- Cuentas válidas por defecto.
- Cuenta destino terminada en `0000` → `CUENTA_DESTINO_NO_EXISTE`.
- Cuenta destino terminada en `9999` → `CUENTA_DESTINO_BLOQUEADA`.
- Saldo disponible simulado: `100000.00`.
- Monto mayor al saldo simulado → `SALDO_INSUFICIENTE`.
- Débito y crédito generan UUIDs simulados.

## 9. Reglas de tarifaje y liquidación

- `transaccionesExitosas = líneas EXITOSA`.
- `transaccionesFallidas = líneas RECHAZADA + FALLIDA`.
- `subtotalComision = transaccionesExitosas × tarifaUnitaraAplicada`.
- `montoIva = subtotalComision × IVA_PORCENTAJE`.
- `totalDebitado = subtotalComision + montoIva`.
- Los montos finales usan escala `2` y `RoundingMode.HALF_UP`.
- La liquidación permite sobregiro para el débito global de comisión.

Movimientos contables registrados:

- `DEBITO_CUENTA_MATRIZ`.
- `CREDITO_INGRESOS`.
- `CREDITO_IVA`.

La liquidación se registra en `LIQUIDACION_SERVICIO` y los movimientos en `DETALLE_LIQUIDACION`.

## 10. Reportes y notificaciones

El reporte de novedades resume líneas exitosas, rechazadas y fallidas, e incluye detalle por línea con secuencial, estado, código/mensaje de error, monto, cuenta destino y beneficiario.

El comprobante de liquidación incluye RUC, cuenta matriz, transacciones exitosas y rechazadas, monto dispersado, tarifa aplicada, subtotal de comisión, IVA y total debitado.

Las notificaciones a beneficiarios se registran en `NOTIFICACION_BENEFICIARIO` para líneas `EXITOSA` con correo. El SMTP es simulado: si el correo tiene formato básico válido se marca `ENVIADA`; si no, se marca `ERROR`. Las notificaciones no afectan la transacción financiera.

Los formatos `PDF`, `CSV` y `XLSX` se registran como metadato, pero el contenido persistido actualmente es JSON estructurado. No se genera archivo binario real.

## 11. Endpoints disponibles

| Método | Ruta | Propósito | Estado requerido | Respuesta esperada |
|---|---|---|---|---|
| `POST` | `/api/v1/pagos-masivos/lotes` | Cargar archivo multipart y registrar lote. | No aplica | `201`, `uuidLote`, estado inicial y siguiente acción. |
| `GET` | `/api/v1/pagos-masivos/lotes` | Listar lotes con filtros. | No aplica | Página con lotes. |
| `GET` | `/api/v1/pagos-masivos/lotes/{uuidLote}/estado` | Consultar estado y resumen del lote. | Lote existente | Estado, fechas, resumen y acciones disponibles. |
| `DELETE` | `/api/v1/pagos-masivos/lotes/{uuidLote}` | Anular lote. | `RECIBIDO`, `VALIDANDO`, `VALIDADO`, `ENCOLADO` o `RECHAZADO` | Estado `ANULADO`. |
| `POST` | `/api/v1/pagos-masivos/lotes/{uuidLote}/validar` | Ejecutar validación estructural. | `RECIBIDO` o `ENCOLADO` | `VALIDADO` o `RECHAZADO`. |
| `GET` | `/api/v1/pagos-masivos/lotes/{uuidLote}/lineas` | Consultar líneas del lote. | Lote existente | Página de líneas. |
| `POST` | `/api/v1/pagos-masivos/lotes/{uuidLote}/procesar` | Procesar líneas contra Core simulado. | `VALIDADO` | `PROCESADO_TOTAL` o `PROCESADO_PARCIAL`. |
| `POST` | `/api/v1/pagos-masivos/lotes/{uuidLote}/liquidar` | Calcular comisión, IVA y registrar movimientos. | `PROCESADO_TOTAL` o `PROCESADO_PARCIAL` | Liquidación `COMPLETADO`. |
| `GET` | `/api/v1/pagos-masivos/tarifas` | Consultar tarifario vigente. | No aplica | Tarifas vigentes. |
| `GET` | `/api/v1/pagos-masivos/horarios-corte` | Consultar horarios operativos. | No aplica | Hora de corte, inicio de encolados y ventana de duplicidad. |
| `GET` | `/api/v1/pagos-masivos/lotes/{uuidLote}/novedades` | Obtener o generar reporte de novedades. | `CERRADO` | Reporte de novedades. |
| `GET` | `/api/v1/pagos-masivos/lotes/{uuidLote}/comprobante` | Obtener o generar comprobante de liquidación. | `CERRADO` y liquidación completada | Comprobante de liquidación. |

## 12. Casos de prueba recomendados

- Lote válido total.
- Lote válido parcial.
- Lote descuadrado.
- Archivo duplicado.
- Lote fuera de horario.
- Cuenta destino inexistente.
- Cuenta destino bloqueada.
- Monto mayor al saldo simulado.
- Liquidación duplicada.
- Consulta de reportes antes de liquidar.

## 13. Limitaciones actuales

- Core Bancario simulado.
- SMTP simulado.
- No hay microservicios.
- No hay colas externas.
- PDF/CSV/XLSX reales no se generan como archivos binarios.
- Autenticación simplificada o ausente si el proyecto aún no integra seguridad completa.
