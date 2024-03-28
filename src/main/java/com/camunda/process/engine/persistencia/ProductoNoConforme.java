package com.camunda.process.engine.persistencia;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoNoConforme {

    private int numBitacora;
    private double cantidad;
    private String tipo;
    private String causa;
}
