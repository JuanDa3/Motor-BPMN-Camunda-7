package com.camunda.process.engine.servicio;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecibirInformacionPersistencia {

    @GetMapping("/saldo-cemento")
    public int recibirDatos(){
        System.out.println("entra");
        return 5; // Devuelve algún valor para ProyectoPersistencia
    }
}

