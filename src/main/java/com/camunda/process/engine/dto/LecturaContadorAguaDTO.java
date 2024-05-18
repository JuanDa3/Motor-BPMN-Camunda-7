package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LecturaContadorAguaDTO {
    private int lecturaIncial;
    private int lecturafinal;
    private int numBitacora;
}
