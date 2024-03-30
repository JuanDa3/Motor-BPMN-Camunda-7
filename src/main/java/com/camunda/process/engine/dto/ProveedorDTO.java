package com.camunda.process.engine.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProveedorDTO {

    private String nombre;
    private String producto;
    private String lote;
}
