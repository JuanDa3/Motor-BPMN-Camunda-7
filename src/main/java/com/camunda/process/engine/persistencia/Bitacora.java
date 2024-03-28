package com.camunda.process.engine.persistencia;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bitacora {

    private String fecha;

    private int consecutivo;

    private LineaProducto lineaProducto;

    private Maquina maquina;

    private Empleado responsable;

    private Producto producto;
}
