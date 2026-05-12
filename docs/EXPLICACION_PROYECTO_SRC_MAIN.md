# Explicacion del proyecto en `src/main`

## Que hace este proyecto

Este proyecto es un backend en Spring Boot para un Switch de Pagos Masivos de Banco BanQuito.

Su funcion principal es recibir archivos de pagos enviados por empresas, validarlos, registrar un lote, procesar cada pago contra un Core Bancario simulado, calcular comisiones e IVA, liquidar contablemente el servicio y generar reportes de cierre.

## Como esta organizado `src/main`

Dentro de `src/main` hay dos partes principales:

- `src/main/java`: contiene toda la logica Java del sistema.
- `src/main/resources`: contiene la configuracion de la aplicacion.

La aplicacion arranca desde:

- `src/main/java/com/banquito/switchpagos/SwitchPagosApplication.java`

La configuracion de base de datos esta en:

- `src/main/resources/application.properties`

## Modulos principales del sistema

### `lote`

Administra el ciclo de vida de un lote de pagos.

Se encarga de:

- registrar el lote cuando se carga un archivo;
- consultar lotes y su estado;
- validar el lote;
- anularlo si todavia no ha sido afectado financieramente;
- consultar las lineas del lote.

Controlador principal:

- `src/main/java/com/banquito/switchpagos/lote/controller/LotePagoController.java`

Servicio clave:

- `src/main/java/com/banquito/switchpagos/lote/service/impl/LotePagoServiceImpl.java`

### `archivo`

Lee y valida el archivo de entrada.

Actualmente soporta archivos `.csv` y `.txt` con registros:

- `H`: cabecera
- `D`: detalle
- `T`: pie

Este modulo:

- parsea el archivo linea por linea;
- valida cantidad de campos;
- valida secuenciales;
- valida totales declarados;
- calcula el hash SHA-256 del archivo.

Servicios clave:

- `src/main/java/com/banquito/switchpagos/archivo/service/impl/ArchivoPagoServiceImpl.java`
- `src/main/java/com/banquito/switchpagos/archivo/service/impl/ValidadorArchivoPagoServiceImpl.java`

### `procesamiento`

Procesa cada linea de pago de un lote ya validado.

Este modulo:

- revisa limites de transaccion;
- consulta saldo de la cuenta matriz;
- valida la cuenta destino;
- ejecuta debito y credito en el Core Bancario simulado;
- marca cada linea como `EXITOSA`, `RECHAZADA` o `FALLIDA`.

Controlador principal:

- `src/main/java/com/banquito/switchpagos/procesamiento/controller/ProcesamientoPagoController.java`

Servicio clave:

- `src/main/java/com/banquito/switchpagos/procesamiento/service/impl/ProcesamientoPagoServiceImpl.java`

### `tarifaje`

Calcula el costo del servicio y realiza la liquidacion del lote.

Este modulo:

- consulta tarifas vigentes;
- calcula comision;
- calcula IVA;
- registra movimientos contables;
- cierra el lote cuando la liquidacion termina correctamente.

Controlador principal:

- `src/main/java/com/banquito/switchpagos/tarifaje/controller/TarifajeController.java`

Servicio clave:

- `src/main/java/com/banquito/switchpagos/tarifaje/service/impl/LiquidacionContableServiceImpl.java`

### `reporte`

Genera los reportes de salida del lote.

Este modulo:

- genera reporte de novedades;
- genera comprobante de liquidacion;
- registra notificaciones para beneficiarios;
- guarda el contenido del reporte como JSON.

Controlador principal:

- `src/main/java/com/banquito/switchpagos/reporte/controller/ReporteLoteController.java`

Servicio clave:

- `src/main/java/com/banquito/switchpagos/reporte/service/impl/ReporteLoteServiceImpl.java`

### `integracioncore`

Simula la comunicacion con el Core Bancario.

Este modulo expone operaciones para:

- consultar saldo disponible;
- validar cuenta destino;
- ejecutar debitos;
- ejecutar creditos.

Importante: actualmente no es una integracion real, sino un stub.

Servicio clave:

- `src/main/java/com/banquito/switchpagos/integracioncore/service/impl/CoreBancarioServiceStub.java`

### Modulos de soporte

- `catalogo`: expone catalogos como tipos de servicio.
- `parametro`: maneja parametros operativos del switch.
- `auditoria`: registra acciones relevantes del sistema.
- `common`: centraliza excepciones, DTOs comunes y manejo global de errores.

## Flujo funcional del proyecto

### 1. Carga del archivo

La empresa envia un archivo de pagos masivos mediante el endpoint de lotes.

Endpoint principal:

- `POST /api/v1/pagos-masivos/lotes`

En este paso el sistema:

- lee el archivo;
- valida que tenga estructura correcta;
- valida cabecera, detalle y pie;
- calcula un hash del archivo;
- registra el lote y las lineas.

### 2. Validacion del lote

Luego se ejecuta la validacion del lote:

- `POST /api/v1/pagos-masivos/lotes/{uuidLote}/validar`

En esta fase el sistema verifica que el lote pueda pasar a procesamiento y actualiza su estado a `VALIDADO` o `RECHAZADO`.

### 3. Procesamiento de lineas

Cuando el lote esta validado, se procesa con:

- `POST /api/v1/pagos-masivos/lotes/{uuidLote}/procesar`

El sistema toma cada linea en orden y hace lo siguiente:

1. valida reglas y limites;
2. consulta saldo disponible de la cuenta matriz;
3. valida la cuenta destino del beneficiario;
4. ejecuta un debito simulado;
5. ejecuta un credito simulado;
6. guarda el resultado de la linea.

Si una linea falla, el lote sigue con las demas. Por eso el resultado final puede ser:

- `PROCESADO_TOTAL`
- `PROCESADO_PARCIAL`

### 4. Liquidacion del servicio

Despues del procesamiento se ejecuta:

- `POST /api/v1/pagos-masivos/lotes/{uuidLote}/liquidar`

En esta etapa el sistema:

- cuenta transacciones exitosas y fallidas;
- busca la tarifa aplicable;
- calcula comision;
- calcula IVA;
- registra movimientos contables;
- deja el lote listo para cierre.

### 5. Generacion de reportes

Cuando el lote ya esta cerrado, se pueden consultar los reportes:

- `GET /api/v1/pagos-masivos/lotes/{uuidLote}/novedades`
- `GET /api/v1/pagos-masivos/lotes/{uuidLote}/comprobante`

Estos reportes muestran:

- resumen de lineas exitosas, rechazadas y fallidas;
- detalle de errores por linea;
- resumen de liquidacion;
- informacion de empresa y cuenta matriz.

## Endpoints importantes

### Lotes

- `POST /api/v1/pagos-masivos/lotes`
- `GET /api/v1/pagos-masivos/lotes`
- `GET /api/v1/pagos-masivos/lotes/{uuidLote}/estado`
- `DELETE /api/v1/pagos-masivos/lotes/{uuidLote}`
- `POST /api/v1/pagos-masivos/lotes/{uuidLote}/validar`
- `GET /api/v1/pagos-masivos/lotes/{uuidLote}/lineas`

### Procesamiento

- `POST /api/v1/pagos-masivos/lotes/{uuidLote}/procesar`

### Tarifaje

- `GET /api/v1/pagos-masivos/tarifas`
- `POST /api/v1/pagos-masivos/lotes/{uuidLote}/liquidar`

### Reportes

- `GET /api/v1/pagos-masivos/lotes/{uuidLote}/novedades`
- `GET /api/v1/pagos-masivos/lotes/{uuidLote}/comprobante`

### Catalogos

- `GET /api/v1/pagos-masivos/tipos-servicio`

## Decisiones importantes que se ven en el codigo

- El proyecto esta hecho como monolito modular, no como microservicios.
- El Core Bancario todavia es simulado.
- Los reportes se guardan en JSON aunque acepten formatos como PDF, CSV o XLSX como metadato.
- Hay manejo global de errores para devolver respuestas controladas.
- Se usa PostgreSQL como base de datos segun `application.properties`.

## En resumen

`src/main` contiene toda la implementacion del backend del switch. El proyecto recibe lotes de pagos, valida archivos, procesa pagos uno por uno, liquida comisiones e impuestos y genera reportes finales para cerrar el ciclo operativo del lote.
