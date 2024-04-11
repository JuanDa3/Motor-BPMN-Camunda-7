package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturaContadorAguaDTO {
    private int lecturaIncial;
    private int lecturafinal;
    private ProduccionDTO produccionDTO;
}
