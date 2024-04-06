package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BitacoraDTO {

    private int consecutivo;

    private String fecha;

    private MaquinaDTO maquinaDTO;

    private EmpleadoDTO empleadoDTO;

    private Boolean esPrincipal;

    private ProductoDTO productoDTO;
}
