package com.banquito.switchpagos.parameter.service.impl;

import com.banquito.switchpagos.parameter.constants.CodigoParametroSwitch;
import com.banquito.switchpagos.parameter.dto.api.HorarioCorteResponse;
import com.banquito.switchpagos.parameter.mapper.HorarioCorteMapper;
import com.banquito.switchpagos.parameter.service.HorarioCorteService;
import com.banquito.switchpagos.parameter.service.ParametroSwitchService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class HorarioCorteServiceImpl implements HorarioCorteService {

    private final ParametroSwitchService parametroSwitchService;
    private final HorarioCorteMapper horarioCorteMapper;

    public HorarioCorteServiceImpl(ParametroSwitchService parametroSwitchService,
                                   HorarioCorteMapper horarioCorteMapper) {
        this.parametroSwitchService = parametroSwitchService;
        this.horarioCorteMapper = horarioCorteMapper;
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
        return horarioCorteMapper.toResponse(horaCorteProceso, horaInicioLotesEncolados, ventanaDuplicidadDias);
    }
}
