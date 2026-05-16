# Integracion real Core - Switch

## Configuracion

El Switch consume el Core por HTTP desde el modulo `integrationcore`.

```properties
core.base-url=http://localhost:8081
core.integration.mode=rest
core.integration.codigo-subtipo-pago-masivo=PAGO_MASIVO
core.integration.codigo-cuenta-ingresos=INGRESOS_SERVICIOS_MASIVOS
core.integration.codigo-cuenta-iva=IVA_SERVICIOS_MASIVOS
core.integration.numero-cuenta-ingresos=9000000001
core.integration.numero-cuenta-iva=9000000002
core.integration.mock-autenticacion=true
core.integration.mock-cuenta-favorita-pagos=true
```

Para volver al stub local:

```properties
core.integration.mode=stub
```

## Endpoints Core usados

- `GET /api/v1/core/integracion-switch/empresas/{ruc}/validacion`
- `GET /api/v1/core/integracion-switch/empresas/{ruc}/cuenta-favorita-pagos`
- `GET /api/v1/core/integracion-switch/empresas/{ruc}/cuentas/{numeroCuenta}/validacion-matriz`
- `GET /api/v1/core/integracion-switch/empresas/{ruc}/credenciales/{username}/validacion`
- `GET /api/v1/core/integracion-switch/calendario/dia-habil?fecha={fecha}`
- `GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/disponibilidad`
- `GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino?identificacionBeneficiario={identificacion}`
- `POST /api/v1/core/integracion-switch/transacciones/transferencia`
- `POST /api/v1/core/integracion-switch/transacciones/liquidacion-servicio`
- `POST /api/v1/core/integracion-switch/autenticacion/login`

Nota temporal: en la version oficial del Core recibida todavia faltan `cuenta-favorita-pagos` y `autenticacion/login`. Por eso el Switch permite mockear solo esos dos contratos con:

```properties
core.integration.mock-autenticacion=true
core.integration.mock-cuenta-favorita-pagos=true
```

Cuando el Core entregue esos endpoints, cambiar ambas propiedades a `false`.

## Comportamiento

- El contrato interno `CoreBancarioService` encapsula toda la comunicacion HTTP con Core.
- La validacion global de lote consulta en Core que la empresa exista, sea juridica, este activa y tenga pagos masivos habilitado.
- La carga web/API usa la cuenta matriz enviada por el frontend y exige que coincida con la cabecera del archivo.
- La validacion global de lotes web/API consulta en Core que la cuenta matriz pertenezca al RUC emisor, este activa para debitos y tenga saldo disponible.
- La carga SFTP resuelve la cuenta matriz desde `cuenta-favorita-pagos`; la cuenta del archivo no se usa como cuenta operativa para SFTP.
- Para SFTP, la validacion de cuenta favorita es autoritativa. No se revalida con `validacion-matriz`, porque el contrato de favorita ya devuelve `valida`, `estado`, `permiteDebito` y `saldoDisponible`.
- La carga de lote consulta en Core la credencial empresarial cuando se recibe `usernameCredencialWebCore`.
- La decision `RECIBIDO`/`ENCOLADO` y la fecha programada de cola consultan el calendario del Core, incluyendo fines de semana y feriados.
- En modo `rest`, `ejecutarDebito` ejecuta una transferencia real en Core y guarda temporalmente el resultado por `uuidOperacionSwitch`.
- En modo `rest`, `ejecutarCredito` recupera el UUID de credito generado por esa misma transferencia real.
- La liquidacion contable usa el endpoint consolidado del Core para debito de cuenta matriz, credito a ingresos y credito a IVA.
- Los codigos institucionales se envian al Core; los numeros institucionales se guardan en el detalle local del Switch para respetar la longitud de `CUENTA_DESTINO_CORE`.
- Los errores de regla de negocio del Core se mapean a codigos del Switch.
- Si el Core no esta disponible, se usa `CORE_NO_DISPONIBLE`.

## Datos de prueba recomendados

Con el seed funcional del Core:

- Empresa: `1790000001001`
- Credencial web: `empresa001`
- Cuenta matriz: `0010000000601`
- Beneficiarios: `1700000002`, `1700000003`, `1700000004`
- Cuentas destino: `0010000000002`, `0010000000003`, `0010000000004`

La coleccion Postman del Switch y los archivos en `postman/examples` fueron alineados con esos datos.

## Prueba local

1. Levantar Core en `http://localhost:8081`.
2. Levantar Switch con `core.integration.mode=rest`.
3. Importar `postman/Switch_Pagos_Masivos_BanQuito.postman_collection.json`.
4. Ejecutar el flujo principal:
   - Cargar lote valido, enviando `usernameCredencialWebCore=empresa001` si se quiere validar credencial empresarial contra Core.
   - Validar lote; aqui se validan empresa y cuenta matriz contra Core.
   - Procesar lote.
   - Liquidar lote.
   - Consultar novedades y comprobante.

## Lotes encolados

El Switch administra la cola en la tabla `COLA_PROCESAMIENTO`. El Core solo informa si una fecha es habil y cual es el siguiente dia habil.

- El scheduler interno esta habilitado con `switch.cola.scheduler.enabled=true`.
- Cada ciclo corre segun `switch.cola.scheduler.fixed-delay-ms`, por defecto cada 60000 ms.
- Procesa registros `PENDIENTE` o `REINTENTO` cuya `FECHA_PROGRAMADA_PROCESO` ya vencio.
- Para pruebas manuales sin esperar la hora programada, usar:

```http
POST /api/v1/pagos-masivos/cola/procesar-pendientes
```

Ese endpoint toma pendientes aunque `FECHA_PROGRAMADA_PROCESO` todavia no haya vencido, valida el lote y lo procesa si queda `VALIDADO`.

Propiedades relacionadas:

```properties
switch.cola.scheduler.enabled=true
switch.cola.scheduler.fixed-delay-ms=60000
switch.cola.max-lotes-por-ciclo=10
switch.cola.reintento-delay-minutos=5
```

## Login delegado y SFTP

El Switch expone un login propio para el frontend, pero no protege endpoints con sesion o JWT en esta fase:

```http
POST /api/v1/pagos-masivos/auth/login
```

Body:

```json
{
  "usuario": "empresa001",
  "contrasena": "Banquito123*"
}
```

Internamente delega al Core:

```http
POST /api/v1/core/integracion-switch/autenticacion/login
```

El Switch acepta solo respuestas con:

- `autenticado=true`
- `rolSwitch=EMPRESA_PAGOS_MASIVOS`
- `activoPagosMasivos=true`

El servidor SFTP embebido usa el mismo contrato del Core para autenticar usuario y contrasena.

Mientras `core.integration.mock-autenticacion=true`, el login se simula para `core.integration.mock-usuario-empresa`.

Propiedades:

```properties
switch.sftp.enabled=true
switch.sftp.host=0.0.0.0
switch.sftp.port=2222
switch.sftp.root-directory=sftp-inbox
switch.sftp.scan-fixed-delay-ms=10000
switch.sftp.file-settle-ms=3000
```

Prueba local:

```bash
sftp -P 2222 empresa001@localhost
put postman/examples/lote_valido.csv
```

El archivo se registra como lote con `canalIngreso=SFTP` y la cuenta matriz se toma de la cuenta favorita de pagos del Core, no de la cabecera del archivo. Si se procesa correctamente se mueve a `sftp-inbox/processed`, y si falla se mueve a `sftp-inbox/error` con un archivo `.error.txt`.

## Limitaciones

- El modo REST depende de que los datos del archivo coincidan con clientes y cuentas reales del Core.
- Si se usa otro seed, ajustar `rucEmpresa`, `cuentaMatrizCargo` y los archivos de ejemplo.
- El modelo persistente actual conserva `idCredencialWebCore` como referencia numerica historica. La validacion real por Core usa el parametro opcional `usernameCredencialWebCore`, porque el endpoint oficial del Core valida por username.
- Los mocks de `autenticacion/login` y `cuenta-favorita-pagos` son temporales y deben desactivarse cuando el Core oficial exponga esos endpoints.
