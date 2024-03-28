package com.camunda.process.engine.persistencia;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaPrima {

    private String nombre;

    private int cantidad;
}
