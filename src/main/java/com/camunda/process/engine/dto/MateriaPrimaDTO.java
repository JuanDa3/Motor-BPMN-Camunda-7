package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MateriaPrimaDTO {

    private String nombre;

    private int cantidad;
}
