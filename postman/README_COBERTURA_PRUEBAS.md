# Cobertura de pruebas Postman - Switch Pagos Masivos

Ejecutar la coleccion sobre una base limpia o con archivos que no hayan sido cargados antes, porque RF-02 valida duplicidad por nombre de archivo, RUC y hash.

## Cobertura por requisitos

| Requisito | Cobertura en la coleccion |
| --- | --- |
| RF-01 Ingesta y horarios | `Configuracion`, carga CSV, carga TXT, canal `PORTAL_WEB` |
| RF-02 Validacion estructural y duplicidad | `Validaciones y errores de archivo`, `Validaciones estructurales adicionales RF-02` |
| RF-03 Procesamiento linea por linea | `Flujo principal exitoso`, `Procesamiento con errores por linea - cobertura RF-03/RF-04` |
| RF-04 Resiliencia del lote | Lotes con cuenta inexistente, cuenta bloqueada, saldo insuficiente, limite, minimo y lote mixto |
| RF-05 Notificacion | `GET Novedades JSON` genera notificaciones para lineas exitosas |
| RF-06 Tarifaje | `GET Tarifas`, liquidacion de lotes NOM, PRV, TXT y parciales |
| RF-07 Liquidacion contable | `POST Liquidar` exitoso y rechazos por estado/no exitosas |
| RF-08 Reportes de cierre | `GET Novedades`, `GET Comprobante`, formatos invalidos y lote no cerrado |

## Archivos agregados

- `lote_error_monto_supera_limite.csv`: monto individual mayor al limite NOM, pero menor al saldo simulado.
- `lote_error_monto_menor_minimo.csv`: monto individual debajo del minimo configurado.
- `lote_mixto_multiples_errores.csv`: una linea exitosa y cuatro rechazos distintos.
- `lote_todas_lineas_rechazadas.csv`: procesamiento sin transacciones exitosas, liquidacion debe rechazar.
- `lote_prv_valido.csv`: flujo exitoso para tipo de servicio PRV.
- `lote_valido_txt.txt`: formato TXT soportado.
- `lote_secuencial_invalido.csv`, `lote_sin_pie.csv`, `lote_sin_cabecera.csv`, `lote_registro_desconocido.csv`, `lote_fecha_invalida.csv`, `lote_monto_invalido.csv`: validaciones estructurales tempranas.
- `lote_formato_no_soportado.pdf`: rechazo por extension no soportada.

## Recomendacion de ejecucion

1. Iniciar la API.
2. Importar `Switch_Pagos_Masivos_BanQuito.postman_collection.json`.
3. Revisar `baseUrl`, `rucEmpresa`, `tipoServicio` y `cuentaMatrizCargo`.
4. Ejecutar carpetas de arriba hacia abajo.
5. Si se repite una corrida completa, limpiar la BD o duplicar los archivos con nombres nuevos para evitar el rechazo esperado por duplicidad.
