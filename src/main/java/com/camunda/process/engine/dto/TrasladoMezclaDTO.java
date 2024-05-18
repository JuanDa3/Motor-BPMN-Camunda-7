package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TrasladoMezclaDTO {
    private String deMaquina;
    private String aMaquina;
    private int cantidadKilos;
    private int numBitacora;
}
