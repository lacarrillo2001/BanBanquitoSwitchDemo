package com.banquito.switchpagos.batch.controller;

import com.banquito.switchpagos.batch.dto.api.AnulacionLoteRequest;
import com.banquito.switchpagos.batch.dto.api.AnulacionLoteResponse;
import com.banquito.switchpagos.batch.dto.api.CargaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.ConsultaLoteResponse;
import com.banquito.switchpagos.batch.dto.api.EstadoLoteResponse;
import com.banquito.switchpagos.batch.dto.api.LineaPagoResponse;
import com.banquito.switchpagos.batch.dto.api.PaginaResponse;
import com.banquito.switchpagos.batch.dto.api.ValidacionLoteResponse;
import com.banquito.switchpagos.batch.dto.internal.RegistroLoteInternalDto;
import com.banquito.switchpagos.batch.enums.CanalIngreso;
import com.banquito.switchpagos.batch.enums.EstadoLote;
import com.banquito.switchpagos.batch.service.LotePagoService;
import com.banquito.switchpagos.processing.enums.EstadoLineaPago;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos-masivos/lotes")
public class LotePagoController {

    private final LotePagoService lotePagoService;

    public LotePagoController(LotePagoService lotePagoService) {
        this.lotePagoService = lotePagoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CargaLoteResponse registrarLote(@RequestParam("archivo") MultipartFile archivo,
                                           @RequestParam("tipoServicio") String tipoServicio,
                                           @RequestParam("cuentaMatrizCargo") String cuentaMatrizCargo,
                                           @RequestParam("canalIngreso") CanalIngreso canalIngreso,
                                           @RequestParam(value = "idCredencialWebCore", required = false)
                                           Integer idCredencialWebCore,
                                           @RequestParam(value = "rucEmpresa", required = false) String rucEmpresa) {
        RegistroLoteInternalDto registroLoteInternalDto = new RegistroLoteInternalDto(
                archivo,
                tipoServicio,
                cuentaMatrizCargo,
                canalIngreso,
                idCredencialWebCore,
                rucEmpresa
        );
        return lotePagoService.registrarLote(registroLoteInternalDto);
    }

    @GetMapping
    public PaginaResponse<ConsultaLoteResponse> consultarLotes(
            @RequestParam(value = "rucEmpresa", required = false) String rucEmpresa,
            @RequestParam(value = "estado", required = false) EstadoLote estado,
            @RequestParam(value = "tipoServicio", required = false) String tipoServicio,
            @RequestParam(value = "fechaDesde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDesde,
            @RequestParam(value = "fechaHasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaHasta,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return lotePagoService.consultarLotes(rucEmpresa, estado, tipoServicio, fechaDesde, fechaHasta, pageable);
    }

    @GetMapping("/{uuidLote}/estado")
    public EstadoLoteResponse consultarEstado(@PathVariable("uuidLote") UUID uuidLote) {
        return lotePagoService.consultarEstado(uuidLote);
    }

    @DeleteMapping("/{uuidLote}")
    public AnulacionLoteResponse anularLote(@PathVariable("uuidLote") UUID uuidLote,
                                            @RequestBody(required = false) AnulacionLoteRequest anulacionLoteRequest) {
        String motivo = anulacionLoteRequest != null ? anulacionLoteRequest.motivo() : "Anulacion solicitada.";
        return lotePagoService.anularLote(uuidLote, motivo);
    }

    @PostMapping("/{uuidLote}/validar")
    public ValidacionLoteResponse validarLote(@PathVariable("uuidLote") UUID uuidLote) {
        return lotePagoService.validarLote(uuidLote);
    }

    @GetMapping("/{uuidLote}/lineas")
    public PaginaResponse<LineaPagoResponse> consultarLineas(
            @PathVariable("uuidLote") UUID uuidLote,
            @RequestParam(value = "estado", required = false) EstadoLineaPago estado,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return lotePagoService.consultarLineas(uuidLote, estado, pageable);
    }
}
