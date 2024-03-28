package com.camunda.process.engine.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Utils {

    public static final String rutaProyecto = new File("").getAbsolutePath();
    public static final String rutaArchivo = rutaProyecto + "/src/main/java/templates/PlantillaConcreto.xlsx";
    public static String cambiarFormatoFechaCamunda(String fechaCamunda) throws ParseException {
        // Formato de entrada
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        Date fecha = formatoEntrada.parse(fechaCamunda);

        // Convertir Date a LocalDateTime
        LocalDateTime localDateTime = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Formatear LocalDateTime al formato deseado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return localDateTime.format(formatter);
    }
}
