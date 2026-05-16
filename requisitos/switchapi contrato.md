#### Contrato de API: switchapi — Endpoints y Recomendaciones

##### 1\. Endpoints de switchapi — Requeridos

###### 1.1 Validar Empresa Emisora



Endpoint actual: GET /api/v1/core/integracion-switch/empresas/{ruc}/validacion   





Prioridad: Media   



Respuesta recomendada:

{

&#x20; "ruc": "1790000001001",

&#x20; "existe": true,

&#x20; "tipoCliente": "JURIDICO",

&#x20; "estado": "ACTIVO",

&#x20; "activoPagosMasivos": true,

&#x20; "credencialWebValida": true,

&#x20; "habilitada": true,

&#x20; "codigo": "EMPRESA\_HABILITADA",

&#x20; "mensaje": "Empresa habilitada para pagos masivos."

}



Prioridad: media

El Switch puede avanzar con la respuesta actual para RUC, estado y pagos masivos. La credencial web empresarial depende del Core si el canal Web requiere validacion estricta.



###### 1.2 Consultar y Validar Cuenta Matriz



Endpoint recomendado:

GET /api/v1/core/integracion-switch/empresas/{ruc}/cuentas/{numeroCuenta}/validacion-matriz



Proposito:

Validar en una sola llamada que la cuenta matriz exista, pertenezca a la empresa emisora, este activa para debitos y devolver saldo disponible.

Respuesta recomendada:

{

&#x20; "numeroCuenta": "0010000000001",

&#x20; "rucEmpresa": "1790000001001",

&#x20; "existe": true,

&#x20; "perteneceEmpresa": true,

&#x20; "estado": "ACTIVA",

&#x20; "permiteDebito": true,

&#x20; "saldoContable": 300,

&#x20; "saldoDisponible": 252.75,

&#x20; "permiteSobregiro": false,

&#x20; "limiteSobregiro": 0,

&#x20; "valida": true,

&#x20; "codigo": "CUENTA\_MATRIZ\_VALIDA",

&#x20; "mensaje": "Cuenta matriz valida para pagos masivos."

}





Prioridad: alta

Temporalmente el Switch podria consultar /clientes/identificacion/{ruc} y /cuentas/numero/{numeroCuenta}, pero eso acopla el Switch a contratos internos del Core.





###### 1.3 Validar Cuenta Destino

Endpoint actual:

GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino



Ajuste recomendado:

GET /api/v1/core/integracion-switch/cuentas/{numeroCuenta}/validacion-destino?identificacionBeneficiario={identificacion}



Proposito:

Validar que la cuenta destino exista, pertenezca al beneficiario indicado, este activa y permita depositos.

Respuesta recomendada:

{

&#x20; "numeroCuenta": "0010000000002",

&#x20; "identificacionBeneficiario": "1710000001",

&#x20; "existe": true,

&#x20; "perteneceBeneficiario": true,

&#x20; "estado": "ACTIVA",

&#x20; "permiteDeposito": true,

&#x20; "bloqueada": false,

&#x20; "valida": true,

&#x20; "codigo": "CUENTA\_DESTINO\_VALIDA",

&#x20; "mensaje": "Cuenta destino valida para recibir pagos."

}



Prioridad: critica

Esta es una regla bancaria que el Switch no deberia simular. Sin esto, el Switch puede avanzar solo con una validacion parcial.





###### 1.4 Transferencia de Pago por Linea

Endpoint actual:

POST /api/v1/core/integracion-switch/transacciones/transferencia



Request esperado:

{

&#x20; "cuentaOrigen": "0010000000001",

&#x20; "cuentaDestino": "0010000000002",

&#x20; "codigoSubtipo": "PAGO\_MASIVO",

&#x20; "monto": 1,

&#x20; "uuidOperacion": "uuid-operacion-switch",

&#x20; "uuidGrupoOperacion": "uuid-grupo-switch",

&#x20; "referenciaExterna": "uuid-lote-o-referencia",

&#x20; "descripcion": "Pago masivo linea 1",

&#x20; "canalOrigen": null,

&#x20; "fechaNegocio": "2026-05-15",

&#x20; "usuarioCoreId": null,

&#x20; "credencialWebId": null

}



Respuesta actual suficiente:

{

&#x20; "estado": "EXITOSA",

&#x20; "uuidDebitoCore": "uuid",

&#x20; "uuidCreditoCore": "uuid",

&#x20; "uuidGrupoOperacion": "uuid",

&#x20; "saldoDisponibleOrigen": 252.75

}



Prioridad: ya cubierto

El Switch debe adaptarse a este contrato.




1.5 Liquidacion de Servicio
---

Endpoint actual:

POST /api/v1/core/integracion-switch/transacciones/liquidacion-servicio



Request recomendado:

{

&#x20; "uuidGrupoOperacion": "uuid-liquidacion-switch",

&#x20; "cuentaMatriz": "0010000000001",

&#x20; "subtotalComision": 1,

&#x20; "montoIva": 0.15,

&#x20; "totalDebitado": 1.15,

&#x20; "permiteSobregiro": true,

&#x20; "codigoCuentaIngresos": "INGRESOS\_SERVICIOS\_MASIVOS",

&#x20; "codigoCuentaIva": "PASIVOS\_IVA\_RETENIDO",

&#x20; "referenciaExterna": "uuid-lote"

}



Respuesta actual suficiente:

{

&#x20; "estado": "APLICADA",

&#x20; "uuidDebitoMatriz": "uuid",

&#x20; "uuidCreditoIngresos": "uuid",

&#x20; "uuidCreditoIva": "uuid",

&#x20; "uuidGrupoOperacion": "uuid"

}



Prioridad: critica si las pruebas con cuenta sin saldo fallan

El Core debe confirmar que permiteSobregiro=true se respeta aunque el saldo disponible remanente no alcance para comision e IVA. Si el Core ya permite sobregiro por configuracion de cuenta, aun se debe documentar el criterio.





###### 1.6 Dia Habil

Endpoint actual:

GET /api/v1/core/integracion-switch/feriados/siguiente-dia-habil?fecha=2026-05-15



Nuevo endpoint recomendado:

GET /api/v1/core/integracion-switch/calendario/dia-habil?fecha=2026-05-15



Respuesta recomendada:

{

&#x20; "fecha": "2026-05-15",

&#x20; "esDiaHabil": true,

&#x20; "esFinSemana": false,

&#x20; "esFeriado": false,

&#x20; "siguienteDiaHabil": "2026-05-18",

&#x20; "codigo": "DIA\_HABIL",

&#x20; "mensaje": "La fecha indicada es dia habil."

}



Prioridad: media

Temporalmente el Switch puede seguir usando su logica sabado/domingo y mejorar luego con feriados desde Core. Para cumplir feriados reales, depende del Core.

 



