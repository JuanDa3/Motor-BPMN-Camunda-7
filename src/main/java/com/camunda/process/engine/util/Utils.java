package com.camunda.process.engine.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Utils {

    public static String mensajeError = "";
    public static final String rutaProyecto = new File("").getAbsolutePath();
    public static final String rutaArchivo = rutaProyecto + "/src/main/java/templates/PlantillaConcreto.xlsx";
    public static LocalDate cambiarFormatoFechaCamunda(String fechaCamunda) throws ParseException {
        // Formato de entrada
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        Date fecha = formatoEntrada.parse(fechaCamunda);

        // Convertir Date a Instant y luego a LocalDate
        Instant instant = fecha.toInstant();

        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date cambiarDeStringADate(String fechaString){
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
            return formato.parse(fechaString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
