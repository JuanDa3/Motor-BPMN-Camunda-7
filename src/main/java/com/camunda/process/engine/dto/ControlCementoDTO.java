package com.camunda.process.engine.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ControlCementoDTO {

    private double saldo;
    private double entradaKilos;
    private String fechaEntradaKilos;
    private double salidaKilos;
    private String fechaSalidaKilos;
    private int numBitacora;
}
