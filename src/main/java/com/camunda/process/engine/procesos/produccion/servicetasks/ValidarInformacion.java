package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.persistencia.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static com.camunda.process.engine.util.Constantes.*;
import static com.camunda.process.engine.util.Utils.cambiarFormatoFechaCamunda;
import static com.camunda.process.engine.util.Utils.rutaArchivo;


public class ValidarInformacion implements JavaDelegate {
    private static int  numFormato;
    private static String fecha;

    private static String responsable;


    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long numFormatoCamunda = (Long) delegateExecution.getVariable("numFormato");
        numFormato = numFormatoCamunda.intValue();
        fecha = cambiarFormatoFechaCamunda(delegateExecution.getVariable("fecha").toString());
        responsable = delegateExecution.getVariable("responsable").toString();
        leerInformacionExcel();
        delegateExecution.setVariable("isValid", false);
    }

    public void leerInformacionExcel() throws IOException {
        FileInputStream inputStream = new FileInputStream(new File(rutaArchivo));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Bitacora datosBitacora = obtenerDatosBitacora(sheet);

        Produccion datosProduccion = obtenerDatosProduccion(sheet, datosBitacora);

        List<ProductoNoConforme>listaProductoNoConforme = crearProductoNoConforme(sheet);

        controlCemento(sheet);

        //TODO Control de sobrante
        //TODO Control de validaciones
        //TODO tiempos parada maquina
        //TODO desmoldante
    }

    private Produccion obtenerDatosProduccion(Sheet sheet, Bitacora datosBitacora) {

        Object horaInicioJornada = obtenerValorCelda(sheet, "A5");
        Object horaFinJornada = obtenerValorCelda(sheet, "D5");
        Double totalProduccion = (Double) obtenerValorCelda(sheet, "D26");

        if(horaInicioJornada != null && horaFinJornada != null){
            assert totalProduccion != null;
            LocalTime horaInicio = convertirHoraExcelToLocalTime((Double) horaInicioJornada);
            LocalTime horaFin = convertirHoraExcelToLocalTime((Double) horaFinJornada);
            double productividad = (double)totalProduccion/calcularDiferenciaTiempoEnMinutos(horaInicio,horaFin);

            return Produccion.builder()
                    .bitacora(datosBitacora)
                    .horaInicio(horaInicio)
                    .horaFin(horaFin)
                    .totalMezcla((int)calcularTotalMezcla(sheet))
                    .productividad(productividad)
                    .totalProduccion(totalProduccion.intValue())
                    .materiaPrimas(obtenerValoresMateriasPrimas(sheet))
                    .build();
        }
        throw new BpmnError("Revise Campos de Hora Inicio y Fin Jornada");
    }


    public static double calcularDiferenciaTiempoEnMinutos(LocalTime horaInicio, LocalTime horaFin) {
        // Calcular la diferencia de tiempo entre la hora de inicio y la hora de fin
        Duration diferencia = Duration.between(horaInicio, horaFin);

        // Convertir la diferencia de tiempo a minutos como un double
        return (double) diferencia.toMinutes()/60;
    }

    private List<MateriaPrima> obtenerValoresMateriasPrimas(Sheet sheet) {
        List<MateriaPrima> materiaPrimaList = new ArrayList<>();
        Map<String, String> mensajesError = new HashMap<>();

        Map<String, String> nombresMateriasPrimas = Map.of(
                "E26", ARENA_FINA,
                "F26", ARENA_GRUESA,
                "G26", TRITURADO,
                "H26", CEMENTO,
                "I26",M_50,
                "J26", M_20,
                "K26", AGUA,
                "L26",ADITIVO
        );

        for (Map.Entry<String, String> entry : nombresMateriasPrimas.entrySet()) {
            String celda = entry.getKey();
            String nombreMateriaPrima = entry.getValue();

            Object totalMateriaPrima = obtenerValorCelda(sheet, celda);
            if (totalMateriaPrima != null) {
                Double valorMateriaPrima = (Double) totalMateriaPrima;
                MateriaPrima materiaPrima = MateriaPrima.builder()
                        .nombre(nombreMateriaPrima)
                        .cantidad(valorMateriaPrima.intValue())
                        .build();
                materiaPrimaList.add(materiaPrima);
            } else {
                mensajesError.put(nombreMateriaPrima, "complete los valores de " + nombreMateriaPrima);
            }
        }

        if (!mensajesError.isEmpty()) {
            throw new BpmnError(mensajesError.values().iterator().next());
        }

        return materiaPrimaList;
    }


    private Bitacora obtenerDatosBitacora(Sheet sheet) {

        Object linea = obtenerValorCelda(sheet, "M3");
        Object valorCeldaNombreProducto = obtenerValorCelda(sheet,"F3");
        Object valorCeldaReferencia = obtenerValorCelda(sheet,"H3");
        Object valorCeldaComplemento = obtenerValorCelda(sheet, "O3");
        int peso = calcularPeso(sheet);

        validarDatosObligatorios(linea, "linea");
        validarDatosObligatorios(valorCeldaNombreProducto, "Nombre del Producto");
        validarDatosObligatorios(valorCeldaReferencia, "Referencia del producto");

        if(valorCeldaComplemento == null){
            valorCeldaComplemento = "";
        }

        Maquina maquina = new Maquina(Objects.requireNonNull(obtenerValorCelda(sheet, "D3")).toString());

        LineaProducto lineaProducto = obtenerValorLineaProducto(linea);
        Empleado empleado = new Empleado(responsable);
        Producto producto = Producto.builder()
                .referencia(Objects.requireNonNull(valorCeldaReferencia).toString())
                .complemento(Objects.requireNonNull(valorCeldaComplemento).toString())
                .nombre(Objects.requireNonNull(valorCeldaNombreProducto).toString())
                .peso(peso)
                .build();

        return Bitacora.builder()
                .fecha(fecha)
                .consecutivo(numFormato)
                .lineaProducto(lineaProducto)
                .maquina(maquina)
                .responsable(empleado)
                .producto(producto)
                .build();
    }

    private void validarDatosObligatorios(Object dato, String nombre){
        if(dato == null){
            throw new BpmnError("El siguiente campo no ha sido ingresado " + nombre);
        }
    }

    private int calcularPeso(Sheet sheet) {

        int totalMezcla = (int)calcularTotalMezcla(sheet);

        Double totalProduccionObj = (Double) obtenerValorCelda(sheet, "D26");
        assert totalProduccionObj != null;

        int totalProduccion = totalProduccionObj.intValue();

        return totalMezcla / totalProduccion;
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

    private double calcularTotalMezcla(Sheet sheet){

        Object totalArenaFina = obtenerValorCelda(sheet, "E26");
        Object totalArenaGruesa = obtenerValorCelda(sheet, "F26");
        Object totalTriturado = obtenerValorCelda(sheet, "G26");
        Object totalCemento = obtenerValorCelda(sheet, "H26");

        return convertirValorCeldaADouble(totalArenaFina)
                + convertirValorCeldaADouble(totalArenaGruesa)
                + convertirValorCeldaADouble(totalTriturado)
                + convertirValorCeldaADouble(totalCemento);
    }

    private void controlCemento(Sheet sheet){
        int saldoInicial = 116;
        int entradasDelDia = 0;
        int salidasDelDia = 0;
        int salidasPulida = 0;
        int salidasVentaOtros = 0;
        //TODO en el B30, traer el saldo inicial

        if(obtenerValorCelda(sheet, "B31") != null){
            entradasDelDia = convertirValorCeldaAInt(obtenerValorCelda(sheet, "B31"));
        }

        if(obtenerValorCelda(sheet, "B31") != null){
            salidasDelDia = convertirValorCeldaAInt(obtenerValorCelda(sheet, "B32"));
        }

        if(obtenerValorCelda(sheet, "B31") != null){
            salidasPulida = convertirValorCeldaAInt(obtenerValorCelda(sheet, "B33"));
        }

        if(obtenerValorCelda(sheet, "B31") != null){
            salidasVentaOtros = convertirValorCeldaAInt(obtenerValorCelda(sheet, "B34"));
        }

        saldoInicial = saldoInicial + entradasDelDia - salidasDelDia- salidasPulida - salidasVentaOtros;
    }

    private Object obtenerValorCelda(Sheet sheet, String direccionCelda) {
        CellAddress cellAddress = new CellAddress(direccionCelda);
        Row row = sheet.getRow(cellAddress.getRow());
        Cell cell = row.getCell(cellAddress.getColumn());
        if (cell != null) {
            switch (cell.getCellType()) {
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    return cell.getRichStringCellValue().getString();
                case FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
                        case BOOLEAN:
                            return cell.getBooleanCellValue();
                        case NUMERIC:
                            return cell.getNumericCellValue();
                        case STRING:
                            return cell.getRichStringCellValue().getString();
                    }
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    private Double convertirValorCeldaADouble(Object valor) {
        if (valor == null) {
            throw new RuntimeException("Valores por revisar");
        }
        return (Double) valor;
    }

    private int convertirValorCeldaAInt(Object valor) {
        if (valor == null) {
            throw new RuntimeException("Valores por revisar");
        }
        return (Integer) valor;
    }

    private LocalTime convertirHoraExcelToLocalTime(double valorHoraExcel) {
        int horas = (int) valorHoraExcel;
        int minutos = (int) ((valorHoraExcel - horas) * 60);

        return LocalTime.of(horas, minutos);
    }

    private void guardarDatosProducto(){

    }

    private List<ProductoNoConforme> crearProductoNoConforme(Sheet sheet){

        List<ProductoNoConforme> listaProductoNoConforme= new ArrayList<>();

        for (int i = 8; i <= 25; i++){
            String direccionCelda = "N"+i;
            if(obtenerValorCelda(sheet, direccionCelda) != null){
                ProductoNoConforme productoNoConforme = new ProductoNoConforme();
                productoNoConforme.setNumBitacora(numFormato);
                productoNoConforme.setCantidad((double)obtenerValorCelda(sheet, direccionCelda));
                productoNoConforme.setTipo((String) obtenerValorCelda(sheet, "O" + i));
                productoNoConforme.setCausa( Objects.requireNonNull(obtenerValorCelda(sheet, "P" + i)).toString());
                listaProductoNoConforme.add(productoNoConforme);
            }
        }

        return listaProductoNoConforme;
    }

}
