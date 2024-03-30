package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.dto.*;
import com.camunda.process.engine.util.SaldoCementoUtil;
import com.google.gson.Gson;
import lombok.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.BpmnError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static com.camunda.process.engine.util.Constantes.*;
import static com.camunda.process.engine.util.Utils.rutaArchivo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargaDatosProceso {

    private int consecutivo;

    private String fecha;

    private String responsable;

    private String productoFabricado;
    public void leerInformacionExcel() throws IOException {
        FileInputStream inputStream = new FileInputStream(new File(rutaArchivo));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Bitacora datosBitacora = obtenerDatosBitacora(sheet);
        Produccion datosProduccion = obtenerDatosProduccion(sheet, datosBitacora);
        List<ProductoNoConforme> listaProductoNoConforme = crearProductoNoConforme(sheet, datosProduccion);
        ArrayList<ProveedorDTO> listaProveedores = obtenerDatosProveedor(sheet);

        ControlCementoDTO controlCementoDTO = controlCemento(sheet);

        if(!listaProductoNoConforme.isEmpty()){
            datosProduccion.setListaProductosNoConformes(listaProductoNoConforme);
        }

    }

    private ArrayList<ProveedorDTO> obtenerDatosProveedor(Sheet sheet) {
        ArrayList<ProveedorDTO> listaProveedores = new ArrayList<>();
        ArrayList<String> listaProductosProveedores = new ArrayList<>();

        // Mapeo de celdas de proveedores a sus respectivos lotes
        Map<String, String> proveedorLoteMap = new HashMap<>();
        proveedorLoteMap.put(CELDA_PROVEEDOR_ARENA_FINA, CELDA_LOTE_ARENA_FINA);
        proveedorLoteMap.put(CELDA_PROVEEDOR_ARENA_GRUESA, CELDA_LOTE_ARENA_GRUESA);
        proveedorLoteMap.put(CELDA_PROVEEDOR_TRITURADO, CELDA_LOTE_TRITURADO);
        proveedorLoteMap.put(CELDA_PROVEEDOR_CEMENTO, CELDA_LOTE_CEMENTO);
        proveedorLoteMap.put(CELDA_PROVEEDOR_M50, CELDA_LOTE_M50);
        proveedorLoteMap.put(CELDA_PROVEEDOR_M20, CELDA_LOTE_M20);
        proveedorLoteMap.put(CELDA_PROVEEDOR_ADITIVO, CELDA_LOTE_ADITIVO);
        proveedorLoteMap.put(CELDA_PROVEEDOR_HIERRO, CELDA_LOTE_HIERRO);
        proveedorLoteMap.put(CELDA_PROVEEDOR_OTRO_1, CELDA_LOTE_OTRO_1);
        proveedorLoteMap.put(CELDA_PROVEEDOR_OTRO_2, CELDA_LOTE_OTRO_2);
        // Nombre de los productos
        listaProductosProveedores.add(ARENA_FINA);
        listaProductosProveedores.add(ARENA_GRUESA);
        listaProductosProveedores.add(TRITURADO);
        listaProductosProveedores.add(CEMENTO);
        listaProductosProveedores.add(M_50);
        listaProductosProveedores.add(M_20);
        listaProductosProveedores.add(ADITIVO);
        listaProductosProveedores.add(HIERRO);
        listaProductosProveedores.add(OTRO_1);
        listaProductosProveedores.add(OTRO_2);

        List<String> keys = new ArrayList<>(proveedorLoteMap.keySet());
        List<String> values = new ArrayList<>(proveedorLoteMap.values());

        for (int i = 0; i < proveedorLoteMap.size(); i++) {
            String proveedor = (String) obtenerValorCelda(sheet, keys.get(i));
            String lote = (String) obtenerValorCelda(sheet, values.get(i));

            if (proveedor != null && lote != null) {
                ProveedorDTO proveedorDTO = ProveedorDTO.builder()
                        .nombre(proveedor)
                        .producto(listaProductosProveedores.get(i))
                        .lote(lote)
                        .build();
                listaProveedores.add(proveedorDTO);
            }
        }

        return listaProveedores;
    }


    private Bitacora obtenerDatosBitacora(Sheet sheet) {

        String linea = (String) obtenerValorCelda(sheet, CELDA_LINEA);

        validarDatosObligatorios(linea, "linea");
        validarDatosObligatorios(consecutivo, "consecutivo de la Bitacora");
        validarDatosObligatorios(fecha, "Fecha de la Bitacota");

        Maquina maquina = getMaquina(sheet);
        Empleado responsable = getEmpleado();
        Producto producto = getProducto(sheet);

        return Bitacora.builder()
                .consecutivo(consecutivo)
                .fecha(fecha)
                .maquina(maquina)
                .responsable(responsable)
                .producto(producto)
                .build();
    }

    private Produccion obtenerDatosProduccion(Sheet sheet, Bitacora datosBitacora) {
        Object valorCeldaHoraInicioJornada = obtenerValorCelda(sheet, CELDA_HORA_INICIO_JORNADA);
        Object valorCeldaHoraFinJornada = obtenerValorCelda(sheet, CELDA_HORA_FIN_JORNADA);
        Object valorCeldaCantidadProductos = obtenerValorCelda(sheet, CELDA_CANTIDAD_PRODUCTOS);
        Object valorCeldaSobranteMezcla = obtenerValorCelda(sheet, CELDA_SOBRANTE_MEZCLA);

        validarDatosObligatorios(valorCeldaHoraInicioJornada, "Hora Inicio Jornada");
        validarDatosObligatorios(valorCeldaHoraFinJornada, "Hora Fin Jornada");
        validarDatosObligatorios(valorCeldaCantidadProductos, "Cantidad Productos Fabricados");

        int cantidadProductos = (int) valorCeldaCantidadProductos;
        int sobranteMezcla = 0;

        // Validación de la cantidad de productos
        if (cantidadProductos <= 0) {
            throw new IllegalArgumentException("La cantidad de productos fabricados debe ser un número positivo");
        }

        if(valorCeldaSobranteMezcla != null){
            sobranteMezcla = (int)valorCeldaSobranteMezcla;
        }

        LocalTime horaInicio = convertirHoraExcelToLocalTime((Double) valorCeldaHoraInicioJornada);
        LocalTime horaFin = convertirHoraExcelToLocalTime((Double) valorCeldaHoraFinJornada);
        double productividad = cantidadProductos/calcularDiferenciaTiempoEnMinutos(horaInicio,horaFin);

        return Produccion.builder()
                    .bitacora(datosBitacora)
                    .horaInicio(horaInicio)
                    .horaFin(horaFin)
                    .totalMezcla((int)calcularTotalMezcla(sheet))
                    .productividad(productividad)
                    .cantidadProductos(cantidadProductos)
                    .sobranteMezcla(sobranteMezcla)
                    .listaDeMateriasPrimas(obtenerValoresMateriasPrimas(sheet))
                    .build();

    }
    private Maquina getMaquina(Sheet sheet) {
        Object valorCelda = obtenerValorCelda(sheet, CELDA_MAQUINA);

        validarDatosObligatorios(valorCelda, "Nombre Maquina");

        String nombreMaquina = (String) valorCelda;

        return new Maquina(nombreMaquina);
    }
    private Empleado getEmpleado() {
        return new Empleado(responsable);
    }
    private Producto getProducto(Sheet sheet) {
        Object valorCeldaReferencia = obtenerValorCelda(sheet, CELDA_REFERENCIA);
        Object valorCeldaComplemento = obtenerValorCelda(sheet, CELDA_COMPLEMENTO);
        Object valorCeldaRefP1 = obtenerValorCelda(sheet, CELDA_REFERENCIA_P1);
        Object valorCeldaLinea = obtenerValorCelda(sheet, CELDA_LINEA);
        String complemento = "";
        String refp1 = "";

        validarDatosObligatorios(valorCeldaReferencia, "Referencia del producto");
        validarDatosObligatorios(valorCeldaLinea, "Linea del producto");

        String referencia = (String)valorCeldaReferencia;
        String linea = (String) valorCeldaLinea;

        if(valorCeldaComplemento != null){
            complemento = (String) valorCeldaComplemento;
        }

        if(valorCeldaRefP1 != null){
            refp1 = (String) valorCeldaRefP1;
        }

        int pesoProducto = calcularPesoProducto(sheet);

        return Producto.builder()
                .referencia(referencia)
                .referenciaP1(refp1)
                .complemento(complemento)
                .nombre(productoFabricado)
                .peso(pesoProducto)
                .linea(linea)
                .build();
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

    private void validarDatosObligatorios(Object dato, String nombre){
        if(dato == null){
            throw new BpmnError("El siguiente campo no ha sido ingresado " + nombre);
        }
    }

    private int calcularPesoProducto(Sheet sheet) {
        int totalMezcla = (int) calcularTotalMezcla(sheet);

        Double totalProduccionObj = (Double) obtenerValorCelda(sheet, CELDA_TOTAL_MEZCLA);
        if (totalProduccionObj == null || totalProduccionObj == 0) {
            throw new IllegalArgumentException("El total de producción es inválido o cero");
        }
        int totalProduccion = totalProduccionObj.intValue();

        return totalMezcla / totalProduccion;
    }
    private double calcularTotalMezcla(Sheet sheet) {
        Object totalArenaFina = obtenerValorCelda(sheet, CELDA_TOTAL_ARENA_FINA);
        Object totalArenaGruesa = obtenerValorCelda(sheet, CELDA_TOTAL_ARENA_GRUESA);
        Object totalTriturado = obtenerValorCelda(sheet, CELDA_TOTAL_TRITURADO);
        Object totalCemento = obtenerValorCelda(sheet, CELDA_TOTAL_CEMENTO);

        return convertirValorCeldaADouble(totalArenaFina, "Total Arena Fina")
                + convertirValorCeldaADouble(totalArenaGruesa, "Total Arena Gruesa")
                + convertirValorCeldaADouble(totalTriturado, "Total Triturado")
                + convertirValorCeldaADouble(totalCemento, "Total Cemento");
    }

    private Double convertirValorCeldaADouble(Object valor, String celda) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de la celda " + celda + " no puede ser nulo");
        }
        if (!(valor instanceof Double)) {
            throw new IllegalArgumentException("El valor de la celda " + celda + " no es un número válido");
        }
        return (Double) valor;
    }
    private LocalTime convertirHoraExcelToLocalTime(double valorHoraExcel) {
        int horas = (int) valorHoraExcel;
        int minutos = (int) ((valorHoraExcel - horas) * 60);

        return LocalTime.of(horas, minutos);
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
                CELDA_TOTAL_ARENA_FINA, ARENA_FINA,
                CELDA_TOTAL_ARENA_GRUESA, ARENA_GRUESA,
                CELDA_TOTAL_TRITURADO, TRITURADO,
                CELDA_TOTAL_CEMENTO, CEMENTO,
                CELDA_TOTAL_M50,M_50,
                CELDA_TOTAL_M20, M_20,
                CELDA_TOTAL_AGUA, AGUA,
                CELDA_TOTAL_ADITIVO,ADITIVO
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

    private List<ProductoNoConforme> crearProductoNoConforme(Sheet sheet, Produccion produccion){
        List<ProductoNoConforme> listaProductoNoConforme = new ArrayList<>();

        for (int i = 8; i <= 25; i++){
            String direccionCeldaCantidad = CELDA_CANTIDAD_PNC + i;
            Object valorCeldaCantidad = obtenerValorCelda(sheet, direccionCeldaCantidad);

            if (valorCeldaCantidad != null){
                int cantidad = (int) valorCeldaCantidad;

                if (cantidad > 0){
                    String tipoNC = obtenerTipoNC(sheet, i);
                    String causaNC = obtenerCausaNC(sheet, i);
                    validarDatosObligatorios(tipoNC, "Tipo NC");
                    validarDatosObligatorios(causaNC, "Causa NC");

                    ProductoNoConforme productoNoConforme = new ProductoNoConforme();
                    productoNoConforme.setNumBitacora(consecutivo);
                    productoNoConforme.setCantidad(cantidad);
                    productoNoConforme.setTipo(tipoNC);
                    productoNoConforme.setCausa(causaNC);
                    productoNoConforme.setProduccion(produccion);
                    listaProductoNoConforme.add(productoNoConforme);
                }
            }
        }

        return listaProductoNoConforme;
    }

    private String obtenerTipoNC(Sheet sheet, int index){
        return (String) obtenerValorCelda(sheet, CELDA_TIPO_PNC + index);
    }

    private String obtenerCausaNC(Sheet sheet, int index){
        return (String) obtenerValorCelda(sheet, CELDA_CAUSA_PNC + index);
    }
    private ControlCementoDTO controlCemento(Sheet sheet) {
        int saldoInicial = SaldoCementoUtil.obtenerSaldo();
        int entradasDelDia = convertirValorCeldaAInt(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_ENTRADAS_DIA));
        int salidasDelDia = convertirValorCeldaAInt(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_DIA));
        int salidasPulida = convertirValorCeldaAInt(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_PULIDA));
        int salidasVentaOtros = convertirValorCeldaAInt(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_VENTA_OTROS));

        int saldoFinal = saldoInicial + entradasDelDia - salidasDelDia - salidasPulida - salidasVentaOtros;

        return ControlCementoDTO.builder()
                .saldo(saldoFinal)
                //si no pusieron, toca poner 0
                .build();
    }


    private int convertirValorCeldaAInt(Object valorCelda) {
        try {
            return Integer.parseInt(String.valueOf(valorCelda));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al convertir el valor de la celda a entero: " + e.getMessage());
        }
    }

}
