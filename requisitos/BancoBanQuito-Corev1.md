# Banco BanQuito - Documento de Requisitos Funcionales: Core de Cuentas
**Entidad:** Banco BanQuito
**Versión:** 1.0
**Fecha:** Abril 2026

---

## 1. Introducción
El Core de Cuentas es el sistema maestro del Banco BanQuito encargado de la administración del ciclo de vida de los productos de depósito. Su función principal es mantener la integridad de los saldos, gestionar los estados de las cuentas y procesar de manera atómica las transacciones de débito y crédito solicitadas por los canales del banco y, fundamentalmente, por el nuevo Switch de Pagos Masivos.

## 2. Conceptos Bancarios del Core
Esta sección define los términos de negocio aplicados a este sistema.

* **Sucursal (Branch):** Unidad física u operativa del banco donde se origina la apertura de la cuenta. Es vital para el reporte territorial y la asignación de presupuestos.
* **Titularidad (Ownership):** Define quién es el dueño legal de los fondos. En BanQuito, una cuenta puede pertenecer a una Persona Natural (Cliente Individual) o una Persona Jurídica (Empresa/Corporativo).
* **Estados de Cuenta:**
    * **Activa:** La cuenta permite depósitos y retiros sin restricciones.
    * **Inactiva:** La cuenta ha superado el tiempo de reposo permitido. Permite depósitos pero requiere una acción del cliente para habilitar retiros.
    * **Bloqueada:** Fondos retenidos por orden administrativa o judicial. No permite movimientos de salida.
    * **Suspendida:** Estado temporal por sospecha de fraude o irregularidad documental. Restricción total de movimientos.
* **Transacciones y Subtipos:** Todo movimiento de dinero se clasifica en dos tipos principales, que a su vez se dividen en razones específicas:
    * **Débito (Salida de dinero):** Pago masivo, retiro por cajero, compra en comercio, cobro de comisión, pago de impuestos.
    * **Crédito (Entrada de dinero):** Abono de nómina, depósito por ventanilla, transferencia recibida.

## 3. Alcance del Sistema (Fase 1)
El alcance de esta primera entrega contempla la construcción de un sistema monolítico modular que abarca tanto el motor transaccional base (Core) como el canal digital de cara al cliente corporativo (Banca Web). El objetivo es proveer una plataforma end-to-end que permita la operatividad de las cuentas y sirva como disparador gráfico para el Switch de Pagos Masivos.

Se debe implementar:
* **Módulo Banca Web Empresas:** Portal transaccional que permite la autenticación de clientes corporativos, la visualización de sus productos (cuentas y saldos) y la interfaz gráfica para la carga de archivos hacia el Switch de Pagos Masivos.
* **Gestión de Entidades y Productos Pasivos:** Administración de la información de Clientes (Naturales y Jurídicos), Sucursales y el ciclo de vida de Cuentas de Ahorro, Corriente y Nómina.
* **Motor Transaccional Unitario:** Procesamiento sincrónico de operaciones de afectación de saldo (Débitos y Créditos) mediante un registro inmutable.
* **Exposición de Servicios Internos:** Comunicación directa entre el módulo de Banca Web, el Switch de Pagos y el Core de Cuentas dentro de la misma arquitectura monolítica.

### Diagrama de Contexto (Resumen Informativo)
El Core Bancario actúa como la fuente única de verdad (Single Source of Truth) para clientes, estados y saldos. Es accedido por Operadores de Agencia para gestión administrativa y expone servicios a Canales Digitales, Red de Cajeros (ATMs) y al Switch de Pagos Masivos. Cualquier evento crítico se deriva al Servidor SMTP para notificar al titular.

## 3.1. Consideraciones y Datos de Configuración (Entorno Ficticio)
Para asegurar que el Banco BanQuito sea funcional para las pruebas y la implementación del Switch, el Core se inicializará con:
* **Clientes Totales:** 500 Clientes Individuales y 50 Clientes Corporativos (Empresas).
* **Cuentas Totales:** 1,500 cuentas activas.
* **Distribución de Cuentas:**
    * Cada cliente individual tendrá al menos una cuenta.
    * El 20% de los clientes individuales tendrán dos cuentas (Ahorros y Corriente).
    * Cada empresa (Corporativo) tendrá al menos 3 cuentas para gestionar diferentes flujos de caja (Operativa, Nómina, Impuestos).
* **Sucursales:** El banco operará con 5 sucursales distribuidas geográficamente (Norte, Sur, Centro, Valles, Digital).

## 4. Requisitos Funcionales

### RF-01: Gestión de Entidades (Clientes y Empresas)
El sistema debe permitir la creación y consulta de titulares. Se debe distinguir entre:
* **Persona Natural:** Requiere Cédula, Nombres, Apellidos y Fecha de Nacimiento.
* **Persona Jurídica (Empresa):** Requiere RUC, Razón Social, Fecha de Constitución y Representante Legal.

### RF-02: Administración de Cuentas por Sucursal
Toda cuenta creada en el sistema debe estar obligatoriamente vinculada a una Sucursal (Branch) y a un Titular. El sistema generará un número de cuenta único que incluya el código de la sucursal.

### RF-03: Control de Estados de Cuenta
El sistema debe permitir el cambio de estado de las cuentas (Activa, Inactiva, Bloqueada, Suspendida).
**Regla de Negocio:** El motor de transacciones deberá rechazar cualquier solicitud de débito si la cuenta no está en estado "Activa".

### RF-04: Motor de Transacciones (Débitos y Créditos)
El sistema debe procesar movimientos financieros detallando el Tipo Principal y el Subtipo. Cada transacción debe generar un registro único con fecha, hora, monto, tipo de movimiento, subtipo y saldo resultante (Post-transacción).

### RF-05: Consulta de Disponibilidad (Soporte al Switch)
El Core debe exponer una función de consulta de saldo "En Tiempo Real".
**Regla de Negocio:** Esta función debe retornar el Saldo Contable (total en cuenta) y el Saldo Disponible (total menos bloqueos). El Switch de Pagos Masivos utilizará siempre el Saldo Disponible para validar las operaciones.

### RF-06: Trazabilidad e Idempotencia (Soporte al Switch)
Para evitar duplicidad de procesamiento:
* Registrar un Identificador Único de Transacción (UUID) enviado por el Switch.
* Si se recibe una transacción con un ID que ya existe para esa cuenta en el mismo día, el Core debe rechazarla por "Duplicidad".

### RF-07: Historial de Movimientos
El sistema debe permitir la consulta de los últimos N movimientos de una cuenta, detallando los subtipos (ej. saber si un crédito fue por "Abono de Nómina" o "Depósito Ventanilla").

---
*Propiedad Intelectual - Banco BanQuito S.A. Proyecto: Re-ingeniería Core V1*