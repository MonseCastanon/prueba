package com.proyecto.servicios.model;

public class PersonaResponse extends GenericResponse {

    private String nombre;
    private String apellidoP;
    private String apellidoMaterno;

    public PersonaResponse() {
        super();
    }

    public PersonaResponse(String nombre, String apellidoP, String apellidoMaterno) {
        super();
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
