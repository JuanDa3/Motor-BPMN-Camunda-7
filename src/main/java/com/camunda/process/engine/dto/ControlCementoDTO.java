package com.camunda.process.engine.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ControlCementoDTO {

    private double saldo;
    private double entradaKilos;
    private Date fechaEntradaKilos;
    private double salidaKilos;
    private Date fechaSalidaKilos;
    private ProduccionDTO produccionDTO;
}
