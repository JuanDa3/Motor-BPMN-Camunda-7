package com.camunda.process.engine.persistencia;

import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produccion {

    private Bitacora bitacora;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private int totalMezcla;

    private int totalProduccion;

    private double productividad;

    private List<MateriaPrima>materiaPrimas = new ArrayList<>();

}
