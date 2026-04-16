package com.cuatrimestre.app.service.dto;

import java.io.Serializable;

/**
 * DTO para los módulos dentro de un menú.
 */
public class ModuloDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nombre;
    private String ruta;

    // --- CONSTRUCTORES ---
    public ModuloDTO() {}

    public ModuloDTO(Integer id, String nombre, String ruta) {
        this.id = id;
        this.nombre = nombre;
        this.ruta = ruta;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}
