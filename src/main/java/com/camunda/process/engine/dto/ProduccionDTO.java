package com.camunda.process.engine.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProduccionDTO {

    private int numBitacora;

    private String horaInicio;

    private String horaFin;

    private int totalMezcla;

    private int cantidadProductos;

    private int cementoPulir;

    private int sobrante;

    private List<MateriaPrimaDTO>listaDeMateriasPrimas = new ArrayList<>();

    private List<ProductoNoConformeDTO> listaProductosNoConformes;

}
