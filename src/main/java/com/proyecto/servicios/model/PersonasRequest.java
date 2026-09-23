package com.proyecto.servicios.model;

public class PersonasRequest {

    private String nombre;
    private String apellidoP;
    private String apellidoMaterno;

    public PersonasRequest() {
    }

    public PersonasRequest(String nombre, String apellidoP, String apellidoMaterno) {
        this.nombre = nombre;
        this.apellidoP = apellidoP;
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoP() {
        return apellidoP;
    }

    public void setApellidoP(String apellidoP) {
        this.apellidoP = apellidoP;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }
}
