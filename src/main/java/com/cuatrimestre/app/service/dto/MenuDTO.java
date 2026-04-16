package com.cuatrimestre.app.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la respuesta del menú dinámico.
 * Representa un menú con sus módulos (submenús).
 */
public class MenuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String titulo;
    private String idHtml;
    private List<ModuloDTO> submodulos;

    // --- CONSTRUCTORES ---
    public MenuDTO() {
        this.submodulos = new ArrayList<>();
    }

    public MenuDTO(Integer id, String titulo) {
        this.id = id;
        this.titulo = titulo;
        this.idHtml = "menu_" + id;
        this.submodulos = new ArrayList<>();
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIdHtml() {
        return idHtml;
    }

    public void setIdHtml(String idHtml) {
        this.idHtml = idHtml;
    }

    public List<ModuloDTO> getSubmodulos() {
        return submodulos;
    }

    public void setSubmodulos(List<ModuloDTO> submodulos) {
        this.submodulos = submodulos;
    }
}
