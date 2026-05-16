# AGENTS.md — Switch de Pagos Masivos Banco BanQuito

## 1. Contexto del proyecto

Este proyecto implementa el backend del sistema **Switch de Pagos Masivos Banco BanQuito** usando Spring Boot.

El sistema se construye como un **monolito modular**.  
No debe implementarse como microservicios.

No deben agregarse colas externas como:

- Kafka
- RabbitMQ
- Redis Streams
- otras tecnologías de mensajería distribuida

salvo que se solicite explícitamente en una fase posterior.

El Switch permite:

- recibir lotes de pagos masivos enviados por empresas cliente;
- validar archivos;
- procesar pagos línea por línea;
- calcular comisiones e IVA;
- liquidar contablemente el servicio;
- generar reportes;
- registrar notificaciones.

El Core Bancario es un sistema externo/lógico y es la fuente de verdad de:

- cuentas;
- saldos;
- movimientos.

El Switch solo guarda referencias lógicas hacia el Core, por ejemplo:

- números de cuenta;
- UUIDs de transacciones;
- identificadores externos.

**No crear entidades JPA del Core dentro de este proyecto.**

---

## 2. Arquitectura general

El proyecto debe organizarse como monolito modular bajo el paquete base:

```text
com.banquito.switchpagos
```

### Módulos principales

- `lote`
- `archivo`
- `procesamiento`
- `tarifaje`
- `reporte`
- `integracioncore`

### Módulos de soporte

- `catalogo`
- `parametro`
- `auditoria`
- `common`
- `config`

Cada módulo representa una responsabilidad funcional o transversal clara.

**No mezclar responsabilidades entre módulos.**

---

## 3. Estructura interna de módulos

La estructura estándar de cada módulo es:

```text
modulo/
├── dto/
│   ├── api/
│   └── internal/
├── model/
├── repository/
├── enums/
└── service/
```

### Reglas

#### `dto/api`

DTOs expuestos por endpoints hacia frontend u otros consumidores externos.

#### `dto/internal`

DTOs usados para comunicación interna entre módulos o resultados internos de servicios.

#### `model`

Entidades JPA.

#### `repository`

Repositorios JPA del módulo.

#### `enums`

Enums del módulo.

#### `service`

Interfaces de servicios del módulo.

### Restricciones

- No usar un paquete llamado `entity`.
- Las entidades JPA siempre deben ir en `model`.
- No usar mappers salvo que se solicite explícitamente en una fase posterior.

---

## 4. Reglas de modularidad

- Cada entidad pertenece al módulo que administra su ciclo de vida.
- Los repositories solo deben usarse dentro del módulo dueño de la entidad.
- Otros módulos no deben modificar entidades ajenas directamente.
- La comunicación entre módulos debe hacerse mediante services.
- Los servicios principales deben exponerse como interfaces.
- Los módulos pueden intercambiar DTOs internos, pero no entidades completas si no es necesario.
- Los DTOs de API no necesariamente son los mismos DTOs internos.
- Los controllers solo deben llamar services.
- Los controllers no deben llamar repositories.
- Las reglas de negocio deben estar en services, no en controllers ni repositories.
- Las entidades JPA deben representar persistencia. No deben controlar el flujo completo del negocio.

---

## 5. Reglas para entidades JPA

- No usar tipos primitivos como `int`, `long`, `double`, `boolean`, etc.
- Usar wrappers como `Integer`, `Long`, `Boolean`.
- Para dinero o valores monetarios usar siempre `BigDecimal`.
- Nunca usar `float` ni `double`.
- Si una tabla tiene clave primaria compuesta, crear una clase aparte para la PK.
- Las restricciones `UNIQUE` compuestas no son claves primarias compuestas.

### Cada entidad debe tener

- constructor vacío;
- constructor solo con la PK;
- getters y setters manuales;
- `equals()` y `hashCode()` basados únicamente en la PK;
- `toString()` sobrescrito.

### Restricciones

- No crear constructores con todos los atributos.
- No incluir relaciones completas en `toString()` para evitar recursividad.
- Las relaciones JPA deben ser preferentemente unidireccionales de hijo a padre mediante `@ManyToOne`.
- Evitar relaciones bidireccionales innecesarias.
- No crear colecciones `@OneToMany` salvo que se solicite explícitamente.
- No mapear relaciones hacia el Core Bancario porque son referencias lógicas, no FKs físicas del schema del Switch.
- Usar `@Transient` solo cuando el atributo no exista en la base de datos.

---

## 6. Reglas de tipos Java

Usar los siguientes tipos:

| Base de datos | Java |
|---|---|
| BIGINT | Long |
| INTEGER | Integer |
| VARCHAR / CHAR | String |
| NUMERIC(p,s) | BigDecimal |
| UUID | java.util.UUID |
| TIMESTAMPTZ | java.time.OffsetDateTime |
| DATE | java.time.LocalDate |
| BOOLEAN | Boolean |
| JSONB | com.fasterxml.jackson.databind.JsonNode |
| INET | String |

### Nota sobre JSONB

Preferentemente usar:

```java
com.fasterxml.jackson.databind.JsonNode
```

Si no es viable, usar `String` y dejarlo documentado.

---

## 7. Reglas de DTOs

- No devolver entidades JPA desde controllers.
- Los DTOs de API deben exponer solo la información necesaria para frontend o consumidores externos.
- Los DTOs internos pueden transportar información entre servicios o módulos, pero deben evitar exponer entidades completas si no es necesario.
- No mezclar DTOs de API con DTOs internos cuando tengan propósitos diferentes.

### Ejemplos de nombres correctos

- `CargaLoteRequest`
- `CargaLoteResponse`
- `EstadoLoteResponse`
- `ResumenLoteInternalDto`
- `ResultadoProcesamientoInternalDto`

### Restricciones

- Mantener consistencia en nombres.
- No usar nombres ambiguos como:
    - `DataDto`
    - `InfoDto`
    - `ResponseDto`

---

## 8. Reglas de repositories

- Los repositories pertenecen al módulo dueño de la entidad.
- No acceder a repositories desde controllers.
- No acceder a repositories de otro módulo directamente.
- Si otro módulo necesita información, debe solicitarla mediante el service del módulo dueño.
- Los repositories deben contener consultas de persistencia, no reglas de negocio.
- Usar nombres descriptivos para métodos de consulta.

---

## 9. Reglas de services

- Los servicios principales deben definirse como interfaces en el paquete `service`.
- Las implementaciones deben ubicarse de forma consistente según la estructura definida para el proyecto.
- Si se crea un paquete de implementación, mantenerlo dentro del módulo correspondiente.
- Los services contienen la lógica de negocio y la coordinación entre repositories, otros services y clientes externos.
- Otros módulos deben depender de interfaces de services, no de implementaciones concretas.
- Programar contra interfaces.
- No poner lógica de negocio en controllers.
- No poner lógica de negocio compleja en entidades.

---

## 10. Reglas de controllers

### Los controllers solo deben

- recibir requests;
- validar aspectos básicos de entrada;
- llamar services;
- devolver responses.

### Los controllers no deben

- acceder a repositories;
- modificar entidades directamente;
- implementar reglas de negocio;
- coordinar procesos complejos que pertenezcan a services.

### Regla general

Los endpoints deben exponer casos de uso externos, no detalles internos de implementación.

---

## 11. Reglas de Clean Code

- Usar nombres descriptivos.
- Variables y atributos deben tener nombres sustantivos.
- Métodos deben tener nombres verbales.
- Usar CamelCase en Java.
- No usar nombres como:
    - `x`
    - `a`
    - `b`
    - `y`
    - `z`
    - `data`
    - `info`
    - `obj`

  si no aportan significado.

- Mantener métodos cortos y con una responsabilidad clara.
- Evitar clases demasiado grandes.
- Evitar duplicación de lógica.
- Evitar comentarios innecesarios.
- Usar comentarios solo para decisiones técnicas complejas o reglas de negocio que no sean evidentes.
- Preferir código legible antes que código “ingenioso”.

---

## 12. Reglas de errores y excepciones

- Usar excepciones específicas cuando una regla de negocio falle.
- No lanzar `RuntimeException` genérica salvo casos temporales.
- Centralizar el manejo de errores con un mecanismo global cuando se implemente la capa web.
- Los mensajes de error deben ser claros y útiles para diagnóstico.

---

## 13. Restricciones técnicas

- No usar mappers salvo instrucción explícita.
- No crear microservicios.
- No agregar colas externas.
- No crear entidades del Core Bancario.
- No modificar nombres de tablas o columnas existentes sin instrucción explícita.
- No exponer campos internos sensibles o innecesarios en DTOs de API.
- No hacer cambios masivos fuera del alcance solicitado en el prompt actual.

---

## 14. Forma de trabajo esperada

### Antes de modificar código

- revisar la estructura existente;
- respetar la arquitectura modular;
- trabajar solo sobre el alcance solicitado en cada prompt;
- si una decisión no está clara, elegir la alternativa más simple que respete el monolito modular.

### Al finalizar una tarea indicar brevemente

- qué archivos se crearon o modificaron;
- qué decisiones relevantes se tomaron;
- qué queda pendiente para fases posteriores.