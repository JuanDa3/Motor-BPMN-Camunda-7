package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.util.HttpUtil;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.json.JSONObject;


import java.net.http.HttpResponse;
import java.time.LocalDate;

import static com.camunda.process.engine.util.Utils.*;

public class DiligenciarFormatoProduccion implements TaskListener {

    private boolean bandera = false;

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            Long numFormatoCamunda = (Long) delegateTask.getVariable("numFormato");
            int consecutivo = numFormatoCamunda.intValue();
            String fechaCamunda = String.valueOf(cambiarFormatoFechaCamunda(delegateTask.getVariable("fecha").toString()));
            LocalDate fecha = LocalDate.parse(fechaCamunda);
            HttpResponse<String> response = HttpUtil.get("bitacora?numeroBitacora=" + consecutivo);
            if (response.statusCode() == 200) {
                bandera = true;
            }
            if (response.statusCode() == 500) {
                String responseBody = response.body();
                JSONObject jsonResponse = new JSONObject(responseBody);
                String contenido = jsonResponse.getString("contenido");
                mensajeError = contenido;
                delegateTask.setVariable("ErrorDiligenciarFormato", mensajeError);
            }
            if(validarFecha(fecha)){
                mensajeError = "La fecha no puede ser superior al día actual";
                delegateTask.setVariable("ErrorDiligenciarFormato", mensajeError);
            }

            delegateTask.setVariable("CamposDiligenciados", bandera);

        } catch (Exception e) {
            mensajeError = e.getMessage();
            delegateTask.setVariable("ErrorDiligenciarFormato", mensajeError);
            delegateTask.setVariable("CamposDiligenciados", false);
        }
    }

    private boolean validarFecha(LocalDate fecha) {
        LocalDate fechaActual = LocalDate.now();
        return fecha.isAfter(fechaActual);
    }
}
