package com.banquito.switchpagos.procesamiento.service.impl;

import com.banquito.switchpagos.common.exception.ReglaNegocioException;
import com.banquito.switchpagos.procesamiento.enums.EstadoLimiteTransaccion;
import com.banquito.switchpagos.procesamiento.model.LimiteTransaccion;
import com.banquito.switchpagos.procesamiento.model.LineaPago;
import com.banquito.switchpagos.procesamiento.repository.LimiteTransaccionRepository;
import com.banquito.switchpagos.procesamiento.service.ValidadorLineaPagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class ValidadorLineaPagoServiceImpl implements ValidadorLineaPagoService {

    private final LimiteTransaccionRepository limiteTransaccionRepository;

    public ValidadorLineaPagoServiceImpl(LimiteTransaccionRepository limiteTransaccionRepository) {
        this.limiteTransaccionRepository = limiteTransaccionRepository;
    }

    @Override
    public void validarLimite(String tipoServicio, LineaPago lineaPago) {
        LocalDate fechaProceso = LocalDate.now();
        LimiteTransaccion limiteTransaccion = limiteTransaccionRepository
                .findFirstByTipoServicioCodigoAndEstadoAndVigenteDesdeLessThanEqualAndVigenteHastaIsNullOrderByVigenteDesdeDesc(
                        tipoServicio,
                        EstadoLimiteTransaccion.ACTIVO,
                        fechaProceso
                )
                .or(() -> limiteTransaccionRepository
                        .findFirstByTipoServicioCodigoAndEstadoAndVigenteDesdeLessThanEqualAndVigenteHastaGreaterThanEqualOrderByVigenteDesdeDesc(
                                tipoServicio,
                                EstadoLimiteTransaccion.ACTIVO,
                                fechaProceso,
                                fechaProceso
                        ))
                .orElseThrow(() -> new ReglaNegocioException(
                        "LIMITE_TRANSACCION_NO_CONFIGURADO",
                        "No existe un limite transaccional vigente para el tipo de servicio."
                ));

        if (lineaPago.getMonto().compareTo(limiteTransaccion.getMontoMinimo()) < 0) {
            throw new ReglaNegocioException(
                    "MONTO_MENOR_AL_MINIMO",
                    "El monto de la linea es menor al minimo permitido."
            );
        }
        if (lineaPago.getMonto().compareTo(limiteTransaccion.getMontoMaximo()) > 0) {
            throw new ReglaNegocioException(
                    "MONTO_SUPERA_LIMITE",
                    "El monto de la linea supera el limite permitido."
            );
        }
    }
}
