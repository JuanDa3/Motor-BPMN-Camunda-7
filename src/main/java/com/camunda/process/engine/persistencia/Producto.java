package com.camunda.process.engine.persistencia;

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
    private TipoProducto tipo;
    private int peso;
}
