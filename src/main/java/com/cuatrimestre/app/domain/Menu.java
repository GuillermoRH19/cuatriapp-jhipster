package com.cuatrimestre.app.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidad Menu - Agrupa módulos por categoría.
 */
@Entity
@Table(name = "menu")
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_menu", nullable = false, length = 100)
    private String nombreMenu;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Modulo> modulos = new HashSet<>();

    // --- CONSTRUCTORES ---
    public Menu() {}

    public Menu(String nombreMenu) {
        this.nombreMenu = nombreMenu;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreMenu() {
        return nombreMenu;
    }

    public void setNombreMenu(String nombreMenu) {
        this.nombreMenu = nombreMenu;
    }

    public Set<Modulo> getModulos() {
        return modulos;
    }

    public void setModulos(Set<Modulo> modulos) {
        this.modulos = modulos;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "id=" + id +
                ", nombreMenu='" + nombreMenu + '\'' +
                '}';
    }
}
