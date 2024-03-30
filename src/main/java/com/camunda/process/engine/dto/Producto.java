package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Producto {

    private String nombre;
    private String referencia;
    private String complemento;
    private String referenciaP1;
    private String linea;
    private int peso;
}
