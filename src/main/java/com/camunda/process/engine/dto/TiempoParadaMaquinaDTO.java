package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TiempoParadaMaquinaDTO {
    private int tipo;
    private int minutos;
    private int consecutivoBitacora;
}
