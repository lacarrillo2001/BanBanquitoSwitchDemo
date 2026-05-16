package com.banquito.switchpagos.batch.mapper;

import com.banquito.switchpagos.batch.enums.EstadoColaProcesamiento;
import com.banquito.switchpagos.batch.model.ColaProcesamiento;
import com.banquito.switchpagos.batch.model.LotePago;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
public class ColaProcesamientoMapper {

    public ColaProcesamiento toEntity(LotePago lotePago, LocalDate fechaHabilProgramada,
                                      OffsetDateTime fechaEncolado, OffsetDateTime fechaProgramadaProceso,
                                      Integer maxIntentos) {
        ColaProcesamiento colaProcesamiento = new ColaProcesamiento();
        colaProcesamiento.setLotePago(lotePago);
        colaProcesamiento.setFechaHabilProgramada(fechaHabilProgramada);
        colaProcesamiento.setFechaEncolado(fechaEncolado);
        colaProcesamiento.setFechaProgramadaProceso(fechaProgramadaProceso);
        colaProcesamiento.setEstadoCola(EstadoColaProcesamiento.PENDIENTE);
        colaProcesamiento.setPrioridad(5);
        colaProcesamiento.setIntentos(0);
        colaProcesamiento.setMaxIntentos(maxIntentos);
        return colaProcesamiento;
    }
}
