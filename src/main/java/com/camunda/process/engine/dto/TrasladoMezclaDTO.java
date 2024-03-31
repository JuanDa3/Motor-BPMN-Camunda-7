package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrasladoMezclaDTO {
    private String deMaqunina;
    private String aMaqunina;
    private int cantidadKilos;
    private Produccion produccion;
}
