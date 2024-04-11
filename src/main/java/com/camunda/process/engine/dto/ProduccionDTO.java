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
@ToString
public class ProduccionDTO {

    private BitacoraDTO bitacora;

    private String horaInicio;

    private String horaFin;

    private int totalMezcla;

    private int cantidadProductos;

    private int sobranteMezcla;

    private List<MateriaPrimaDTO>listaDeMateriasPrimas = new ArrayList<>();

    private List<ProductoNoConformeDTO> listaProductosNoConformes;

}
