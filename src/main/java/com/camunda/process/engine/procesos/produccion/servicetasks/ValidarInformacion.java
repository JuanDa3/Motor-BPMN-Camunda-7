package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.dto.LineaProducto;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import static com.camunda.process.engine.util.Utils.cambiarFormatoFechaCamunda;


public class ValidarInformacion implements JavaDelegate {
    private  int  consecutivo;
    private String fecha;

    private String responsable;

    private String productoFabricado;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long numFormatoCamunda = (Long) delegateExecution.getVariable("numFormato");
        consecutivo = numFormatoCamunda.intValue();
        fecha = cambiarFormatoFechaCamunda(delegateExecution.getVariable("fecha").toString());
        responsable = delegateExecution.getVariable("responsable").toString();

        CargaDatosProceso cargaDatosProceso = new CargaDatosProceso(consecutivo, fecha,responsable,productoFabricado);

        delegateExecution.setVariable("isValid", false);
    }

    private LineaProducto obtenerValorLineaProducto(Object valorCelda){
        if (valorCelda == null){
            return null;
        }

        String valor = valorCelda.toString();

        return switch (valor) {
            case "PC" -> LineaProducto.PC;
            case "TC" -> LineaProducto.TC;
            default -> throw new BpmnError("Valor de Linea de producto no validos");
        };
    }

}
