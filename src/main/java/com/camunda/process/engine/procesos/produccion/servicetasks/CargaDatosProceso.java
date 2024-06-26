package com.camunda.process.engine.procesos.produccion.servicetasks;

import com.camunda.process.engine.dto.*;
import com.camunda.process.engine.util.SaldoCementoUtil;
import lombok.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.camunda.bpm.engine.delegate.BpmnError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.camunda.process.engine.util.Constantes.*;
import static com.camunda.process.engine.util.Utils.mensajeError;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargaDatosProceso {

    private int consecutivo;

    private String fecha;

    private String responsable;

    private String nombre;

    public LecturaContadorAguaDTO obtenerLecturaContador(Sheet sheet, ProduccionDTO datosProduccionDTO) {

        int lecturaIncial = convertirValorCeldaAInt(obtenerValorCelda(sheet, CELDA_CONTADOR_AGUA_LECTURA_INICIAL));
        int lecturaFinal = convertirValorCeldaAInt(obtenerValorCelda(sheet, CELDA_CONTADOR_AGUA_LECTURA_FINAL));
        if (lecturaIncial > 0 && lecturaFinal > 0) {
            return LecturaContadorAguaDTO.builder()
                    .lecturaIncial(lecturaIncial)
                    .lecturafinal(lecturaFinal)
                    .numBitacora(datosProduccionDTO.getNumBitacora())
                    .build();
        }
        return null;
    }

    public BitacoraDTO obtenerDatosBitacora(Sheet sheet) {

        String linea = (String) obtenerValorCelda(sheet, CELDA_LINEA);

        validarDatosObligatorios(linea, "linea");
        validarDatosObligatorios(consecutivo, "consecutivo de la Bitacora");
        validarDatosObligatorios(fecha, "Fecha de la Bitacota");

        MaquinaDTO maquinaDTO = getMaquina(sheet);
        EmpleadoDTO empleadoDTO = getEmpleado();
        ProductoDTO productoDTO = getProducto(sheet);

        return BitacoraDTO.builder()
                .consecutivo(consecutivo)
                .fecha(fecha)
                .maquinaDTO(maquinaDTO)
                .empleadoDTO(empleadoDTO)
                .productoDTO(productoDTO)
                .build();
    }

    public ProduccionDTO obtenerDatosProduccion(Sheet sheet, BitacoraDTO datosBitacora) {
        Double valorCeldaHoraInicioJornada = (Double) obtenerValorCelda(sheet, CELDA_HORA_INICIO_JORNADA);
        Double valorCeldaHoraFinJornada = (Double) obtenerValorCelda(sheet, CELDA_HORA_FIN_JORNADA);
        Double valorCeldaCantidadProductos = (Double) obtenerValorCelda(sheet, CELDA_CANTIDAD_PRODUCTOS);
        Double valorCeldaSobranteMezcla = (Double) obtenerValorCelda(sheet, CELDA_SOBRANTE_MEZCLA);
        Double valorCeldaCementoPulir = (Double) obtenerValorCelda(sheet, CELDA_CEMENTO_PULIR_KILOS);

        validarDatosObligatorios(valorCeldaHoraInicioJornada, "Hora Inicio Jornada");
        validarDatosObligatorios(valorCeldaHoraFinJornada, "Hora Fin Jornada");
        validarDatosObligatorios(valorCeldaCantidadProductos, "Cantidad Productos Fabricados");
        int cantidadProductos = valorCeldaCantidadProductos.intValue();
        int sobranteMezcla = 0;

        // Validación de la cantidad de productos
        if (cantidadProductos <= 0) {
            throw new BpmnError("Error de Negocio", "La cantidad de productos fabricados debe ser un número positivo");
        }

        if (valorCeldaSobranteMezcla != null) {
            sobranteMezcla = valorCeldaSobranteMezcla.intValue();
        }

        if(valorCeldaCementoPulir == null){
            valorCeldaCementoPulir = 0.0;
        }

        String horaInicio = convertirHoraFormatoExcel(valorCeldaHoraInicioJornada);
        String horaFin = convertirHoraFormatoExcel(valorCeldaHoraFinJornada);

        return ProduccionDTO.builder()
                .numBitacora(datosBitacora.getConsecutivo())
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .totalMezcla((int) calcularTotalMezcla(sheet))
                .cantidadProductos(cantidadProductos)
                .cementoPulir(valorCeldaCementoPulir.intValue())
                .sobrante(sobranteMezcla)
                .listaDeMateriasPrimas(obtenerValoresMateriasPrimas(sheet))
                .build();

    }

    public String convertirHoraFormatoExcel(Double horaFormatoExcel) {
        double horas = (horaFormatoExcel * 24);
        double minutos = (horaFormatoExcel * 24 - horas) * 60;

        return String.format("%02d:%02d:00", (int) horas, (int) minutos);
    }

    public MaquinaDTO getMaquina(Sheet sheet) {
        Object valorCelda = obtenerValorCelda(sheet, CELDA_MAQUINA);

        validarDatosObligatorios(valorCelda, "Nombre Maquina");

        String nombreMaquina = (String) valorCelda;

        return new MaquinaDTO(nombreMaquina);
    }

    private EmpleadoDTO getEmpleado() {
        return new EmpleadoDTO(responsable);
    }

    private ProductoDTO getProducto(Sheet sheet) {
        Object valorCeldaReferencia = obtenerValorCelda(sheet, CELDA_REFERENCIA);
        Object valorCeldaComplemento = obtenerValorCelda(sheet, CELDA_COMPLEMENTO);
        Object valorCeldaRefP1 = obtenerValorCelda(sheet, CELDA_REFERENCIA_P1);
        Object valorCeldaLinea = obtenerValorCelda(sheet, CELDA_LINEA);
        String complemento = "";
        String refp1 = "";

        validarDatosObligatorios(valorCeldaReferencia, "Referencia del producto");
        validarDatosObligatorios(valorCeldaLinea, "Linea del producto");

        String referencia = valorCeldaReferencia.toString();
        String linea = (String) valorCeldaLinea;

        if (valorCeldaComplemento != null) {
            complemento = (String) valorCeldaComplemento;
        }

        if (valorCeldaRefP1 != null) {
            refp1 = (String) valorCeldaRefP1;
        }

        int pesoProducto = calcularPesoProducto(sheet);

        return ProductoDTO.builder()
                .referencia(referencia)
                .referenciaP1(refp1)
                .complemento(complemento)
                .nombre(nombre)
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

    private void validarDatosObligatorios(Object dato, String nombre) {
        if (dato == null) {
            mensajeError = "El siguiente campo no ha sido ingresado";
            throw new BpmnError("Error de Negocio", "El siguiente campo no ha sido ingresado" + nombre);
        }
    }

    public int calcularPesoProducto(Sheet sheet) {
        int totalMezcla = (int) calcularTotalMezcla(sheet);

        Double totalProduccionObj = (Double) obtenerValorCelda(sheet, CELDA_TOTAL_MEZCLA);
        if (totalProduccionObj == null || totalProduccionObj == 0) {
            mensajeError = "El total de producción es inválido o cero";
            throw new BpmnError("Error de Negocio", "El total de producción es inválido o cero");

        }
        int totalProduccion = totalProduccionObj.intValue();

        return totalMezcla / totalProduccion;
    }

    public double calcularTotalMezcla(Sheet sheet) {
        Object totalArenaFina = obtenerValorCelda(sheet, CELDA_TOTAL_ARENA_FINA);
        Object totalArenaGruesa = obtenerValorCelda(sheet, CELDA_TOTAL_ARENA_GRUESA);
        Object totalTriturado = obtenerValorCelda(sheet, CELDA_TOTAL_TRITURADO);
        Object totalCemento = obtenerValorCelda(sheet, CELDA_TOTAL_CEMENTO);

        return convertirValoresObligatoriosCeldaADouble(totalArenaFina, "Total Arena Fina")
                + convertirValoresObligatoriosCeldaADouble(totalArenaGruesa, "Total Arena Gruesa")
                + convertirValoresObligatoriosCeldaADouble(totalTriturado, "Total Triturado")
                + convertirValoresObligatoriosCeldaADouble(totalCemento, "Total Cemento");
    }

    private Double convertirValoresObligatoriosCeldaADouble(Object valor, String celda) {
        if (valor == null) {
            throw new BpmnError("Error de Negocio", "El valor de la celda " + celda + " no puede ser nulo");
        }
        if (!(valor instanceof Double)) {
            throw new BpmnError("Error de Negocio", "El valor de la celda " + celda + " no es un número válido");
        }
        return (Double) valor;
    }

    private List<MateriaPrimaDTO> obtenerValoresMateriasPrimas(Sheet sheet) {
        List<MateriaPrimaDTO> materiaPrimaDTOList = new ArrayList<>();
        Map<String, String> mensajesError = new HashMap<>();

        Map<String, String> nombresMateriasPrimas = Map.of(
                CELDA_TOTAL_ARENA_FINA, ARENA_FINA,
                CELDA_TOTAL_ARENA_GRUESA, ARENA_GRUESA,
                CELDA_TOTAL_TRITURADO, TRITURADO,
                CELDA_TOTAL_CEMENTO, CEMENTO,
                CELDA_TOTAL_AGUA, AGUA,
                CELDA_TOTAL_ADITIVO, ADITIVO,
                CELDA_TOTAL_ACELERANTE, ACELERANTE,
                CELDA_DESMOLDANTE, DESMOLDANTE
        );

        for (Map.Entry<String, String> entry : nombresMateriasPrimas.entrySet()) {
            String celda = entry.getKey();
            String nombreMateriaPrima = entry.getValue();

            Object totalMateriaPrima = obtenerValorCelda(sheet, celda);
            if (totalMateriaPrima != null && !celda.equals(ACELERANTE)) {
                Double valorMateriaPrima = (Double) totalMateriaPrima;
                MateriaPrimaDTO materiaPrimaDTO = MateriaPrimaDTO.builder()
                        .nombre(nombreMateriaPrima)
                        .cantidad(valorMateriaPrima.intValue())
                        .build();
                materiaPrimaDTOList.add(materiaPrimaDTO);
            } else if (totalMateriaPrima != null) {
                Double valorMateriaPrima = (Double) totalMateriaPrima;
                MateriaPrimaDTO materiaPrimaDTO = MateriaPrimaDTO.builder()
                        .nombre(nombreMateriaPrima)
                        .cantidad(valorMateriaPrima.intValue())
                        .build();
                materiaPrimaDTOList.add(materiaPrimaDTO);
            } else {
                mensajesError.put(nombreMateriaPrima, "complete los valores de " + nombreMateriaPrima);
            }
        }

        if (!mensajesError.isEmpty()) {
            mensajeError = mensajesError.values().iterator().next();
            throw new BpmnError("Error de Negocio", mensajesError.values().iterator().next());
        }

        return materiaPrimaDTOList;
    }

    public List<ProductoNoConformeDTO> obtenerProductosNoConformes(Sheet sheet) {
        List<ProductoNoConformeDTO> listaProductoNoConformeDTO = new ArrayList<>();

        for (int i = 8; i <= 25; i++) {
            String direccionCeldaCantidad = CELDA_CANTIDAD_PNC + i;
            Double valorCeldaCantidad = convertirValoresCeldaADouble(obtenerValorCelda(sheet, direccionCeldaCantidad), direccionCeldaCantidad);

            if (valorCeldaCantidad > 0) {
                int cantidad = valorCeldaCantidad.intValue();
                String tipoNC = obtenerTipoNC(sheet, i);
                String causaNC = obtenerCausaNC(sheet, i);
                validarDatosObligatorios(tipoNC, "Tipo NC");
                validarDatosObligatorios(causaNC, "Causa NC");

                ProductoNoConformeDTO productoNoConformeDTO = new ProductoNoConformeDTO();
                productoNoConformeDTO.setNumBitacora(consecutivo);
                productoNoConformeDTO.setCantidad(cantidad);
                productoNoConformeDTO.setTipo(tipoNC);
                productoNoConformeDTO.setCausa(causaNC);
                listaProductoNoConformeDTO.add(productoNoConformeDTO);

            }
        }

        return listaProductoNoConformeDTO;
    }

    private String obtenerTipoNC(Sheet sheet, int index) {
        return (String) obtenerValorCelda(sheet, CELDA_TIPO_PNC + index);
    }

    private String obtenerCausaNC(Sheet sheet, int index) {
        Double codigoCausaPNC = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CAUSA_PNC + index), CELDA_CAUSA_PNC);
        return String.valueOf(codigoCausaPNC.intValue());
    }

    public ControlCementoDTO obtenerDatosControlCemento(Sheet sheet, ProduccionDTO produccionDTO) {
        double saldoInicialKilos = SaldoCementoUtil.obtenerSaldo();

        double entradasDelDia42K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_ENTRADAS_DIA_42K), CELDA_CONTROL_DE_CEMENTO_ENTRADAS_DIA_42K);
        double entradasDelDia50K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_ENTRADAS_DIA_50K), CELDA_CONTROL_DE_CEMENTO_ENTRADAS_DIA_50K);
        double entradasDelDiaKilos = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_ENTRADAS_DIA_KILOS), CELDA_CONTROL_DE_CEMENTO_ENTRADAS_DIA_KILOS);

        double salidasDelDia42K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_DIA_42K), CELDA_CONTROL_DE_CEMENTO_SALIDAS_DIA_42K);
        double salidasDelDia50K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_DIA_50K), CELDA_CONTROL_DE_CEMENTO_SALIDAS_DIA_50K);
        double salidasDelDiaKilos = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_DIA_KILOS), CELDA_CONTROL_DE_CEMENTO_SALIDAS_DIA_KILOS);

        double salidasPulida42K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_PULIDA_42K), CELDA_CONTROL_DE_CEMENTO_SALIDAS_PULIDA_42K);
        double salidasPulida50K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_PULIDA_50K), CELDA_CONTROL_DE_CEMENTO_SALIDAS_PULIDA_50K);
        double salidasPulidaKilos = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_PULIDA_KILOS), CELDA_CONTROL_DE_CEMENTO_SALIDAS_PULIDA_KILOS);

        double salidasVentaOtros42K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_VENTA_OTROS_42K), CELDA_CONTROL_DE_CEMENTO_SALIDAS_VENTA_OTROS_42K);
        double salidasVentaOtros50K = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_VENTA_OTROS_50K), CELDA_CONTROL_DE_CEMENTO_SALIDAS_VENTA_OTROS_50K);
        double salidasVentaOtrosKilos = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CONTROL_DE_CEMENTO_SALIDAS_VENTA_OTROS_KILOS), CELDA_CONTROL_DE_CEMENTO_SALIDAS_VENTA_OTROS_KILOS);

        double salidasCementoKilosPulida = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_CEMENTO_PULIR_KILOS), CELDA_CEMENTO_PULIR_KILOS);

        String fechaEntradaKilos = null;
        if (entradasDelDia42K != 0 || entradasDelDia50K != 0) {
            fechaEntradaKilos = fecha;
        }

        double entradaKilos = (entradasDelDia42K * 42.5 + entradasDelDia50K * 50 + entradasDelDiaKilos);
        double salidaKilos = (salidasDelDiaKilos + salidasDelDia42K * 42.5 + salidasDelDia50K * 50) +
                (salidasPulida42K * 42.5 + salidasPulida50K * 50 + salidasPulidaKilos) +
                (salidasVentaOtros42K * 42.5 + salidasVentaOtros50K * 50 + salidasVentaOtrosKilos) - salidasCementoKilosPulida;
        double saldoFinal = (saldoInicialKilos + entradaKilos) - salidaKilos;

        return ControlCementoDTO.builder()
                .saldo(saldoFinal)
                .entradaKilos(entradaKilos)
                .fechaEntradaKilos(fechaEntradaKilos)
                .salidaKilos(salidaKilos)
                .fechaSalidaKilos(fecha)
                .numBitacora(produccionDTO.getNumBitacora())
                .build();
    }

    public PruebaDTO obtenerDatosPrueba(Sheet sheet) {

        Double valorCeldaNumeroCilindro = (Double) obtenerValorCelda(sheet, CELDA_PRUEBAS_CILINDROS_NUMERO);
        if (valorCeldaNumeroCilindro != null) {
            Double valorCeldaNumeroCochaCilindro = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_PRUEBAS_CILINDROS_NUMERO_COCHA), CELDA_PRUEBAS_CILINDROS_NUMERO_COCHA);
            String valorCeldaResponsableCilindro = (String) obtenerValorCelda(sheet, CELDA_PRUEBAS_CILINDROS_RESPONSABLE);

            validarDatosObligatorios(valorCeldaNumeroCochaCilindro, "Numero de cocha Cilindros");
            validarDatosObligatorios(valorCeldaResponsableCilindro, "Responsable Cilindros");
            return PruebaDTO.builder()
                    .numero(valorCeldaNumeroCilindro.intValue())
                    .numero_cocha(valorCeldaNumeroCochaCilindro.intValue())
                    .consecutivoBitacora(consecutivo)
                    .nombreResponsable(valorCeldaResponsableCilindro)
                    .build();
        }

        return null;
    }

    public TrasladoMezclaDTO obtenerDatosTrasladoMezcla(Sheet sheet, ProduccionDTO produccionDTO) {
        Double valorCeldaTrasladoKilos = (Double) obtenerValorCelda(sheet, CELDA_TRASLADO_MEZCLA_KILOS);
        Object valorCeldaTrasladoDeMaquina = obtenerValorCelda(sheet, CELDA_TRASLADO_MEZCLA_DE_MAQUINA_1);
        Object valorCeldaTrasladoAMaquina = obtenerValorCelda(sheet, CELDA_TRASLADO_MEZCLA_A_MAQUINA_1);
        if (valorCeldaTrasladoKilos > 0 && valorCeldaTrasladoDeMaquina != null) {
            validarDatosObligatorios(valorCeldaTrasladoDeMaquina, "De Maquina");
            validarDatosObligatorios(valorCeldaTrasladoAMaquina, "A Maquina");
            return TrasladoMezclaDTO.builder()
                    .deMaquina((String) valorCeldaTrasladoDeMaquina)
                    .aMaquina((String) valorCeldaTrasladoAMaquina)
                    .cantidadKilos(valorCeldaTrasladoKilos.intValue())
                    .numBitacora(produccionDTO.getNumBitacora())
                    .build();
        }
        return null;
    }

    public List<TiempoParadaMaquinaDTO> obtenerTiemposParadaMaquina(Sheet sheet) {
        List<TiempoParadaMaquinaDTO> listaTiemposParadaMaquina = new ArrayList<>();
        for (int i = 30; i <= 35; i++) {
            Double tipoTiempoParadaMaquina = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_TIEMPOS_PARADA_MAQUINA_TIPO + i), CELDA_TIEMPOS_PARADA_MAQUINA_TIPO + i);
            Double minutosTiempoParadaMaquina = convertirValoresCeldaADouble(obtenerValorCelda(sheet, CELDA_TIEMPOS_PARADA_MAQUINA_MINUTOS + i), CELDA_TIEMPOS_PARADA_MAQUINA_MINUTOS + i);
            if (tipoTiempoParadaMaquina > 0) {
                validarDatosObligatorios(minutosTiempoParadaMaquina, "Minutos Tiempo Parada Maquina");
                TiempoParadaMaquinaDTO tiempoParadaMaquinaDTO = TiempoParadaMaquinaDTO.builder()
                        .tipo(tipoTiempoParadaMaquina.intValue())
                        .minutos(minutosTiempoParadaMaquina.intValue())
                        .consecutivoBitacora(consecutivo)
                        .build();
                listaTiemposParadaMaquina.add(tiempoParadaMaquinaDTO);
            }
        }
        return listaTiemposParadaMaquina;
    }

    private int convertirValorCeldaAInt(Object valorCelda) {

        Double convertirValorCelda = (Double) valorCelda;
        try {
            if (valorCelda == null) {
                return 0;
            }
            return convertirValorCelda.intValue();
        } catch (NumberFormatException e) {
            throw new BpmnError("Error de Negocio", "Error al convertir el valor de la celda a entero: " + e.getMessage());
        }
    }

    private Double convertirValoresCeldaADouble(Object valor, String celda) {
        if (valor == null) {
            return 0.0;
        }
        if (!(valor instanceof Double)) {
            throw new BpmnError("Error de Negocio", "Ingrese valores validos en la celda: " + celda);
        }
        return (Double) valor;
    }

}
