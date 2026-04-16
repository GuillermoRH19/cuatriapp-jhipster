package com.cuatrimestre.app.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad Modulo - Items del menú con rutas.
 */
@Entity
@Table(name = "modulo")
public class Modulo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_modulo", nullable = false, length = 100)
    private String nombreModulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_menu", nullable = false)
    @JsonIgnoreProperties("modulos")
    private Menu menu;

    @Column(name = "ruta", length = 255)
    private String ruta;

    // --- CONSTRUCTORES ---
    public Modulo() {}

    public Modulo(String nombreModulo, Menu menu, String ruta) {
        this.nombreModulo = nombreModulo;
        this.menu = menu;
        this.ruta = ruta;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreModulo() {
        return nombreModulo;
    }

    public void setNombreModulo(String nombreModulo) {
        this.nombreModulo = nombreModulo;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    @Override
    public String toString() {
        return "Modulo{" +
                "id=" + id +
                ", nombreModulo='" + nombreModulo + '\'' +
                ", ruta='" + ruta + '\'' +
                '}';
    }
}
