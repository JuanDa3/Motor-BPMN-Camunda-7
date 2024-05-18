package com.camunda.process.engine.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DatosPersistenciaDTO {
    private BitacoraDTO bitacora;
    private ProduccionDTO produccion;
    private TrasladoMezclaDTO trasladoMezcla;
    private LecturaContadorAguaDTO lecturaContadorAgua;
    private ControlCementoDTO controlCemento;
    private List<TiempoParadaMaquinaDTO> listaTiemposParadaMaquina;
    private List<ProductoNoConformeDTO>listaProductoNoConforme;
    private PruebaDTO prueba;
}
