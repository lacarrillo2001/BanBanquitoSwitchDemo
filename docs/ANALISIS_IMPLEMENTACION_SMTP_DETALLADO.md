# Analisis detallado de la implementacion de envio SMTP

## Objetivo

Este documento describe en detalle tecnico como esta implementado el envio de correos en la rama actual, con el fin de replicarlo en otra rama con el menor numero posible de cambios funcionales y estructurales.

El foco no es solo explicar que se envia un correo, sino detallar:

- librerias involucradas;
- configuracion necesaria;
- clases, interfaces y entidades participantes;
- flujo exacto desde el endpoint hasta el servidor SMTP;
- persistencia del estado del envio;
- endpoints de soporte y prueba;
- consideraciones para portar la implementacion sin romper el comportamiento actual.

## Vista general de la solucion

La implementacion de correo esta montada sobre Spring Boot Mail y se apoya en dos caminos complementarios:

- envio SMTP real usando `JavaMailSender`;
- inspeccion de mensajes usando Mailpit por HTTP REST.

El diseño actual no separa el envio en un proceso asíncrono ni en una cola. El envio ocurre dentro del flujo transaccional del backend cuando se generan notificaciones pendientes.

## Librerias y dependencias involucradas

### Dependencia principal para correo

En `pom.xml:85` se declara:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

Esta dependencia aporta principalmente:

- autoconfiguracion de `JavaMailSender`;
- integracion con propiedades `spring.mail.*`;
- soporte para `SimpleMailMessage` y `MimeMessage`.

En esta implementacion solo se usa `SimpleMailMessage`.

### Otras librerias relevantes

- `spring-boot-starter-data-jpa` en `pom.xml:37` para persistir el estado de las notificaciones.
- `spring-boot-starter-webmvc` en `pom.xml:41` para exponer endpoints REST de prueba y consulta.
- `jackson-databind` en `pom.xml:45` para construir el contenido JSON persistido en cada notificacion.
- `RestTemplate` de Spring Web para consultar Mailpit desde `MailpitServiceImpl`.
- `jakarta.persistence` para entidad, repositorio y `EntityManager`.

## Configuracion externa requerida

### Carga de variables

La aplicacion carga variables desde `.env` por medio de:

- `src/main/resources/application.properties:2`

```properties
spring.config.import=optional:file:.env[.properties]
```

Esto es importante para la rama destino: si no se preserva esta linea o un mecanismo equivalente, `spring.mail.*` no recibira los valores esperados desde `.env`.

### Propiedades SMTP usadas por Spring Boot

Definidas en `src/main/resources/application.properties:21`:

```properties
spring.mail.host=${SPRING_MAIL_HOST}
spring.mail.port=${SPRING_MAIL_PORT:1025}
spring.mail.username=${SPRING_MAIL_USERNAME:}
spring.mail.password=${SPRING_MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH:false}
spring.mail.properties.mail.smtp.starttls.enable=${SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE:false}
spring.mail.from=${SPRING_MAIL_FROM}
mailpit.api.url=${MAILPIT_API_URL}
```

### Lectura tecnica de cada propiedad

- `spring.mail.host`
  - host del servidor SMTP al que `JavaMailSender` se conecta.
- `spring.mail.port`
  - puerto SMTP; tiene `1025` como default.
- `spring.mail.username`
  - usuario SMTP, actualmente opcional y vacio en este entorno.
- `spring.mail.password`
  - password SMTP, actualmente opcional y vacio.
- `spring.mail.properties.mail.smtp.auth`
  - habilita o no autenticacion SMTP.
- `spring.mail.properties.mail.smtp.starttls.enable`
  - activa o no STARTTLS.
- `spring.mail.from`
  - remitente logico que el servicio asigna explicitamente al mensaje.
- `mailpit.api.url`
  - URL base del API HTTP de Mailpit para listar, leer y borrar mensajes de prueba.

### Valores actuales observados en el repo

En `.env` se encontraron estos valores:

```properties
SPRING_MAIL_HOST=34.27.240.236
SPRING_MAIL_PORT=1025
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=false
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=false
SPRING_MAIL_FROM=notificaciones@banquito.com
MAILPIT_API_URL=http://34.27.240.236:8025/api/v1
```

Interpretacion tecnica:

- el backend espera un SMTP simple en `34.27.240.236:1025`;
- no usa autenticacion;
- no usa STARTTLS;
- usa Mailpit como receptor/visor de pruebas mediante API HTTP en puerto `8025`.

## Componentes de codigo involucrados

### Servicio principal de notificaciones

Archivo:

- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java`

Responsabilidades:

- registrar notificaciones por lineas exitosas;
- construir contenido de la notificacion;
- validar minimamente el correo destino;
- enviar notificaciones pendientes por SMTP;
- persistir estado final del intento;
- registrar auditoria;
- exponer un flujo de prueba directa.

### Interfaz del servicio

Archivo:

- `src/main/java/com/banquito/switchpagos/report/service/NotificacionService.java`

Metodos expuestos:

```java
void registrarNotificacionesBeneficiarios(UUID uuidLote);
void registrarNotificacionLineaExitosa(LineaPagoInternalDto lineaPagoInternalDto, String rucEmpresa);
void enviarNotificacionesPendientes();
void enviarEmailPruebaDirecto(String destinatario, String asunto, String cuerpo);
String obtenerResumenDb();
```

Esto define claramente la API interna que la rama destino debe conservar para mantener el acoplamiento actual con el modulo `report`.

### Mapper de notificaciones

Archivo:

- `src/main/java/com/banquito/switchpagos/report/mapper/NotificacionBeneficiarioMapper.java`

Responsabilidad:

- transformar la informacion de una `LineaPagoInternalDto` mas su contenido JSON a una entidad `NotificacionBeneficiario` lista para persistirse.

### Entidad persistente

Archivo:

- `src/main/java/com/banquito/switchpagos/report/model/NotificacionBeneficiario.java`

Tabla:

- `switch_banquito.NOTIFICACION_BENEFICIARIO`

Campos relevantes:

- `id_notificacion`
- `id_linea`
- `correo_destino`
- `tipo_notificacion`
- `asunto`
- `contenido`
- `estado_envio`
- `fecha_envio`
- `error_envio`
- `reintentos`
- `proximo_reintento_en`
- `fecha_actualizacion`
- `version`

### Repositorio

Archivo:

- `src/main/java/com/banquito/switchpagos/report/repository/NotificacionBeneficiarioRepository.java`

Metodos usados por el flujo:

```java
Boolean existsByLineaPagoIdLinea(Long idLinea);
List<NotificacionBeneficiario> findByLineaPago(LineaPago lineaPago);
List<NotificacionBeneficiario> findByEstadoEnvio(EstadoEnvioNotificacion estadoEnvio);
```

### Servicio auxiliar Mailpit

Archivos:

- `src/main/java/com/banquito/switchpagos/report/service/MailpitService.java`
- `src/main/java/com/banquito/switchpagos/report/service/impl/MailpitServiceImpl.java`

Responsabilidad:

- consultar el buzón de Mailpit por HTTP;
- listar mensajes;
- leer un mensaje puntual;
- limpiar la bandeja.

Importante: este servicio no envia correos. Solo inspecciona el servidor de pruebas por API HTTP.

## Inyeccion de dependencias en NotificacionServiceImpl

Constructor en `NotificacionServiceImpl.java:46`:

```java
public NotificacionServiceImpl(NotificacionBeneficiarioRepository notificacionBeneficiarioRepository,
                               LineaPagoService lineaPagoService,
                               LotePagoService lotePagoService,
                               AuditoriaSwitchService auditoriaSwitchService,
                               ObjectMapper objectMapper,
                               EntityManager entityManager,
                               NotificacionBeneficiarioMapper notificacionBeneficiarioMapper,
                               JavaMailSender mailSender,
                               @Value("${spring.mail.from}") String remitenteNotificaciones)
```

Cada dependencia tiene una funcion precisa:

- `NotificacionBeneficiarioRepository`
  - persiste y consulta notificaciones.
- `LineaPagoService`
  - recupera lineas del lote y sus estados.
- `LotePagoService`
  - obtiene datos del lote, especialmente `rucEmpresa`.
- `AuditoriaSwitchService`
  - registra eventos funcionales y tecnicos.
- `ObjectMapper`
  - construye JSON del contenido.
- `EntityManager`
  - crea referencias JPA y ejecuta consulta puntual en el endpoint de prueba.
- `NotificacionBeneficiarioMapper`
  - encapsula el armado base de la entidad.
- `JavaMailSender`
  - canal concreto de envio SMTP.
- `spring.mail.from`
  - remitente configurado externamente.

## Flujo funcional completo del envio normal

### 1. Endpoint que dispara el envio funcional

El envio normal no se dispara al cerrar el procesamiento de cada linea, sino al generar el reporte de novedades.

Endpoint:

- `GET /api/v1/pagos-masivos/lotes/{uuidLote}/novedades`

Implementado en:

- `src/main/java/com/banquito/switchpagos/report/controller/ReporteLoteController.java:25`

Metodo:

```java
public ReporteNovedadesResponse obtenerNovedades(
        @PathVariable("uuidLote") UUID uuidLote,
        @RequestParam(value = "formato", defaultValue = "JSON") FormatoReporte formato)
```

Este endpoint no recibe body. Usa:

- `uuidLote` como path param;
- `formato` como query param opcional.

### 2. Entrada al servicio de reportes

En `ReporteLoteServiceImpl.java:95`:

```java
private ReporteNovedadesResponse generarReporteNovedades(UUID uuidLote, FormatoReporte formato) {
    LoteProcesamientoInternalDto lote = lotePagoService.obtenerDatosProcesamiento(uuidLote);
    validarLoteCerrado(lote);
    notificacionService.registrarNotificacionesBeneficiarios(uuidLote);
    ...
}
```

Puntos claves:

- solo se ejecuta si el lote esta `CERRADO`;
- el disparador del correo esta embebido dentro de la generacion de novedades;
- si se quiere mantener el mismo comportamiento en otra rama, este punto de integracion debe preservarse.

### 3. Registro masivo de notificaciones

Metodo:

- `NotificacionServiceImpl.registrarNotificacionesBeneficiarios(UUID uuidLote)`

Implementacion base:

```java
String rucEmpresa = lotePagoService.obtenerDatosProcesamiento(uuidLote).rucEmpresa();
lineaPagoService.listarLineasPorLoteUuidYEstado(uuidLote, EstadoLineaPago.EXITOSA)
        .forEach(linea -> registrarNotificacionLineaExitosa(linea, rucEmpresa));
enviarNotificacionesPendientes();
```

Secuencia exacta:

1. Obtiene `rucEmpresa` del lote.
2. Consulta todas las lineas del lote en estado `EXITOSA`.
3. Para cada linea intenta registrar una notificacion.
4. Luego toma todas las `PENDIENTE` y las envia por SMTP.

### 4. Obtencion de lineas exitosas

El origen de datos viene de `LineaPagoService`.

Implementacion:

- `src/main/java/com/banquito/switchpagos/processing/service/impl/LineaPagoServiceImpl.java:92`

```java
public List<LineaPagoInternalDto> listarLineasPorLoteUuidYEstado(UUID uuidLote, EstadoLineaPago estado) {
    return lineaPagoRepository.findByLotePagoUuidLoteAndEstadoOrderBySecuencialAsc(uuidLote, estado)
            .stream()
            .map(lineaPagoMapper::toInternalDto)
            .toList();
}
```

Repositorio asociado:

- `src/main/java/com/banquito/switchpagos/processing/repository/LineaPagoRepository.java:26`

```java
List<LineaPago> findByLotePagoUuidLoteAndEstadoOrderBySecuencialAsc(UUID uuidLote, EstadoLineaPago estado);
```

Esto significa que la notificacion trabaja sobre DTOs internos, no directamente sobre entidades JPA de `LineaPago` completas.

### 5. Registro de una notificacion por linea exitosa

Metodo:

- `NotificacionServiceImpl.registrarNotificacionLineaExitosa(...)`

Reglas implementadas:

#### Regla 1: no registrar si no hay correo

```java
if (lineaPagoInternalDto.correoNotificacion() == null || lineaPagoInternalDto.correoNotificacion().isBlank()) {
    return;
}
```

Consecuencia:

- las lineas exitosas sin correo simplemente se omiten;
- no se registra ni `PENDIENTE` ni `ERROR` para esos casos.

#### Regla 2: no duplicar por linea

```java
if (Boolean.TRUE.equals(notificacionBeneficiarioRepository.existsByLineaPagoIdLinea(lineaPagoInternalDto.idLinea()))) {
    return;
}
```

Consecuencia:

- solo se intenta una notificacion por `idLinea` en el flujo normal;
- si el reporte de novedades se vuelve a generar, no crea otra notificacion para la misma linea.

#### Regla 3: asociar la notificacion a una linea existente

```java
entityManager.getReference(LineaPago.class, lineaPagoInternalDto.idLinea())
```

Punto tecnico:

- usa una referencia JPA perezosa sin hacer necesariamente un `select` completo;
- es suficiente para mantener la FK `id_linea` en `NOTIFICACION_BENEFICIARIO`.

#### Regla 4: construir contenido JSON

Metodo:

- `NotificacionServiceImpl.construirContenidoNotificacion(...)`

Contenido generado:

```json
{
  "montoAcreditado": ...,
  "concepto": "...",
  "empresaEmisora": "...",
  "cuentaDestino": "****1234"
}
```

Observaciones:

- el cuerpo persistido del correo no se guarda como texto plano final sino como JSON estructurado;
- la cuenta destino se enmascara;
- la empresa emisora sale del `rucEmpresa` del lote.

#### Regla 5: validar minimamente el correo

Metodo:

- `NotificacionServiceImpl.esCorreoValido(String correo)`

Implementacion:

```java
return correo != null && correo.contains("@") && correo.contains(".");
```

Es una validacion deliberadamente simple. Si quieres replicar comportamiento identico, no la endurezcas en la rama destino.

#### Regla 6: mapear a entidad con estado inicial

El mapper `NotificacionBeneficiarioMapper.toEntity(...)` define:

```java
notificacion.setTipoNotificacion(TipoNotificacion.PAGO_EXITOSO);
notificacion.setAsunto("Pago recibido Banco BanQuito");
notificacion.setContenido(contenido);
notificacion.setEstadoEnvio(Boolean.TRUE.equals(correoValido)
        ? EstadoEnvioNotificacion.PENDIENTE
        : EstadoEnvioNotificacion.ERROR);
notificacion.setErrorEnvio(Boolean.TRUE.equals(correoValido)
        ? null
        : "Correo de notificacion invalido.");
notificacion.setReintentos(0);
```

Observaciones funcionales:

- el asunto esta hardcodeado;
- si el correo es invalido, no entra al flujo SMTP porque nace ya como `ERROR`;
- `reintentos` se inicializa, pero no se usa realmente para retries automaticos.

#### Regla 7: persistencia y auditoria

Luego de crear la entidad:

```java
notificacionBeneficiarioRepository.save(notificacion);
registrarAuditoria("CREACION_NOTIFICACION", rucEmpresa, notificacion.getCorreoDestino(), notificacion.getEstadoEnvio().name());
```

La auditoria almacena:

- accion;
- ruc empresa;
- correo destino;
- estado.

## Flujo de envio SMTP de pendientes

### 1. Seleccion de pendientes

Metodo:

- `NotificacionServiceImpl.enviarNotificacionesPendientes()`

Implementacion:

```java
notificacionBeneficiarioRepository.findByEstadoEnvio(EstadoEnvioNotificacion.PENDIENTE)
        .forEach(this::enviarEmailReal);
```

Esto implica:

- solo se envian registros `PENDIENTE`;
- registros `ERROR` no se reintentan automaticamente;
- no hay paginacion ni chunking;
- una sola invocacion puede intentar enviar todos los pendientes de la tabla.

### 2. Construccion del mensaje SMTP

Metodo:

- `NotificacionServiceImpl.enviarEmailReal(NotificacionBeneficiario notificacion)`

Codigo central:

```java
SimpleMailMessage message = new SimpleMailMessage();
message.setTo(notificacion.getCorreoDestino());
message.setSubject(notificacion.getAsunto());
message.setText("Notificacion de Pago BanQuito: " + notificacion.getContenido().toString());
message.setFrom(remitenteNotificaciones);
mailSender.send(message);
```

Desglose tecnico:

- `SimpleMailMessage`
  - mensaje de texto plano, sin MIME complejo;
- `setTo(...)`
  - usa `correo_destino` persistido;
- `setSubject(...)`
  - usa el asunto almacenado en la entidad;
- `setText(...)`
  - no usa plantillas; serializa el JSON `contenido` con `toString()` y lo concatena a un prefijo fijo;
- `setFrom(...)`
  - usa `spring.mail.from`, no el usuario SMTP;
- `mailSender.send(...)`
  - delega completamente a la configuracion autoconfigurada de Spring Boot.

### 3. Resultado del intento

#### Caso exitoso

Si no hay excepcion:

```java
notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ENVIADA);
notificacion.setFechaEnvio(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
notificacion.setErrorEnvio(null);
```

#### Caso fallido

Si ocurre cualquier excepcion:

```java
notificacion.setEstadoEnvio(EstadoEnvioNotificacion.ERROR);
notificacion.setErrorEnvio(e.getMessage());
```

Observacion:

- se captura `Exception` generica;
- no hay diferenciacion entre error de red, timeout, rechazo SMTP, auth o formato.

### 4. Persistencia final del resultado

Siempre se ejecuta:

```java
notificacion.setFechaActualizacion(OffsetDateTime.now(ZONA_HORARIA_OPERATIVA));
notificacionBeneficiarioRepository.saveAndFlush(notificacion);
registrarAuditoria("ENVIO_NOTIFICACION_REAL", null, notificacion.getCorreoDestino(),
        notificacion.getEstadoEnvio().name());
```

Puntos tecnicos relevantes:

- `saveAndFlush(...)` fuerza sincronizacion inmediata con BD;
- la auditoria del envio usa `rucEmpresa = null`;
- la auditoria de creacion y la de envio no llevan exactamente la misma informacion contextual.

## Comportamiento transaccional

Los metodos principales del servicio estan anotados con `@Transactional`.

Esto significa que:

- el registro de notificaciones y el intento de envio corren dentro de transacciones JPA;
- el envio SMTP es un efecto externo ejecutado dentro de esa transaccion;
- no existe mecanismo outbox ni post-commit.

Implicaciones al portar:

- puede ocurrir envio real al SMTP aunque luego la transaccion de BD falle;
- puede haber desalineacion entre estado persistido y efecto externo si hay rollback despues de `send()`;
- si quieres comportamiento identico, no muevas el `send()` a un listener post-commit ni a una cola.

## Endpoint de prueba directa y su importancia

Controlador:

- `src/main/java/com/banquito/switchpagos/report/controller/NotificationTestController.java`

### Endpoint de prueba SMTP

```http
POST /api/v1/report/notifications/test-email?email=destino@correo.com
```

Metodo:

```java
public ResponseEntity<String> enviarEmailPrueba(@RequestParam String email)
```

Body request:

- no usa body;
- recibe solo query param `email`.

Internamente llama:

```java
notificacionService.enviarEmailPruebaDirecto(
    email,
    "Prueba BanQuito",
    "Correo de prueba usando tabla NOTIFICACION_BENEFICIARIO"
);
```

### Implementacion de `enviarEmailPruebaDirecto(...)`

Pasos exactos:

1. Busca la ultima `LineaPago` existente con JPQL:

```java
SELECT l FROM LineaPago l ORDER BY l.idLinea DESC
```

2. Limita a 1 resultado.
3. Si no existe ninguna linea, lanza `RuntimeException`.
4. Crea manualmente una nueva `NotificacionBeneficiario`.
5. Le asigna una `LineaPago` existente para cumplir la FK `id_linea` no nula.
6. Setea correo, asunto, tipo, estado `PENDIENTE`, contenido JSON, reintentos y fecha.
7. Persiste con `saveAndFlush(...)`.
8. Reusa `enviarEmailReal(...)` para mandar el mensaje por SMTP.

Puntos clave para migracion:

- este endpoint es una prueba end-to-end completa de DB + SMTP + Mailpit;
- no necesita generar un lote nuevo;
- depende de que exista al menos una fila en `LINEA_PAGO`.

## Endpoints tecnicos disponibles

### 1. Reenviar pendientes

```http
POST /api/v1/report/notifications/send-pending
```

Archivo:

- `NotificationTestController.java:27`

Body:

- no lleva body.

Funcion:

- reejecuta `enviarNotificacionesPendientes()` y vuelve a intentar todos los `PENDIENTE`.

### 2. Probar envio directo

```http
POST /api/v1/report/notifications/test-email?email=destino@correo.com
```

Archivo:

- `NotificationTestController.java:33`

Body:

- no lleva body.

Funcion:

- inserta una notificacion de prueba y usa el mismo flujo SMTP real.

### 3. Listar mensajes de Mailpit

```http
GET /api/v1/report/notifications/mailpit/messages
```

Archivo:

- `NotificationTestController.java:40`

Body:

- no lleva body.

Funcion:

- llama a `mailpitService.listarMensajes()`;
- devuelve el JSON crudo de Mailpit.

### 4. Obtener detalle de un mensaje Mailpit

```http
GET /api/v1/report/notifications/mailpit/messages/{id}
```

Archivo:

- `NotificationTestController.java:45`

Body:

- no lleva body.

Funcion:

- llama a `mailpitService.obtenerMensaje(id)`.

### 5. Limpiar mensajes de Mailpit

```http
DELETE /api/v1/report/notifications/mailpit/messages
```

Archivo:

- `NotificationTestController.java:50`

Body:

- no lleva body.

Funcion:

- llama a `mailpitService.borrarTodosLosMensajes()`.

### 6. Estado basico de BD para pruebas

```http
GET /api/v1/report/notifications/db-status
```

Archivo:

- `NotificationTestController.java:56`

Body:

- no lleva body.

Funcion:

- obtiene conteos agregados de lotes, lineas y notificaciones.

### 7. Endpoint funcional que dispara notificaciones normales

```http
GET /api/v1/pagos-masivos/lotes/{uuidLote}/novedades?formato=JSON
```

Archivo:

- `ReporteLoteController.java:25`

Body:

- no lleva body.

Funcion:

- genera el reporte de novedades y, como efecto colateral, registra y envia notificaciones a beneficiarios de lineas exitosas.

## Integracion con Mailpit

`MailpitServiceImpl` usa `RestTemplate` y la propiedad `mailpit.api.url`.

Implementacion:

```java
public JsonNode listarMensajes() {
    return restTemplate.getForObject(apiUrl + "/messages", JsonNode.class);
}

public JsonNode obtenerMensaje(String id) {
    return restTemplate.getForObject(apiUrl + "/message/" + id, JsonNode.class);
}

public void borrarTodosLosMensajes() {
    restTemplate.delete(apiUrl + "/messages");
}
```

Esto desacopla el envio SMTP de la inspeccion del buzón:

- el correo se manda por SMTP al puerto SMTP configurado;
- luego se verifica por HTTP en Mailpit.

## Estados de la notificacion y semantica

Enum:

- `src/main/java/com/banquito/switchpagos/report/enums/EstadoEnvioNotificacion.java`

Valores:

- `PENDIENTE`
- `ENVIADA`
- `ERROR`
- `CANCELADA`

Uso real actual:

- `PENDIENTE`: lista para enviar.
- `ENVIADA`: `JavaMailSender.send(...)` no fallo.
- `ERROR`: correo invalido o excepcion de envio.
- `CANCELADA`: definida en enum pero no usada en este flujo.

## Tipo de notificacion

Enum:

- `src/main/java/com/banquito/switchpagos/report/enums/TipoNotificacion.java`

Valores:

- `PAGO_EXITOSO`
- `PAGO_RECHAZADO`
- `PAGO_REVERSADO`

Uso real actual:

- el flujo SMTP implementado solo utiliza `PAGO_EXITOSO`.

## De donde sale el correo del beneficiario

La cadena de datos es esta:

1. `DetalleArchivoPagoInternalDto.correoNotificacion` entra al backend desde el archivo.
2. `LineaPagoMapper.toEntity(...)` lo mueve a `LineaPago.correoNotificacion` en `LineaPagoMapper.java:25`.
3. `LineaPago` lo persiste en la columna `correo_notificacion` de `LINEA_PAGO` en `LineaPago.java:46`.
4. `LineaPagoMapper.toInternalDto(...)` lo expone como `LineaPagoInternalDto.correoNotificacion()` en `LineaPagoMapper.java:41`.
5. `NotificacionServiceImpl` lo toma desde el DTO interno y lo usa para crear `NotificacionBeneficiario`.

Esto es importante: si en la rama destino el flujo de carga o mapping de `correoNotificacion` cambia, la notificacion dejara de encontrar destinatarios.

## Decisiones de diseño actuales que conviene no tocar si se busca compatibilidad

- mantener `JavaMailSender` autoconfigurado en lugar de crear un cliente SMTP custom;
- mantener `SimpleMailMessage` en lugar de migrar a `MimeMessage`;
- conservar el disparo del envio desde `generarReporteNovedades(...)`;
- conservar la tabla `NOTIFICACION_BENEFICIARIO` como bitacora de intentos;
- conservar la validacion basica de correo si se desea mismo resultado funcional;
- conservar el endpoint `/test-email` como prueba operativa;
- conservar la inspeccion via Mailpit HTTP si se quiere la misma trazabilidad manual.

## Limitaciones y riesgos actuales

### Sin retry real

Aunque hay campos `reintentos` y `proximo_reintento_en`, no existe logica de:

- incremento de reintentos;
- reprogramacion;
- backoff;
- scheduler automatico.

### Sin control de concurrencia de envio

Dos ejecuciones concurrentes de `send-pending` podrian intentar procesar los mismos `PENDIENTE` si corren al mismo tiempo.

### Sin plantillas ni HTML

El cuerpo del correo es texto plano con JSON serializado. Si otra rama quiere “mejorarlo” a HTML, eso cambia el comportamiento observable.

### Sin distincion de errores SMTP

Todo error cae en `catch (Exception e)`. No hay manejo diferenciado por codigo SMTP o tipo de fallo.

### Efecto externo dentro de transaccion

El envio no esta desacoplado de la transaccion. Puede haber correo enviado con rollback posterior en BD.

### Duplicidad protegida solo a nivel servicio

La proteccion `existsByLineaPagoIdLinea(...)` evita crear duplicados en el flujo normal, pero no se ve en este codigo una restriccion unica de base de datos que lo garantice por si sola.

## Recomendacion concreta para adaptarlo a otra rama

Si el objetivo es que funcione igual sin modificar mucho, la rama destino deberia copiar o preservar estos elementos casi intactos:

1. Dependencia `spring-boot-starter-mail`.
2. Propiedades `spring.mail.*`, `spring.mail.from` y `mailpit.api.url`.
3. `NotificacionService` y `NotificacionServiceImpl`.
4. `NotificacionBeneficiario`, `EstadoEnvioNotificacion`, `TipoNotificacion`.
5. `NotificacionBeneficiarioRepository` con los mismos metodos.
6. `NotificacionBeneficiarioMapper`.
7. `MailpitService` y `MailpitServiceImpl`.
8. `NotificationTestController`.
9. Llamada a `notificacionService.registrarNotificacionesBeneficiarios(uuidLote)` desde `ReporteLoteServiceImpl`.
10. Campo `correoNotificacion` en `LineaPago` y su mapping a `LineaPagoInternalDto`.

Si se cambia cualquiera de esos puntos, ya no se replica fielmente el comportamiento actual.

## Resumen ejecutivo tecnico

- El backend envia correos usando `JavaMailSender` de Spring Boot.
- Usa configuracion externa via `.env` mapeada a `spring.mail.*`.
- El mensaje es `SimpleMailMessage`, por lo tanto solo texto plano.
- El flujo funcional normal se dispara al generar el reporte de novedades de un lote cerrado.
- Primero persiste una notificacion en `NOTIFICACION_BENEFICIARIO`, luego intenta enviarla por SMTP.
- El resultado del envio se guarda como `PENDIENTE`, `ENVIADA` o `ERROR`.
- Mailpit se usa como servidor de pruebas y como API HTTP para inspeccionar mensajes.
- Existen endpoints tecnicos para probar envio, reenviar pendientes, inspeccionar mensajes y limpiar la bandeja.
- La forma menos riesgosa de adaptarlo a otra rama es conservar intactos servicio, entidad, repositorio, propiedades y punto de disparo funcional.
