package com.camunda.process.engine.procesos.produccion.servicetasks;

import lombok.Getter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.io.*;
import java.text.ParseException;

import static com.camunda.process.engine.util.Utils.*;

@Getter
public class DigitarInformacion implements TaskListener {

    private Long numFormato;

    private String responsable;

    private String tipo;

    private String fecha;

    @Override
    public void notify(DelegateTask delegateTask) {
        mensajeError = "";
        delegateTask.setVariable("ErrorMessage", mensajeError);
        numFormato = (Long) delegateTask.getVariable("numFormato");
        responsable = delegateTask.getVariable("responsable").toString();
        tipo = delegateTask.getVariable("tipoBitacora").toString();
        try {
            fecha = String.valueOf(cambiarFormatoFechaCamunda(delegateTask.getVariable("fecha").toString()));
        } catch (ParseException e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
        String producto = delegateTask.getVariable("productoFabricado").toString();


        try {
            abrirExcel(numFormato,responsable,fecha, producto);
        } catch (IOException e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }
    public void abrirExcel(Long numFormato, String responsable, String fecha, String producto) throws IOException {
        try (FileInputStream file = new FileInputStream(rutaArchivo)) {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Definir el color azul claro con el matiz deseado (RGB: 142, 169, 219)
            byte[] rgb = {(byte) 142, (byte) 169, (byte) 219};
            XSSFColor lightBlue = new XSSFColor(rgb, null);

            // Crear un estilo con el color de fondo azul claro
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(lightBlue);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);

            // Crear y establecer el valor y estilo para cada celda
            crearCeldaConEstilo(sheet, 4, 14, numFormato, style);
            crearCeldaConEstilo(sheet, 2, 9, responsable, style);
            crearCeldaConEstilo(sheet, 2, 0, fecha, style);
            crearCeldaConEstilo(sheet, 2, 5, producto, style);
            crearCeldaConEstilo(sheet, 29, 6, producto, style);

            // Escribir los cambios en el archivo Excel
            try (FileOutputStream outFile = new FileOutputStream(rutaArchivo)) {
                workbook.write(outFile);
                workbook.close();
                file.close();
            }
        } catch (IOException e) {
            mensajeError = e.getMessage();
            throw new BpmnError("Error de Negocio", e.getMessage());
        }
    }

    private void crearCeldaConEstilo(XSSFSheet sheet, int rowNum, int cellNum, Object value, XSSFCellStyle style) {
        XSSFRow row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        XSSFCell cell = row.createCell(cellNum);
        if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

}
