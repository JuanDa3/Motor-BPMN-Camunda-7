package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.dto.*;
import com.camunda.process.engine.util.HttpUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import static com.camunda.process.engine.util.Utils.*;

@Service
public class ValidarInformacion implements JavaDelegate {
    private Sheet sheet;
    private CargaDatosProceso cargaDatosProceso;
    private boolean bandera = false;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        try {
            String productoFabricado = (String) delegateExecution.getVariable("productoFabricado");
            Long numFormatoCamunda = (Long) delegateExecution.getVariable("numFormato");
            int consecutivo = numFormatoCamunda.intValue();
            String fecha = String.valueOf(cambiarFormatoFechaCamunda(delegateExecution.getVariable("fecha").toString()));
            String responsable = delegateExecution.getVariable("responsable").toString();
            leerDatosExcel();
            cargaDatosProceso = new CargaDatosProceso(consecutivo, fecha, responsable, productoFabricado);
            enviarDatosPersistencia();
            delegateExecution.setVariable("ErrorMessage", mensajeError);
            delegateExecution.setVariable("isValid", bandera);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private void leerDatosExcel() throws IOException {
        FileInputStream inputStream = new FileInputStream(rutaArchivo);
        Workbook workbook = new XSSFWorkbook(inputStream);
        sheet = workbook.getSheetAt(0);
        inputStream.close();
        workbook.close();
    }

    private void enviarDatosPersistencia() {

        try {
            DatosPersistenciaDTO datosPersistenciaDTO = new DatosPersistenciaDTO();
            BitacoraDTO bitacoraDTO = obtenerDatosBitacora();
            ProduccionDTO produccionDTO = obtenerDatosProduccion(bitacoraDTO);

            datosPersistenciaDTO.setBitacora(bitacoraDTO);
            datosPersistenciaDTO.setProduccion(obtenerDatosProduccion(bitacoraDTO));
            datosPersistenciaDTO.setTrasladoMezcla(obtenerDatosTrasladoMezcla(produccionDTO));
            datosPersistenciaDTO.setLecturaContadorAgua(obtenerDatosLecturaContadorAgua(produccionDTO));
            datosPersistenciaDTO.setControlCemento(enviarDatosControlCemento(produccionDTO));
            datosPersistenciaDTO.setListaTiemposParadaMaquina(obtenerListaTiemposParadaMaquina());
            datosPersistenciaDTO.setListaProductoNoConforme(enviarDatosProductoNoConforme());
            datosPersistenciaDTO.setPrueba(enviarDatosPruebasCilindros());
            System.out.println(datosPersistenciaDTO);
            HttpResponse<String> response = HttpUtil.post("persistencia", datosPersistenciaDTO);

            if(response.statusCode() == 200 || response.statusCode() == 201){
                bandera = true;
            }
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private PruebaDTO enviarDatosPruebasCilindros() {
        try {
            return cargaDatosProceso.obtenerDatosPrueba(sheet);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private List<ProductoNoConformeDTO> enviarDatosProductoNoConforme() {
        try {
            return cargaDatosProceso.obtenerProductosNoConformes(sheet);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private List<TiempoParadaMaquinaDTO> obtenerListaTiemposParadaMaquina() {
        try {
            return cargaDatosProceso.obtenerTiemposParadaMaquina(sheet);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private LecturaContadorAguaDTO obtenerDatosLecturaContadorAgua(ProduccionDTO produccionDTO) {
        try {
            return cargaDatosProceso.obtenerLecturaContador(sheet, produccionDTO);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private BitacoraDTO obtenerDatosBitacora() {
        try {
            return cargaDatosProceso.obtenerDatosBitacora(sheet);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            System.out.println("Entra a obtener Datos Bitacora " + e.getMessage());
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private ProduccionDTO obtenerDatosProduccion(BitacoraDTO bitacoraDTO) {
        try {
            return cargaDatosProceso.obtenerDatosProduccion(sheet, bitacoraDTO);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private TrasladoMezclaDTO obtenerDatosTrasladoMezcla(ProduccionDTO produccionDTO) {
        try {
            return cargaDatosProceso.obtenerDatosTrasladoMezcla(sheet, produccionDTO);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private ControlCementoDTO enviarDatosControlCemento(ProduccionDTO produccionDTO) {
        try {
            return cargaDatosProceso.obtenerDatosControlCemento(sheet, produccionDTO);
        } catch (Exception e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }
}
