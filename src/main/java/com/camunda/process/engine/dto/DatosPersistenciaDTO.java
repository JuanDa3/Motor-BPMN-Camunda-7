package com.camunda.process.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
