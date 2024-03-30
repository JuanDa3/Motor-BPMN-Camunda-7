package com.camunda.process.engine.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ControlCementoDTO {

    private int saldo;
    private int entradaKilos;
    private Date fechaEntradaKilos;
    private int salidaKilos;
    private Date fechaSalidaKilos;
    private Produccion produccion;
}
