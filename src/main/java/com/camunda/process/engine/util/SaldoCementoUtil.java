package com.camunda.process.engine.util;

import com.camunda.process.engine.dto.SaldoCementoDTO;
import com.google.gson.Gson;

import java.net.http.HttpResponse;

public class SaldoCementoUtil {

    public static int obtenerSaldo() {
        try {
            HttpResponse<String> response = HttpUtil.get("http://localhost:8081/api/produccion/control-cemento/saldo");
            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                SaldoCementoDTO saldoCementoDTO = gson.fromJson(response.body(), SaldoCementoDTO.class);
                return saldoCementoDTO.getSaldo();
            } else {
                throw new RuntimeException("Error al obtener el saldo. Código de estado: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
