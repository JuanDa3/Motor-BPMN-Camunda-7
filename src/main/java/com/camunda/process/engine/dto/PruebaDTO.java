package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PruebaDTO {
    private int numero;
    private int numero_cocha;
    private String resultado;
    private ProduccionDTO produccionDTO;
    private EmpleadoDTO empleadoDTO;
}
