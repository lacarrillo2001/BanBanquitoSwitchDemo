package com.banquito.switchpagos.parameter.mapper;

import com.banquito.switchpagos.parameter.constants.ParametroSwitchConstantes;
import com.banquito.switchpagos.parameter.dto.api.HorarioCorteResponse;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class HorarioCorteMapper {

    public HorarioCorteResponse toResponse(LocalTime horaCorteProceso,
                                           LocalTime horaInicioLotesEncolados,
                                           Integer ventanaDuplicidadDias) {
        String horaCorteTexto = horaCorteProceso.toString();
        String mensaje = "Los archivos recibidos despues de las " + horaCorteTexto
                + " se procesan el siguiente dia habil.";
        return new HorarioCorteResponse(
                horaCorteTexto,
                horaInicioLotesEncolados.toString(),
                ventanaDuplicidadDias,
                ParametroSwitchConstantes.ZONA_HORARIA_OPERATIVA,
                mensaje
        );
    }
}
