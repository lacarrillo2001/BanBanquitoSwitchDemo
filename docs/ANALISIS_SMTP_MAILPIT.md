# Analisis tecnico del servidor SMTP / Mailpit

## Resumen

El backend usa Spring Boot Mail para enviar correos mediante `JavaMailSender`.
La configuracion SMTP no esta hardcodeada en Java; se carga desde variables de entorno definidas en `.env` y enlazadas desde `src/main/resources/application.properties`.

Con la configuracion encontrada en el repositorio, el sistema esta preparado para enviar correos a un servidor tipo Mailpit de pruebas, sin autenticacion SMTP y sin STARTTLS.

## Donde se carga la configuracion

La aplicacion importa variables desde el archivo `.env` con esta propiedad:

- `src/main/resources/application.properties:2`
  - `spring.config.import=optional:file:.env[.properties]`

Las propiedades SMTP definidas en `src/main/resources/application.properties` son:

- `src/main/resources/application.properties:22`
  - `spring.mail.host=${SPRING_MAIL_HOST}`
- `src/main/resources/application.properties:23`
  - `spring.mail.port=${SPRING_MAIL_PORT:1025}`
- `src/main/resources/application.properties:24`
  - `spring.mail.username=${SPRING_MAIL_USERNAME:}`
- `src/main/resources/application.properties:25`
  - `spring.mail.password=${SPRING_MAIL_PASSWORD:}`
- `src/main/resources/application.properties:26`
  - `spring.mail.properties.mail.smtp.auth=${SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH:false}`
- `src/main/resources/application.properties:27`
  - `spring.mail.properties.mail.smtp.starttls.enable=${SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE:false}`
- `src/main/resources/application.properties:28`
  - `spring.mail.from=${SPRING_MAIL_FROM}`

Adicionalmente, la URL de la API HTTP de Mailpit se configura en:

- `src/main/resources/application.properties:31`
  - `mailpit.api.url=${MAILPIT_API_URL}`

## Valores encontrados en el `.env`

Los valores actualmente presentes en el archivo `.env` del repositorio son:

- `.env:34`
  - `SPRING_MAIL_HOST=34.27.240.236`
- `.env:37`
  - `SPRING_MAIL_PORT=1025`
- `.env:40`
  - `SPRING_MAIL_USERNAME=`
- `.env:43`
  - `SPRING_MAIL_PASSWORD=`
- `.env:46`
  - `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=false`
- `.env:49`
  - `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=false`
- `.env:52`
  - `SPRING_MAIL_FROM=notificaciones@banquito.com`
- `.env:55`
  - `MAILPIT_API_URL=http://34.27.240.236:8025/api/v1`

## Interpretacion tecnica de la configuracion

Con esos valores, el sistema queda configurado asi:

- Host SMTP: `34.27.240.236`
- Puerto SMTP: `1025`
- Usuario SMTP: vacio
- Password SMTP: vacio
- Autenticacion SMTP: deshabilitada
- STARTTLS: deshabilitado
- Remitente logico: `notificaciones@banquito.com`
- API HTTP de inspeccion: `http://34.27.240.236:8025/api/v1`

Esto corresponde a un entorno de pruebas o laboratorio, no a un relay SMTP corporativo endurecido.

## Evidencia de que el proyecto usa Mailpit

Existe evidencia documental y de codigo de que el servidor esperado es Mailpit:

### Documento de infraestructura

En `requisitos/Cre_PostgresClou.txt` aparece esta referencia:

- `requisitos/Cre_PostgresClou.txt:18`
  - `servidor smtp`
- `requisitos/Cre_PostgresClou.txt:22`
  - `http://34.27.240.236:8025/`
- `requisitos/Cre_PostgresClou.txt:23`
  - `http://34.27.240.236:1025/`
- `requisitos/Cre_PostgresClou.txt:27`
  - `1025 = SMTP (envio de correos)`
- `requisitos/Cre_PostgresClou.txt:30`
  - `8025 = Web UI + API HTTP`

Ese esquema coincide con Mailpit:

- puerto `1025`: servidor SMTP
- puerto `8025`: interfaz web y API HTTP

### Cliente HTTP para Mailpit en el backend

El backend tiene un servicio dedicado a consultar la API HTTP de Mailpit:

- `src/main/java/com/banquito/switchpagos/report/service/impl/MailpitServiceImpl.java:15`
  - recibe `mailpit.api.url`
- `src/main/java/com/banquito/switchpagos/report/service/impl/MailpitServiceImpl.java:21`
  - lista mensajes con `GET /messages`
- `src/main/java/com/banquito/switchpagos/report/service/impl/MailpitServiceImpl.java:26`
  - obtiene un mensaje con `GET /message/{id}`
- `src/main/java/com/banquito/switchpagos/report/service/impl/MailpitServiceImpl.java:31`
  - borra mensajes con `DELETE /messages`

## Dependencia usada para SMTP

La aplicacion incorpora el starter oficial de Spring para correo:

- `pom.xml:85`
  - `spring-boot-starter-mail`

No hay una clase de configuracion manual para SMTP; se usa la autoconfiguracion de Spring Boot para construir `JavaMailSender` a partir de `spring.mail.*`.

## Donde se hace el envio real del correo

El envio real ocurre en:

- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:102`

El flujo interno es:

1. Se crea un `SimpleMailMessage`.
2. Se asigna destinatario con `message.setTo(...)`.
3. Se asigna asunto con `message.setSubject(...)`.
4. Se asigna cuerpo con `message.setText(...)`.
5. Se asigna remitente con `message.setFrom(remitenteNotificaciones)`.
6. Se ejecuta `mailSender.send(message)`.

Referencias puntuales:

- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:104`
  - `SimpleMailMessage message = new SimpleMailMessage();`
- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:105`
  - `message.setTo(notificacion.getCorreoDestino());`
- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:106`
  - `message.setSubject(notificacion.getAsunto());`
- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:107`
  - `message.setText("Notificacion de Pago BanQuito: " + notificacion.getContenido().toString());`
- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:108`
  - `message.setFrom(remitenteNotificaciones);`
- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:110`
  - `mailSender.send(message);`

## Tipo de correo que soporta hoy

La implementacion actual usa `SimpleMailMessage`, por lo tanto:

- envia texto plano;
- no envia HTML;
- no maneja adjuntos;
- no usa `MimeMessage`;
- no incluye imagenes embebidas.

## Flujo funcional que dispara el envio SMTP

### Flujo normal de negocio

El envio de notificaciones no nace directamente en el procesamiento del lote. Se activa al generar el reporte de novedades del lote cerrado.

Punto de entrada:

- `src/main/java/com/banquito/switchpagos/report/controller/ReporteLoteController.java:25`
  - `GET /api/v1/pagos-masivos/lotes/{uuidLote}/novedades`

Ejecucion interna:

- `src/main/java/com/banquito/switchpagos/report/service/impl/ReporteLoteServiceImpl.java:98`
  - `notificacionService.registrarNotificacionesBeneficiarios(uuidLote);`

Ese metodo realiza dos acciones:

1. Busca lineas de pago exitosas del lote.
2. Registra notificaciones y luego envia las pendientes.

Referencias:

- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:68`
  - `registrarNotificacionesBeneficiarios(UUID uuidLote)`
- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:72`
  - `enviarNotificacionesPendientes();`
- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:97`
  - `enviarNotificacionesPendientes()`

### Flujo de prueba manual

Existe un endpoint de prueba que crea una notificacion y usa el mismo mecanismo SMTP:

- `src/main/java/com/banquito/switchpagos/report/controller/NotificationTestController.java:33`
  - `POST /api/v1/report/notifications/test-email?email=...`

Este endpoint termina llamando:

- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:162`
  - `enviarEmailPruebaDirecto(...)`

## Endpoints tecnicos relacionados con Mailpit y pruebas

En `src/main/java/com/banquito/switchpagos/report/controller/NotificationTestController.java` existen estos endpoints:

- `POST /api/v1/report/notifications/send-pending`
  - fuerza el envio de notificaciones pendientes
- `POST /api/v1/report/notifications/test-email?email=destino@correo.com`
  - crea y envia un correo de prueba
- `GET /api/v1/report/notifications/mailpit/messages`
  - lista mensajes recibidos por Mailpit
- `GET /api/v1/report/notifications/mailpit/messages/{id}`
  - consulta detalle de un mensaje en Mailpit
- `DELETE /api/v1/report/notifications/mailpit/messages`
  - elimina todos los mensajes en Mailpit
- `GET /api/v1/report/notifications/db-status`
  - resume lotes, lineas y notificaciones en la base de datos

## Persistencia y trazabilidad del envio

La entidad de persistencia usada es:

- `src/main/java/com/banquito/switchpagos/report/model/NotificacionBeneficiario.java:25`
  - tabla `switch_banquito.NOTIFICACION_BENEFICIARIO`

Campos relevantes para seguimiento:

- `correo_destino`
- `asunto`
- `contenido`
- `estado_envio`
- `fecha_envio`
- `error_envio`
- `reintentos`

Comportamiento al enviar:

- si `mailSender.send(...)` termina sin excepcion:
  - `estado_envio = ENVIADA`
  - se llena `fecha_envio`
  - `error_envio = null`
- si ocurre una excepcion:
  - `estado_envio = ERROR`
  - `error_envio = e.getMessage()`

Ademas se registra auditoria desde:

- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:145`

## Validaciones actuales sobre el correo destino

La validacion de correo es minima:

- `src/main/java/com/banquito/switchpagos/report/service/impl/NotificacionServiceImpl.java:141`
  - solo verifica que el string contenga `@` y `.`

No existe actualmente:

- validacion RFC estricta;
- verificacion MX;
- validacion de dominio;
- control de rebotes.

## Credenciales de conexion

Con lo encontrado en el repositorio:

- no hay usuario SMTP configurado;
- no hay password SMTP configurado;
- la propiedad `mail.smtp.auth` esta en `false`;
- la propiedad `mail.smtp.starttls.enable` esta en `false`.

Por tanto, la conexion esperada es una sesion SMTP simple, sin autenticacion y sin cifrado STARTTLS.

## Lo que no esta versionado en el repositorio

No se encontraron en el proyecto:

- `docker-compose.yml`;
- manifiestos Kubernetes;
- Terraform;
- scripts de provisionamiento del servicio SMTP/Mailpit;
- definiciones de systemd o Windows Service;
- configuracion de firewall o balanceadores.

Por eso, desde este repositorio no se puede afirmar con certeza:

- en que VM o contenedor esta montado hoy el SMTP;
- como fue levantado Mailpit;
- si la IP actual en runtime sigue siendo la misma;
- si existe NAT, proxy o balanceador delante del servicio.

## Inconsistencia documental detectada

En `README.md:26` se indica:

- `SMTP: simulado; las notificaciones pendientes se marcan como ENVIADA si el correo tiene formato basico valido, o ERROR si no.`

Sin embargo, el codigo actual si ejecuta un envio real por `JavaMailSender` contra un host SMTP configurado.

Eso sugiere que el `README.md` esta desactualizado respecto a la implementacion actual.

## Conclusiones tecnicas

- El sistema usa `JavaMailSender` autoconfigurado por Spring Boot.
- La configuracion se carga desde `.env` a traves de `application.properties`.
- El host y puerto SMTP configurados en el repositorio son `34.27.240.236:1025`.
- La inspeccion de mensajes se hace via Mailpit API en `http://34.27.240.236:8025/api/v1`.
- No hay credenciales SMTP configuradas.
- No hay STARTTLS configurado.
- El envio real se hace con `SimpleMailMessage`, solo en texto plano.
- No existe en este repo evidencia de la infraestructura exacta donde esta montado el servicio.
