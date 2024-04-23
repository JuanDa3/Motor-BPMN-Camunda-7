package com.camunda.process.engine.util;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtil {
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String DIRECCION_PERSISTENCIA = "http://localhost:8081/api/produccion/";

    public static HttpResponse<String> get(String uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DIRECCION_PERSISTENCIA+uri))
                .header("Content-Type", "application/json")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> post(String uri, Object body) throws Exception {

        Gson gson = new Gson();
        String jsonBody = gson.toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DIRECCION_PERSISTENCIA+uri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> put(String uri, Object body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .PUT(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
