package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistroContableDTO {
    private int numero;
    private Produccion produccion;
}
