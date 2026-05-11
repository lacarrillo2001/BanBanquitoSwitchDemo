package com.banquito.switchpagos.parametro.service.impl;

import com.banquito.switchpagos.parametro.constants.CodigoParametroSwitch;
import com.banquito.switchpagos.parametro.constants.ParametroSwitchConstantes;
import com.banquito.switchpagos.parametro.dto.api.HorarioCorteResponse;
import com.banquito.switchpagos.parametro.service.HorarioCorteService;
import com.banquito.switchpagos.parametro.service.ParametroSwitchService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class HorarioCorteServiceImpl implements HorarioCorteService {

    private final ParametroSwitchService parametroSwitchService;

    public HorarioCorteServiceImpl(ParametroSwitchService parametroSwitchService) {
        this.parametroSwitchService = parametroSwitchService;
    }

    @Override
    public HorarioCorteResponse obtenerHorarioCorte() {
        LocalTime horaCorteProceso = parametroSwitchService.obtenerHora(CodigoParametroSwitch.HORA_CORTE_PROCESO);
        LocalTime horaInicioLotesEncolados = parametroSwitchService.obtenerHora(
                CodigoParametroSwitch.HORA_INICIO_LOTES_ENCOLADOS
        );
        Integer ventanaDuplicidadDias = parametroSwitchService.obtenerInteger(
                CodigoParametroSwitch.VENTANA_DUPLICIDAD_DIAS
        );
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
