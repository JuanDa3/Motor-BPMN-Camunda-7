package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProductoNoConformeDTO {

    private int numBitacora;
    private double cantidad;
    private String tipo;
    private String causa;
}
