package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.dto.BitacoraDTO;
import com.camunda.process.engine.dto.ProduccionDTO;
import com.camunda.process.engine.dto.TrasladoMezclaDTO;
import com.camunda.process.engine.util.HttpUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.http.HttpResponse;

import static com.camunda.process.engine.util.Utils.cambiarFormatoFechaCamunda;
import static com.camunda.process.engine.util.Utils.rutaArchivo;


public class ValidarInformacion implements JavaDelegate {
    private  int  consecutivo;
    private String fecha;

    private String responsable;

    private Sheet sheet;

    private String productoFabricado;

    private CargaDatosProceso cargaDatosProceso;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        productoFabricado = (String) delegateExecution.getVariable("productoFabricado");
        Long numFormatoCamunda = (Long) delegateExecution.getVariable("numFormato");
        consecutivo = numFormatoCamunda.intValue();
        fecha = String.valueOf(cambiarFormatoFechaCamunda(delegateExecution.getVariable("fecha").toString()));
        responsable = delegateExecution.getVariable("responsable").toString();
        leerDatosExcel();
        cargaDatosProceso = new CargaDatosProceso(consecutivo, fecha,responsable,productoFabricado);
        enviarDatosPersistencia();
        delegateExecution.setVariable("isValid", false);
    }

    private void leerDatosExcel() throws IOException {
        FileInputStream inputStream = new FileInputStream(rutaArchivo);
        Workbook workbook = new XSSFWorkbook(inputStream);
        sheet = workbook.getSheetAt(0);
    }

    private void enviarDatosPersistencia(){
        enviarDatosBitacora();
        enviarDatosProduccion();
        enviarDatosTrasladoMezcla();
        enviarDatosLecturaContadorAgua();
    }

    private void enviarDatosLecturaContadorAgua() {
        try {
            BitacoraDTO bitacoraDTO = cargaDatosProceso.obtenerDatosBitacora(sheet);
            ProduccionDTO produccionDTO = cargaDatosProceso.obtenerDatosProduccion(sheet, bitacoraDTO);
            Object body = cargaDatosProceso.obtenerLecturaContador(sheet, produccionDTO);
            HttpResponse<String>response = HttpUtil.post("lectura-contador", body);
        } catch (Exception e) {
            throw new BpmnError(e.getMessage());
        }
    }

    private void enviarDatosBitacora() {
        try {
            Object body = cargaDatosProceso.obtenerDatosBitacora(sheet);
            HttpResponse<String>response = HttpUtil.post("bitacora", body);
        } catch (Exception e) {
            throw new BpmnError(e.getMessage());
        }
    }

    private void enviarDatosProduccion(){
        try {
            BitacoraDTO bitacoraDTO = cargaDatosProceso.obtenerDatosBitacora(sheet);
            Object body = cargaDatosProceso.obtenerDatosProduccion(sheet, bitacoraDTO);
            HttpResponse<String>response = HttpUtil.post("produccion", body);
        } catch (Exception e) {
            throw new BpmnError(e.getMessage());
        }
    }

    private void enviarDatosTrasladoMezcla(){
        try {
            BitacoraDTO bitacoraDTO = cargaDatosProceso.obtenerDatosBitacora(sheet);
            ProduccionDTO produccionDTO = cargaDatosProceso.obtenerDatosProduccion(sheet, bitacoraDTO);
            Object body = cargaDatosProceso.obtenerDatosTrasladoMezcla(sheet, produccionDTO);
            HttpResponse<String>response = HttpUtil.post("traslado-mezcla", body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
