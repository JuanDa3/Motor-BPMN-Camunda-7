package com.camunda.process.engine.dto;

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

    private double productividad;

    private int cantidadProductos;

    private int sobranteMezcla;

    private List<MateriaPrima>listaDeMateriasPrimas = new ArrayList<>();

    private List<ProductoNoConforme> listaProductosNoConformes;

}
