package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bitacora {

    private int consecutivo;

    private String fecha;

    private Maquina maquina;

    private Empleado responsable;

    private Producto producto;
}
