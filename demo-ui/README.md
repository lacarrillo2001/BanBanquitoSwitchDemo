# Demo UI

Frontend minimo para probar el flujo del backend sin tocar `src/main`.

## Archivos

- `index.html`: pagina unica de prueba
- `styles.css`: estilos de la consola
- `app.js`: llamadas HTTP y render de respuestas

## Uso rapido

1. Levanta el backend Spring Boot en `http://localhost:8080`.
2. En otra terminal entra a `demo-ui/`.
3. Instala dependencias con `npm install`.
4. Ejecuta `npm run dev`.
5. Abre `http://localhost:3000`.

La UI queda servida en `localhost:3000` y Vite hace proxy de `/api/*` hacia `http://localhost:8080`, evitando CORS sin tocar el backend.

## Nota importante

- En la pantalla, deja `Base URL del backend` en `http://localhost:3000`.
- No uses `http://localhost:8080` en la UI cuando quieras pasar por el proxy.
