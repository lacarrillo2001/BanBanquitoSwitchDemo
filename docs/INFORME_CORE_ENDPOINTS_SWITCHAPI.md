# Informe para Core: endpoints requeridos en `integration/switchapi`

## Resumen ejecutivo

Con las pruebas manuales ejecutadas el 2026-05-15, se confirma que el Core ya permite avanzar con una integracion real parcial desde el Switch:

- La validacion de empresa por RUC funciona.
- La consulta de disponibilidad de cuenta funciona y devuelve `saldoDisponible`.
- La transferencia Switch funciona y devuelve UUIDs de debito, credito y grupo.
- La liquidacion de comision e IVA funciona y devuelve los tres UUIDs contables.

Sin embargo, para que el Switch no dependa de endpoints internos de dominios del Core (`clientes`, `cuentas`, `credenciales-web`, `feriados`, `cuentas-institucionales`) se recomienda concentrar en `com.banquito.core.integration.switchapi` todos los contratos que necesita el Switch.

Como el equipo Core puede tardar en responder, la prioridad debe ser separar:

- Dependencias 100% Core: reglas que solo el Core puede validar con autoridad, como titularidad de cuenta destino, estado/bloqueo real de cuenta, sobregiro autorizado en liquidacion, idempotencia financiera y afectacion de saldos.
- Logica que el Switch puede manejar temporalmente: orquestacion, mapeo de errores, validacion de montos, validacion de archivo, reglas de lote, uso de endpoints internos ya existentes cuando no haya alternativa en `switchapi`.

## Pruebas manuales confirmadas

### 1. Validacion de empresa

Endpoint:

```http
GET /api/v1/core/integracion-switch/empresas/1790000001001/validacion
```

Respuesta confirmada:

```json
{
  "success": true,
  "message": "Validacion de empresa",
  "data": {
    "ruc": "1790000001001",
    "existe": true,
    "estado": "ACTIVO",
    "activoPagosMasivos": true
  }
}
```

Diagnostico: funcional, pero la respuesta deberia ser mas explicita para integracion.

### 2. Validacion de cuenta destino

Endpoint:

```http
GET /api/v1/core/integracion-switch/cuentas/0010000000002/validacion-destino
```

Respuesta confirmada:

```json
{
  "success": true,
  "message": "Validacion de cuenta destino",
  "data": {
    "numeroCuenta": "0010000000002",
    "estado": "ACTIVA",
    "saldoContable": 257.5000,
    "saldoDisponible": 257.5000,
    "permiteDebito": true
  }
}
```

Diagnostico: confirma existencia y estado, pero no confirma titularidad contra identificacion del beneficiario ni permiso de deposito.

### 3. Transferencia Switch

Endpoint:

```http
POST /api/v1/core/integracion-switch/transacciones/transferencia
```

Respuesta confirmada:

```json
{
  "success": true,
  "message": "Transferencia Switch procesada",
  "data": {
    "estado": "EXITOSA",
    "uuidDebitoCore": "eeb9b237-9605-4578-9c5a-2e55e854ef8a",
    "uuidCreditoCore": "b92bc866-575c-4c05-8172-9e11e671bcd5",
    "uuidGrupoOperacion": "b3cfa545-1579-4eeb-ba3d-db25433af4fa",
    "saldoDisponibleOrigen": 252.7500
  }
}
```

Diagnostico: suficiente para reemplazar el stub en procesamiento de linea, adaptando el Switch a una transferencia atomica en lugar de debito y credito separados.

### 4. Liquidacion de servicio

Endpoint:

```http
POST /api/v1/core/integracion-switch/transacciones/liquidacion-servicio
```

Respuesta confirmada:

```json
{
  "success": true,
  "message": "Liquidacion procesada",
  "data": {
    "estado": "APLICADA",
    "uuidDebitoMatriz": "b0db66ae-5fad-4e1a-9dee-71bcfa7ca288",
    "uuidCreditoIngresos": "fa144ba8-a018-474d-9d28-8a96f21ce348",
    "uuidCreditoIva": "18e9d2b0-934a-48a1-88fb-eb4b9b24871d",
    "uuidGrupoOperacion": "bd90c7af-4fc2-4970-b6a6-fcac639d39a9"
  }
}
```

Diagnostico: suficiente para una primera integracion, pero debe aclararse y garantizarse el comportamiento de sobregiro cuando `permiteSobregiro=true`.

## Endpoints que ya estan en `switchapi`

| Endpoint | Estado actual | Necesidad de ajuste |
|---|---|---|
| `GET /api/v1/core/integracion-switch/empresas/{ruc}/validacion` | Funciona | Ampliar respuesta para evitar inferencias en Switch. |
| `GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/disponibilidad` | Funciona | Ampliar respuesta para cuenta matriz: titular, estado, permite debito, saldo disponible. |
| `GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino` | Parcial | Debe recibir identificacion del beneficiario y validar titularidad. |
| `POST /api/v1/core/integracion-switch/transacciones/transferencia` | Funciona | Mantener como contrato principal para pago por linea. |
| `POST /api/v1/core/integracion-switch/transacciones/liquidacion-servicio` | Funciona | Garantizar uso de `permiteSobregiro`. |
| `GET /api/v1/core/integracion-switch/feriados/siguiente-dia-habil` | Parcial | Agregar endpoint para saber si la fecha actual es habil. |

## Que necesitamos que devuelva cada endpoint de `switchapi`

### Validar empresa emisora

Endpoint actual:

```http
GET /api/v1/core/integracion-switch/empresas/{ruc}/validacion
```

Respuesta recomendada:

```json
{
  "ruc": "1790000001001",
  "existe": true,
  "tipoCliente": "JURIDICO",
  "estado": "ACTIVO",
  "activoPagosMasivos": true,
  "credencialWebValida": true,
  "habilitada": true,
  "codigo": "EMPRESA_HABILITADA",
  "mensaje": "Empresa habilitada para pagos masivos."
}
```

Prioridad: media. El Switch puede avanzar con la respuesta actual para RUC, estado y pagos masivos. La credencial web empresarial si depende del Core si el canal Web requiere validacion estricta.

### Consultar y validar cuenta matriz

Endpoint recomendado:

```http
GET /api/v1/core/integracion-switch/empresas/{ruc}/cuentas/{numeroCuenta}/validacion-matriz
```

Proposito:

Validar en una sola llamada que la cuenta matriz exista, pertenezca a la empresa emisora, este activa para debitos y devolver saldo disponible.

Respuesta recomendada:

```json
{
  "numeroCuenta": "0010000000001",
  "rucEmpresa": "1790000001001",
  "existe": true,
  "perteneceEmpresa": true,
  "estado": "ACTIVA",
  "permiteDebito": true,
  "saldoContable": 300.00,
  "saldoDisponible": 252.75,
  "permiteSobregiro": false,
  "limiteSobregiro": 0.00,
  "valida": true,
  "codigo": "CUENTA_MATRIZ_VALIDA",
  "mensaje": "Cuenta matriz valida para pagos masivos."
}
```

Prioridad: alta. Temporalmente el Switch podria consultar `/clientes/identificacion/{ruc}` y `/cuentas/numero/{numeroCuenta}`, pero eso acopla el Switch a contratos internos del Core.

### Validar cuenta destino

Endpoint actual:

```http
GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino
```

Ajuste recomendado:

```http
GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino?identificacionBeneficiario={identificacion}
```

Proposito:

Validar que la cuenta destino exista, pertenezca al beneficiario indicado, este activa y permita depositos.

Respuesta recomendada:

```json
{
  "numeroCuenta": "0010000000002",
  "identificacionBeneficiario": "1710000001",
  "existe": true,
  "perteneceBeneficiario": true,
  "estado": "ACTIVA",
  "permiteDeposito": true,
  "bloqueada": false,
  "valida": true,
  "codigo": "CUENTA_DESTINO_VALIDA",
  "mensaje": "Cuenta destino valida para recibir pagos."
}
```

Prioridad: critica. Esta es una regla bancaria que el Switch no deberia simular. Sin esto, el Switch puede avanzar solo con una validacion parcial.

### Transferencia de pago por linea

Endpoint actual:

```http
POST /api/v1/core/integracion-switch/transacciones/transferencia
```

Request esperado:

```json
{
  "cuentaOrigen": "0010000000001",
  "cuentaDestino": "0010000000002",
  "codigoSubtipo": "PAGO_MASIVO",
  "monto": 1.00,
  "uuidOperacion": "uuid-operacion-switch",
  "uuidGrupoOperacion": "uuid-grupo-switch",
  "referenciaExterna": "uuid-lote-o-referencia",
  "descripcion": "Pago masivo linea 1",
  "canalOrigen": null,
  "fechaNegocio": "2026-05-15",
  "usuarioCoreId": null,
  "credencialWebId": null
}
```

Respuesta actual suficiente:

```json
{
  "estado": "EXITOSA",
  "uuidDebitoCore": "uuid",
  "uuidCreditoCore": "uuid",
  "uuidGrupoOperacion": "uuid",
  "saldoDisponibleOrigen": 252.75
}
```

Prioridad: ya cubierto. El Switch debe adaptarse a este contrato.

### Liquidacion de servicio

Endpoint actual:

```http
POST /api/v1/core/integracion-switch/transacciones/liquidacion-servicio
```

Request recomendado:

```json
{
  "uuidGrupoOperacion": "uuid-liquidacion-switch",
  "cuentaMatriz": "0010000000001",
  "subtotalComision": 1.00,
  "montoIva": 0.15,
  "totalDebitado": 1.15,
  "permiteSobregiro": true,
  "codigoCuentaIngresos": "INGRESOS_SERVICIOS_MASIVOS",
  "codigoCuentaIva": "PASIVOS_IVA_RETENIDO",
  "referenciaExterna": "uuid-lote"
}
```

Respuesta actual suficiente:

```json
{
  "estado": "APLICADA",
  "uuidDebitoMatriz": "uuid",
  "uuidCreditoIngresos": "uuid",
  "uuidCreditoIva": "uuid",
  "uuidGrupoOperacion": "uuid"
}
```

Ajuste requerido:

El Core debe confirmar que `permiteSobregiro=true` se respeta aunque el saldo disponible remanente no alcance para comision e IVA.

Prioridad: critica si las pruebas con cuenta sin saldo fallan. Si el Core ya permite sobregiro por configuracion de cuenta, aun se debe documentar el criterio.

### Dia habil

Endpoint actual:

```http
GET /api/v1/core/integracion-switch/feriados/siguiente-dia-habil?fecha=2026-05-15
```

Nuevo endpoint recomendado:

```http
GET /api/v1/core/integracion-switch/calendario/dia-habil?fecha=2026-05-15
```

Respuesta recomendada:

```json
{
  "fecha": "2026-05-15",
  "esDiaHabil": true,
  "esFinSemana": false,
  "esFeriado": false,
  "siguienteDiaHabil": "2026-05-18",
  "codigo": "DIA_HABIL",
  "mensaje": "La fecha indicada es dia habil."
}
```

Prioridad: media. Temporalmente el Switch puede seguir usando su logica sabado/domingo y mejorar luego con feriados desde Core. Para cumplir feriados reales, depende del Core.

## Endpoints fuera de `switchapi` que deberian moverse o encapsularse

| Endpoint fuera de `switchapi` | Uso para Switch | Recomendacion |
|---|---|---|
| `GET /api/v1/core/clientes/identificacion/{identificacion}` | Obtener `clienteId`, tipo, estado y pagos masivos | No consumir directamente desde Switch; encapsular en validacion empresa o matriz. |
| `GET /api/v1/core/clientes/ruc/{ruc}/validacion-pagos-masivos` | Validacion simple de empresa | Ya existe alternativa en `switchapi`; mantener `switchapi` como contrato oficial. |
| `GET /api/v1/core/cuentas/numero/{numeroCuenta}` | Obtener `clienteId`, estado, saldo, sobregiro, favorita pagos | Encapsular en `validacion-matriz` y `validacion-destino`. |
| `GET /api/v1/core/cuentas/numero/{numeroCuenta}/saldo` | Saldo disponible | Ya esta cubierto por `switchapi/disponibilidad`. |
| `GET /api/v1/core/credenciales-web/{username}` | Validar credencial empresarial | Encapsular en validacion empresa o nuevo endpoint de credencial Switch. |
| `GET /api/v1/core/credenciales-web/{username}/validacion` | Validar estado de credencial | Encapsular, porque falta relacion clara con RUC. |
| `GET /api/v1/core/feriados/siguiente-dia-habil` | Calendario operativo | Encapsular en calendario Switch con respuesta completa. |
| `GET /api/v1/core/cuentas-institucionales/codigo/{codigo}` | Validar cuentas institucionales | Puede quedar interno si liquidacion ya valida y rechaza correctamente. |

## Nuevos endpoints solicitados

### 1. Validacion de cuenta matriz por empresa

```http
GET /api/v1/core/integracion-switch/empresas/{ruc}/cuentas/{numeroCuenta}/validacion-matriz
```

Debe regresar:

- `existe`
- `perteneceEmpresa`
- `estado`
- `permiteDebito`
- `saldoDisponible`
- `saldoContable`
- `permiteSobregiro`
- `limiteSobregiro`
- `valida`
- `codigo`
- `mensaje`

Motivo: evita que el Switch consulte `clientes` y `cuentas` por separado.

### 2. Validacion de cuenta destino con identificacion

```http
GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino?identificacionBeneficiario={identificacion}
```

Debe regresar:

- `existe`
- `perteneceBeneficiario`
- `estado`
- `permiteDeposito`
- `bloqueada`
- `valida`
- `codigo`
- `mensaje`

Motivo: es indispensable para rechazar lineas con beneficiario incorrecto, cuenta bloqueada o cuenta inactiva.

### 3. Consulta de dia habil

```http
GET /api/v1/core/integracion-switch/calendario/dia-habil?fecha={yyyy-MM-dd}
```

Debe regresar:

- `fecha`
- `esDiaHabil`
- `esFinSemana`
- `esFeriado`
- `siguienteDiaHabil`
- `codigo`
- `mensaje`

Motivo: el endpoint actual calcula siguiente dia habil, pero no responde directamente si la fecha consultada es habil.

### 4. Validacion de credencial empresarial para pagos masivos

```http
GET /api/v1/core/integracion-switch/empresas/{ruc}/credenciales/{username}/validacion
```

Debe regresar:

- `ruc`
- `username`
- `existe`
- `perteneceEmpresa`
- `estado`
- `valida`
- `codigo`
- `mensaje`

Motivo: evita que el Switch valide una credencial activa que no corresponda a la empresa emisora.

## Priorizacion para no bloquear al Switch

### Critico: pedir a Core antes de cerrar integracion productiva

1. Validacion de cuenta destino contra identificacion del beneficiario.
2. Garantia explicita de sobregiro en liquidacion cuando `permiteSobregiro=true`.
3. Validacion de cuenta matriz contra RUC empresa.

Estas reglas dependen de la fuente de verdad del Core y no deberian quedar como inferencias permanentes en Switch.

### Alto: el Switch puede avanzar con workaround temporal

1. Validacion de empresa con respuesta actual.
2. Saldo disponible de cuenta matriz.
3. Transferencia por linea usando endpoint unico.
4. Liquidacion usando endpoint actual.

Workaround: el Switch puede mapear los DTOs actuales y, si es necesario, consumir temporalmente endpoints de `clientes` y `cuentas` para validar titularidad de matriz. Para destino, el workaround es mas debil porque el endpoint actual no devuelve titular.

### Medio: puede esperar si se documenta la deuda

1. Dia habil real con feriados.
2. Credencial web empresarial vinculada a RUC.
3. Respuestas enriquecidas con codigos y mensajes de negocio.

## Plan recomendado para el equipo Switch

Mientras Core responde, avanzar asi:

1. Implementar cliente HTTP real para `POST /integracion-switch/transacciones/transferencia`.
2. Adaptar `CoreBancarioService.ejecutarDebito` y `ejecutarCredito` a una operacion interna de transferencia unica, para no duplicar movimientos.
3. Usar `GET /integracion-switch/cuentas/{cuenta}/disponibilidad` para saldo disponible.
4. Usar `GET /integracion-switch/cuentas/{cuenta}/validacion-destino` solo como validacion parcial de existencia y estado.
5. Usar `POST /integracion-switch/transacciones/liquidacion-servicio` para comision e IVA.
6. Mapear errores HTTP y `ApiResponse.success=false` a `codigoError` y `mensajeError`.
7. Dejar marcada la deuda: titularidad de cuenta destino y matriz-RUC deben pasar a contratos Core.

## Decision final

Se puede avanzar con integracion real en Switch usando los endpoints actuales, pero el alcance debe declararse como integracion real parcial:

- Real para saldo, transferencia, UUIDs, idempotencia financiera y liquidacion.
- Parcial para validaciones de titularidad, permisos de deposito, dia habil real y credencial empresarial.

La prioridad para Core no es crear todo perfecto, sino entregar o ajustar los endpoints que el Switch no puede resolver con autoridad bancaria.
