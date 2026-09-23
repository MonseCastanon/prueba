package com.proyecto.servicios.model;

public class EliminaPersonaRequest {

    private String nombre;

    public EliminaPersonaRequest() {
    }

    public EliminaPersonaRequest(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
