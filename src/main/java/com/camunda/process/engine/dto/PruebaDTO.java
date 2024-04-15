package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PruebaDTO {
    private int numero;
    private int numero_cocha;
    private int consecutivoBitacora;
    private String nombreResponsable;
}
