Banco BanQuito - Documento de Requisitos Funcionales

Switch de Pagos Masivos (V1)

Entidad: Banco BanQuito



Versión: 1.3



Fecha: Abril 2026



##### 1\. Introducción y Antecedentes (El Caso de Negocio)

Durante los últimos dos semestres, el Banco BanQuito se enfrenta a una crisis sistémica de retención en su segmento de Banca Empresarial. La falta de automatización y los constantes retrasos en la liquidación de fondos han provocado la fuga de 15 de nuestros clientes corporativos más grandes hacia la competencia.



Esta migración no solo representa una salida de liquidez (fuga de capitales) que supera los $45 millones de dólares mensuales, sino que ha originado un agujero financiero proyectado en $500,000 dólares anuales por pérdida directa de ingresos por comisiones transaccionales. Actualmente, los gerentes financieros (CFOs) de las empresas experimentan extrema frustración: los pagos a sus empleados no se acreditan a tiempo y los proveedores detienen despachos por falta de liquidez, mientras el banco responde con procesos manuales lentos y opacos.



Para detener esta sangría financiera y recuperar el liderazgo en el mercado, Banco BanQuito tiene la urgencia crítica de implementar un Switch de Pagos Masivos altamente confiable, que devuelva la autonomía a las empresas y garantice la liquidación exacta y oportuna de sus obligaciones, reactivando así nuestra principal fuente de ingresos por servicios no financieros.



##### 2\. Contexto de Negocio: Impacto, Operación y Contabilidad

Esta sección tiene un fin informativo para alinear el conocimiento del negocio bancario en el equipo de desarrollo.



###### 2.1. El Valor del Cliente Corporativo vs. Cliente Individual (Retail)

En la banca, la pérdida de un cliente corporativo tiene un efecto destructivo multiplicador en comparación con la pérdida de un cliente individual. Un cliente individual (Retail) maneja saldos bajos y realiza transferencias esporádicas. Por el contrario, un cliente corporativo actúa como un "ancla" del ecosistema: concentra millones de dólares en cuentas a la vista y obliga a sus empleados a abrir cuentas en el banco para recibir su sueldo (captación masiva a costo cero). Perder a una empresa significa perder la liquidez central, perder las comisiones por transacciones masivas y, eventualmente, perder a los miles de empleados que cerrarán sus cuentas al ya no recibir su nómina allí.



###### 2.2. ¿Qué son los Pagos Masivos?

Una empresa necesita ejecutar cientos o miles de pagos simultáneos en fechas específicas (ej. Nómina quincenal, pago a proveedores los viernes). En lugar de hacer transferencias manuales una por una, el banco recibe un lote de instrucciones, debita el dinero de la cuenta matriz de la empresa y va "dispersando" esos fondos en las cuentas de los beneficiarios finales de forma automática.



###### 2.3. Ingresos por Comisiones y Separación de Impuestos

El banco invierte recursos en infraestructura de alta disponibilidad y asume riesgos transaccionales. Por ello, cobra un "peaje" (comisión o tarifa) por la prestación de este servicio. Dado que es un servicio facturado, las leyes tributarias exigen que se grave el Impuesto al Valor Agregado (IVA). Al cobrar, el banco debe separar el dinero rigurosamente:



Cuenta Contable de Ingresos (Tarifas): Aquí ingresa el valor neto de la comisión. Este dinero es ganancia real del banco y entra a su Estado de Resultados (P\&L).



Cuenta Contable de Pasivos (Impuestos por Pagar): Aquí ingresa el valor del IVA. Este dinero no es del banco, le pertenece al Estado (entidad tributaria). El banco solo lo custodia temporalmente y lo registra como un pasivo (deuda).



Mezclar estos fondos constituiría una grave infracción regulatoria.



##### 3\. Esquema Tarifario Comercial

El banco ha definido un modelo de cobro escalonado para incentivar el volumen. La tarifa se calcula únicamente sobre las transacciones que resulten exitosas al finalizar el lote.



============================================================

&#x20;             ESQUEMA TARIFARIO COMERCIAL

============================================================

&#x20;VOLUMEN DE TRANSACCIONES      |    TARIFA UNITARIA

&#x20;       EXITOSAS               | (Por cada transacción)

\------------------------------------------------------------

&#x20;De 1 a 10                     |         $0.50

&#x20;De 11 a 100                    |         $0.40

&#x20;De 101 a 500                   |         $0.30

&#x20;De 501 a 1.000                 |         $0.20

&#x20;De 1.001 a 10.000              |         $0.10

&#x20;10.001 en adelante             |         $0.05

\------------------------------------------------------------

&#x20;Nota: Se debe aplicar el 15% de IVA al total generado.

============================================================





##### 4\. Alcance de la Fase 1

Esta primera fase abarca la construcción del motor principal para el procesamiento intrabancario (cuentas de origen y destino pertenecientes a BanQuito). El procesamiento será de naturaleza sincrónica y unitaria (procesamiento línea por línea), culminando con un proceso automático y consolidado de cobro de servicios e impuestos al cliente.



Flujo Operativo de Alto Nivel (Resumen del Diagrama de Contexto):



1. Empresa Cliente: Sube archivo de pagos vía Portal Web Banca Empresas o lo deposita en un buzón SFTP Seguro.
2. Canales de Entrada: Envían instrucciones al Switch de Pagos Masivos (V1).
3. Switch de Pagos Masivos: Actúa como orquestador central.
Consulta saldos y aplica débitos/créditos en el Core Bancario (Motor de Cuentas).
Liquida comisiones e IVA en el Core Bancario.
Ordena alertas al Servidor SMTP de Notificaciones.
4. Salidas:
El Servidor SMTP envía avisos de pago exitoso a los Beneficiarios (Empleados/Proveedores).
El Switch genera comprobantes y reportes finales para la Empresa Cliente.



##### 5\. Especificación Avanzada del Archivo de Entrada

El sistema deberá ser capaz de interpretar archivos planos estructurados que contengan tres bloques de información obligatorios:



###### 5.1. Registro de Cabecera (1 línea)

Identificador del Cliente: RUC de la empresa emisora.



Tipo de Servicio: Código que define el propósito del lote (Ej: NOM para Nómina, PRV para Proveedores).



Fecha y Hora de Generación: Timestamp emitido por el cliente.



Cuenta Matriz de Cargo: Número de cuenta de la empresa de donde saldrán los fondos.



Total de Registros: Cantidad de transacciones detalladas.



Monto Total de Control: Sumatoria exacta de todos los pagos a realizar.



###### 5.2. Registros de Detalle (N líneas)

Secuencial: Número de línea (1, 2, 3...).



Identificación del Beneficiario: Cédula, RUC o Pasaporte del receptor.



Nombre del Beneficiario: Nombre o Razón Social del receptor.



Cuenta Destino: Número de cuenta BanQuito del receptor.



Monto a Transferir: Valor numérico con dos decimales.



Referencia / Concepto: Texto libre (ej. "Honorarios Marzo").



Correo de Notificación: Email del beneficiario para el envío del aviso de pago.



###### 5.3. Registro de Pie de Control (1 línea)

Hash/Código de Seguridad: Cadena alfanumérica generada por la empresa para evitar la manipulación del archivo en tránsito.



Suma de Verificación: Re-confirmación del monto total y número de registros para cuadre operativo.





##### 6\. Requisitos Funcionales Detallados

###### RF-01: Ingesta y Horarios de Corte

El sistema recibirá las instrucciones vía Portal Web de Banca Empresas (carga manual) o mediante un buzón SFTP seguro (carga automatizada).Regla de Horario: El sistema validará la hora de recepción. Los archivos recibidos antes de las 18:00 se procesarán inmediatamente. Los archivos recibidos después de las 18:00, o en fines de semana/feriados, quedarán en estado "Encolado" y su procesamiento iniciará automáticamente a las 00:01 del siguiente día hábil.En el caso del SFTP, se asumirá el uso de la cuenta marcada como "favorita" para pagos.

###### RF-02: Validación Estructural y Prevención de Fraude Operativo

Antes de interactuar con el Core Bancario, el switch debe rechazar el archivo completo de forma temprana si:La sumatoria de montos en el detalle no coincide con los valores declarados en cabecera y pie.El RUC de la cabecera no corresponde a un cliente con el servicio de pagos masivos activo.Se detecta que el mismo nombre de archivo y Hash ya fue procesado con éxito en los últimos 30 días (Prevención de duplicidad accidental de nómina).

###### RF-03: Procesamiento Financiero (Línea por Línea)

El sistema iterará sobre los registros de detalle ejecutando las siguientes reglas de negocio por cada línea, de manera secuencial:Validación de Límites: Verificar que el monto individual no supere el límite máximo permitido por el banco para ese tipo de transacción.Validación de Origen: Verificar saldo disponible en la Cuenta Matriz para cubrir el monto específico de esa línea.Validación de Destino: Confirmar que la Cuenta Destino exista, pertenezca a la identificación indicada en el archivo y su estado permita depósitos (rechazar si está "Bloqueada" o "Inactiva").Liquidación: Generar el débito en la cuenta origen y el crédito en la cuenta destino.

###### RF-04: Resiliencia Transaccional del Lote

La falla de una instrucción individual (ej. un proveedor con cuenta bloqueada o falta de saldo para una línea específica) bajo ninguna circunstancia debe abortar el archivo completo. El sistema registrará el error, marcará la línea como "Rechazada" indicando la causal, y continuará con la siguiente instrucción hasta finalizar la totalidad del lote.

###### RF-05: Notificación Inmediata al Beneficiario

Inmediatamente después de que una línea se liquide con estado "Exitoso", el sistema deberá generar una orden de notificación al correo electrónico del beneficiario, indicando el monto acreditado, el concepto del pago y el nombre de la empresa emisora.

###### RF-06: Cálculo de Tarifaje y Comisiones

Una vez finalizada la iteración de todas las líneas del archivo (RF-03), el sistema debe calcular el costo del servicio:Contabilizar el número exacto de transacciones que finalizaron con estado "Exitoso".Determinar la "Tarifa Unitaria" aplicando la tabla del Esquema Tarifario Comercial basada en el volumen total exitoso.Calcular el Subtotal de Comisión ($Transacciones Exitosas \\times Tarifa Unitaria$).Calcular el Monto del IVA ($Subtotal de Comisión \\times 0.15$).Calcular el Total a Debitar por Servicios ($Subtotal + IVA$).

###### RF-07: Liquidación Contable de Servicios

Inmediatamente después del cálculo (RF-06), el sistema debe ejecutar de forma automática los siguientes movimientos centralizados en el Core Bancario para cobrar el servicio a la empresa:Débito Global: Un único débito a la Cuenta Matriz de la empresa por el "Total a Debitar por Servicios".(Nota de Negocio: Si la empresa no tiene saldo remanente para cubrir la comisión al final del proceso, el débito contable debe ejecutarse de todas formas, permitiendo que la cuenta matriz ingrese en sobregiro).Crédito a Ingresos: Un crédito por el valor del "Subtotal de Comisión" a la cuenta contable interna del banco denominada INGRESOS\_SERVICIOS\_MASIVOS.Crédito a Impuestos: Un crédito por el valor del "Monto del IVA" a la cuenta contable interna del banco denominada PASIVOS\_IVA\_RETENIDO.

###### RF-08: Cuadre y Reporte de Cierre

Al finalizar los movimientos contables (RF-07), el sistema cambiará el estado del archivo a "Procesado" y generará:Comprobante de Liquidación Corporativa: Un documento/resumen para la empresa que indique el monto total dispersado con éxito a terceros, el detalle del cálculo de la comisión, el IVA retenido y el total final debitado de su cuenta.Reporte de Novedades: Un archivo estructurado de salida a disposición de la empresa, detallando el estado definitivo de cada línea enviada (Exitosa / Rechazada indicando explícitamente el código o motivo del rechazo).

