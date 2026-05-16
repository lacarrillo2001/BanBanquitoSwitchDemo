const storageKeys = {
  baseUrl: "banquito.demo.baseUrl",
  uuidLote: "banquito.demo.uuidLote",
  history: "banquito.demo.history"
};

const defaultBaseUrl = window.location.origin || "http://localhost:3000";

const state = {
  history: loadHistory(),
  lastResponse: null
};

const elements = {
  baseUrl: document.getElementById("baseUrl"),
  pingButton: document.getElementById("pingButton"),
  connectionHint: document.getElementById("connectionHint"),
  loadHorariosButton: document.getElementById("loadHorariosButton"),
  loadTarifasButton: document.getElementById("loadTarifasButton"),
  horariosSummary: document.getElementById("horariosSummary"),
  tarifasSummary: document.getElementById("tarifasSummary"),
  uploadForm: document.getElementById("uploadForm"),
  clearFileButton: document.getElementById("clearFileButton"),
  uuidLote: document.getElementById("uuidLote"),
  validarButton: document.getElementById("validarButton"),
  estadoButton: document.getElementById("estadoButton"),
  lineasButton: document.getElementById("lineasButton"),
  procesarButton: document.getElementById("procesarButton"),
  liquidarButton: document.getElementById("liquidarButton"),
  novedadesButton: document.getElementById("novedadesButton"),
  comprobanteButton: document.getElementById("comprobanteButton"),
  clearHistoryButton: document.getElementById("clearHistoryButton"),
  formatoReporte: document.getElementById("formatoReporte"),
  responseViewer: document.getElementById("responseViewer"),
  debugMeta: document.getElementById("debugMeta"),
  historyList: document.getElementById("historyList"),
  currentUuid: document.getElementById("currentUuid"),
  currentEstado: document.getElementById("currentEstado"),
  currentAction: document.getElementById("currentAction"),
  estadoSummary: document.getElementById("estadoSummary"),
  lineasSummary: document.getElementById("lineasSummary"),
  reportSummary: document.getElementById("reportSummary"),
  lineasTableBody: document.getElementById("lineasTableBody"),
  observacionProcesamiento: document.getElementById("observacionProcesamiento")
};

bootstrap();

function bootstrap() {
  elements.baseUrl.value = localStorage.getItem(storageKeys.baseUrl) || defaultBaseUrl;
  elements.uuidLote.value = localStorage.getItem(storageKeys.uuidLote) || "";
  syncActiveState();
  renderHistory();
  bindEvents();
}

function bindEvents() {
  elements.baseUrl.addEventListener("change", persistBaseUrl);
  elements.pingButton.addEventListener("click", handlePing);
  elements.loadHorariosButton.addEventListener("click", handleHorarios);
  elements.loadTarifasButton.addEventListener("click", handleTarifas);
  elements.uploadForm.addEventListener("submit", handleUpload);
  elements.clearFileButton.addEventListener("click", () => {
    document.getElementById("archivo").value = "";
  });
  elements.uuidLote.addEventListener("change", persistUuidLote);
  elements.validarButton.addEventListener("click", () => handleSimpleAction("Validar lote", "/api/v1/pagos-masivos/lotes/{uuidLote}/validar", { method: "POST" }));
  elements.estadoButton.addEventListener("click", () => handleSimpleAction("Consultar estado", "/api/v1/pagos-masivos/lotes/{uuidLote}/estado"));
  elements.lineasButton.addEventListener("click", () => handleSimpleAction("Consultar lineas", "/api/v1/pagos-masivos/lotes/{uuidLote}/lineas?page=0&size=50"));
  elements.procesarButton.addEventListener("click", handleProcesar);
  elements.liquidarButton.addEventListener("click", () => handleSimpleAction("Liquidar lote", "/api/v1/pagos-masivos/lotes/{uuidLote}/liquidar", { method: "POST" }));
  elements.novedadesButton.addEventListener("click", () => handleSimpleAction("Reporte novedades", `/api/v1/pagos-masivos/lotes/{uuidLote}/novedades?formato=${encodeURIComponent(elements.formatoReporte.value)}`));
  elements.comprobanteButton.addEventListener("click", () => handleSimpleAction("Comprobante", `/api/v1/pagos-masivos/lotes/{uuidLote}/comprobante?formato=${encodeURIComponent(elements.formatoReporte.value)}`));
  elements.clearHistoryButton.addEventListener("click", clearHistory);

  document.querySelectorAll(".example-chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      const fileName = chip.dataset.example || chip.textContent.trim();
      pushHistory({ action: "Referencia de archivo", status: "INFO", detail: `Usa postman/examples/${fileName}` });
      updateResponseViewer({
        action: "Referencia de archivo",
        status: "INFO",
        body: { ruta: `postman/examples/${fileName}`, nota: "Selecciona este archivo manualmente desde el formulario." }
      });
    });
  });
}

function persistBaseUrl() {
  localStorage.setItem(storageKeys.baseUrl, normalizeBaseUrl(elements.baseUrl.value));
  elements.baseUrl.value = normalizeBaseUrl(elements.baseUrl.value);
}

function persistUuidLote() {
  localStorage.setItem(storageKeys.uuidLote, elements.uuidLote.value.trim());
  syncActiveState();
}

function loadHistory() {
  try {
    const value = localStorage.getItem(storageKeys.history);
    return value ? JSON.parse(value) : [];
  } catch (error) {
    return [];
  }
}

function saveHistory() {
  localStorage.setItem(storageKeys.history, JSON.stringify(state.history.slice(0, 8)));
}

function clearHistory() {
  state.history = [];
  saveHistory();
  renderHistory();
}

async function handlePing() {
  const result = await callApi({
    action: "Probar conexion",
    path: "/api/v1/pagos-masivos/horarios-corte"
  });

  if (result.ok) {
    elements.connectionHint.textContent = "Conexion HTTP alcanzada. Si mas adelante falla otra llamada, revisa datos o estado del lote.";
    renderHorarios(result.data);
  }
}

async function handleHorarios() {
  const result = await callApi({
    action: "Consultar horarios",
    path: "/api/v1/pagos-masivos/horarios-corte"
  });

  if (result.ok) {
    renderHorarios(result.data);
  }
}

async function handleTarifas() {
  const tipoServicio = document.getElementById("tipoServicio").value.trim();
  const query = tipoServicio ? `?tipoServicio=${encodeURIComponent(tipoServicio)}` : "";
  const result = await callApi({
    action: "Consultar tarifas",
    path: `/api/v1/pagos-masivos/tarifas${query}`
  });

  if (result.ok) {
    renderTarifas(result.data);
  }
}

async function handleUpload(event) {
  event.preventDefault();

  const fileInput = document.getElementById("archivo");
  if (!fileInput.files || !fileInput.files[0]) {
    updateResponseViewer({ action: "Cargar lote", status: "ERROR", body: { mensaje: "Selecciona un archivo antes de cargar." } });
    return;
  }

  const formData = new FormData();
  formData.append("archivo", fileInput.files[0]);
  formData.append("tipoServicio", document.getElementById("tipoServicio").value.trim());
  formData.append("cuentaMatrizCargo", document.getElementById("cuentaMatrizCargo").value.trim());
  formData.append("canalIngreso", document.getElementById("canalIngreso").value);
  formData.append("rucEmpresa", document.getElementById("rucEmpresa").value.trim());

  const idCredencial = document.getElementById("idCredencialWebCore").value.trim();
  if (idCredencial) {
    formData.append("idCredencialWebCore", idCredencial);
  }

  const result = await callApi({
    action: "Cargar lote",
    path: "/api/v1/pagos-masivos/lotes",
    options: {
      method: "POST",
      body: formData
    }
  });

  if (result.ok && result.data && result.data.uuidLote) {
    setUuidLote(result.data.uuidLote);
    setEstado(result.data.estado || "RECIBIDO");
    renderEstadoSummary(result.data);
  }
}

async function handleProcesar() {
  let path;
  try {
    path = withUuid("/api/v1/pagos-masivos/lotes/{uuidLote}/procesar");
  } catch (error) {
    return;
  }

  const result = await callApi({
    action: "Procesar lote",
    path,
    options: {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        ejecutadoPor: "DEMO_UI",
        observacion: elements.observacionProcesamiento.value.trim() || "Procesamiento desde demo-ui"
      })
    }
  });

  if (result.ok && result.data) {
    setEstado(result.data.estado || "PROCESANDO");
    renderEstadoSummary(result.data);
  }
}

async function handleSimpleAction(action, pathTemplate, options = {}) {
  let path;
  try {
    path = withUuid(pathTemplate);
  } catch (error) {
    return;
  }

  const result = await callApi({
    action,
    path,
    options
  });

  if (!result.ok || !result.data) {
    return;
  }

  if (result.data.uuidLote) {
    setUuidLote(result.data.uuidLote);
  }

  if (result.data.estado) {
    setEstado(result.data.estado);
  } else if (result.data.estadoLiquidacion) {
    setEstado(result.data.estadoLiquidacion);
  }

  if (action === "Consultar estado" || action === "Validar lote" || action === "Procesar lote" || action === "Liquidar lote") {
    renderEstadoSummary(result.data);
  }

  if (action === "Consultar lineas") {
    renderLineas(result.data);
  }

  if (action === "Reporte novedades" || action === "Comprobante") {
    renderReporte(result.data, action);
  }
}

async function callApi({ action, path, options = {} }) {
  const startedAt = performance.now();
  const baseUrl = normalizeBaseUrl(elements.baseUrl.value);
  persistBaseUrl();

  setAction(action);

  let response;
  let text;
  let data;

  try {
    response = await fetch(`${baseUrl}${path}`, options);
    text = await response.text();
    data = parseBody(text);
  } catch (error) {
    const durationMs = Math.round(performance.now() - startedAt);
    const payload = {
      action,
      status: "NETWORK_ERROR",
      durationMs,
      body: {
        mensaje: error.message,
        sugerencia: "Si el backend responde en Postman, revisa CORS o que el servidor este levantado en la URL configurada."
      }
    };
    pushHistory({ action, status: "NETWORK_ERROR", detail: error.message });
    updateResponseViewer(payload);
    return { ok: false, error };
  }

  const durationMs = Math.round(performance.now() - startedAt);
  const payload = {
    action,
    status: `${response.status} ${response.statusText}`,
    durationMs,
    body: data,
    path
  };

  pushHistory({ action, status: response.status, detail: resolveHistoryDetail(data) });
  updateResponseViewer(payload);

  return {
    ok: response.ok,
    response,
    data,
    text
  };
}

function renderHorarios(data) {
  if (!data) {
    return;
  }

  elements.horariosSummary.innerHTML = summaryMarkup([
    ["Hora corte", data.horaCorteProceso],
    ["Inicio encolados", data.horaInicioLotesEncolados],
    ["Ventana duplicidad", data.ventanaDuplicidadDias],
    ["Zona horaria", data.zonaHoraria],
    ["Mensaje", data.mensaje]
  ]);
}

function renderTarifas(data) {
  const tarifas = Array.isArray(data) ? data : data ? [data] : [];
  if (!tarifas.length) {
    elements.tarifasSummary.innerHTML = "<div class=\"empty-state\">No se recibieron tarifas.</div>";
    return;
  }

  const first = tarifas[0];
  const rango = Array.isArray(first.rangos) && first.rangos.length
    ? `${first.rangos[0].rangoDesde} - ${first.rangos[0].rangoHasta} => ${first.rangos[0].tarifaUnitaria}`
    : "Sin rangos";

  elements.tarifasSummary.innerHTML = summaryMarkup([
    ["Tipo servicio", first.tipoServicio],
    ["Moneda", first.moneda],
    ["Vigente desde", first.vigenteDesde],
    ["Primer rango", rango],
    ["Total tarifas", tarifas.length]
  ]);
}

function renderEstadoSummary(data) {
  elements.estadoSummary.innerHTML = summaryMarkup([
    ["UUID lote", data.uuidLote || elements.uuidLote.value || "-"],
    ["Estado", badgeForStatus(data.estado || data.estadoLiquidacion || "Sin estado")],
    ["Valido", data.valido],
    ["Siguiente accion", data.siguienteAccion],
    ["Motivo rechazo", data.motivoRechazoGlobal],
    ["Totales", compactObject(data.totales || data.resultado || data.resumenLineas)]
  ]);
}

function renderLineas(data) {
  const content = Array.isArray(data?.contenido) ? data.contenido : [];
  elements.lineasSummary.innerHTML = summaryMarkup([
    ["Total pagina", content.length],
    ["Pagina", data?.pagina],
    ["Tamano", data?.tamano],
    ["Total elementos", data?.totalElementos],
    ["Total paginas", data?.totalPaginas]
  ]);

  if (!content.length) {
    elements.lineasTableBody.innerHTML = "<tr><td colspan=\"6\" class=\"empty-table\">No hay lineas para mostrar.</td></tr>";
    return;
  }

  elements.lineasTableBody.innerHTML = content.map((linea) => `
    <tr>
      <td>${safe(linea.secuencial)}</td>
      <td>${safe(linea.nombreBeneficiario)}</td>
      <td>${safe(linea.cuentaDestino)}</td>
      <td>${safe(linea.monto)}</td>
      <td>${badgeForStatus(linea.estado || "-")}</td>
      <td>${safe(linea.codigoError || linea.mensajeError || "-")}</td>
    </tr>
  `).join("");
}

function renderReporte(data, action) {
  if (!data) {
    return;
  }

  if (action === "Reporte novedades") {
    elements.reportSummary.innerHTML = summaryMarkup([
      ["Tipo", data.tipoReporte],
      ["Formato", data.formato],
      ["Generado", data.fechaGeneracion],
      ["Resumen", compactObject(data.resumen)],
      ["Lineas incluidas", Array.isArray(data.lineas) ? data.lineas.length : 0]
    ]);
    return;
  }

  elements.reportSummary.innerHTML = summaryMarkup([
    ["Tipo", data.tipoReporte],
    ["Formato", data.formato],
    ["Empresa", compactObject(data.empresa)],
    ["Resumen pagos", compactObject(data.resumenPagos)],
    ["Liquidacion", compactObject(data.liquidacionServicio)],
    ["Generado", data.fechaGeneracion]
  ]);
}

function updateResponseViewer(payload) {
  state.lastResponse = payload;
  elements.debugMeta.innerHTML = summaryMarkup([
    ["Accion", payload.action],
    ["Estado", payload.status],
    ["Duracion", payload.durationMs ? `${payload.durationMs} ms` : "-"],
    ["Ruta", payload.path || "-"]
  ]);
  elements.responseViewer.textContent = JSON.stringify(payload.body, null, 2);
  syncActiveState();
}

function renderHistory() {
  if (!state.history.length) {
    elements.historyList.innerHTML = "<div class=\"empty-state\">No hay acciones registradas.</div>";
    return;
  }

  elements.historyList.innerHTML = state.history.map((entry) => `
    <div class="history-entry">
      <strong>${safe(entry.action)}</strong>
      <div class="history-meta">${safe(entry.status)} · ${safe(entry.timestamp)}</div>
      <div class="history-entry-code">${safe(entry.detail || "Sin detalle")}</div>
    </div>
  `).join("");
}

function pushHistory({ action, status, detail }) {
  state.history.unshift({
    action,
    status,
    detail,
    timestamp: new Date().toLocaleString("es-EC")
  });
  state.history = state.history.slice(0, 8);
  saveHistory();
  renderHistory();
}

function syncActiveState() {
  elements.currentUuid.textContent = elements.uuidLote.value.trim() || "Sin lote activo";
  elements.currentEstado.innerHTML = state.lastResponse?.body?.estado
    ? badgeForStatus(state.lastResponse.body.estado)
    : state.lastResponse?.body?.estadoLiquidacion
      ? badgeForStatus(state.lastResponse.body.estadoLiquidacion)
      : "Sin ejecucion";
  elements.currentAction.textContent = state.lastResponse?.action || "Ninguna";
}

function setUuidLote(uuid) {
  elements.uuidLote.value = uuid;
  persistUuidLote();
}

function setEstado(estado) {
  state.lastResponse = state.lastResponse || {};
  state.lastResponse.body = state.lastResponse.body || {};
  state.lastResponse.body.estado = estado;
  syncActiveState();
}

function setAction(action) {
  elements.currentAction.textContent = action;
}

function withUuid(pathTemplate) {
  const uuid = elements.uuidLote.value.trim();
  if (!uuid) {
    throwMissingUuid(pathTemplate);
  }
  return pathTemplate.replace("{uuidLote}", encodeURIComponent(uuid));
}

function throwMissingUuid(pathTemplate) {
  updateResponseViewer({
    action: "UUID requerido",
    status: "ERROR",
    path: pathTemplate,
    body: { mensaje: "Debes cargar un lote o pegar un uuidLote antes de ejecutar este paso." }
  });
  throw new Error("uuidLote requerido");
}

function normalizeBaseUrl(value) {
  return (value || defaultBaseUrl).trim().replace(/\/$/, "");
}

function parseBody(text) {
  if (!text) {
    return { mensaje: "Respuesta vacia" };
  }
  try {
    return JSON.parse(text);
  } catch (error) {
    return { raw: text };
  }
}

function resolveHistoryDetail(data) {
  if (!data) {
    return "Sin body";
  }
  return data.mensaje || data.message || data.estado || data.estadoLiquidacion || data.uuidLote || compactObject(data);
}

function compactObject(value) {
  if (!value) {
    return "-";
  }
  if (typeof value === "string") {
    return value;
  }
  return JSON.stringify(value);
}

function summaryMarkup(entries) {
  const validEntries = entries.filter(([, value]) => value !== undefined && value !== null && value !== "");
  if (!validEntries.length) {
    return '<div class="empty-state">Sin datos.</div>';
  }
  return validEntries.map(([key, value]) => `
    <div class="summary-item">
      <span class="summary-key">${safe(key)}</span>
      <span class="summary-value">${value}</span>
    </div>
  `).join("");
}

function badgeForStatus(value) {
  const text = safe(value);
  const upper = String(value || "").toUpperCase();
  let tone = "neutral";

  if (upper.includes("EXITOSA") || upper.includes("VALIDADO") || upper.includes("CERRADO") || upper.includes("COMPLETADO")) {
    tone = "success";
  } else if (upper.includes("RECHAZ") || upper.includes("FALL") || upper.includes("ERROR") || upper.includes("ANUL")) {
    tone = "danger";
  } else if (upper.includes("PROCES") || upper.includes("ENCOL") || upper.includes("RECIB") || upper.includes("PENDIENTE")) {
    tone = "warning";
  }

  return `<span class="tag ${tone}">${text}</span>`;
}

function safe(value) {
  return String(value ?? "-")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
