package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.dto.RegistroContableDTO;
import com.camunda.process.engine.util.HttpUtil;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import static com.camunda.process.engine.util.Utils.mensajeError;

public class AgregarCodigoMP implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        Long registroContableMP = (Long) delegateTask.getVariable("registroContableMP");
        Long numBitacora = (Long) delegateTask.getVariable("numFormato");
        agregarCodigoMP(registroContableMP, numBitacora);
    }

    private void agregarCodigoMP(Long registroContableMP, Long numBitacora) {
        try {
            RegistroContableDTO registroContableDTO = new RegistroContableDTO();
            registroContableDTO.setNumero(Math.toIntExact(registroContableMP));
            registroContableDTO.setBitacora(Math.toIntExact(numBitacora));
            System.out.println(registroContableDTO);
            HttpUtil.post("registro-contable", registroContableDTO);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("ErrorMP", e.getMessage());
        }
    }

}
