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
 * Entidad Perfil - Define los perfiles de usuario.
 */
@Entity
@Table(name = "perfil")
public class Perfil implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_perfil", nullable = false, length = 100)
    private String nombrePerfil;

    @Column(name = "bit_administrador", nullable = false)
    private Boolean administrador = false;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PermisosPerfil> permisos = new HashSet<>();

    // --- CONSTRUCTORES ---
    public Perfil() {}

    public Perfil(String nombrePerfil, Boolean administrador) {
        this.nombrePerfil = nombrePerfil;
        this.administrador = administrador;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombrePerfil() {
        return nombrePerfil;
    }

    public void setNombrePerfil(String nombrePerfil) {
        this.nombrePerfil = nombrePerfil;
    }

    public Boolean getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Boolean administrador) {
        this.administrador = administrador;
    }

    public Set<PermisosPerfil> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<PermisosPerfil> permisos) {
        this.permisos = permisos;
    }

    @Override
    public String toString() {
        return "Perfil{" +
                "id=" + id +
                ", nombrePerfil='" + nombrePerfil + '\'' +
                ", administrador=" + administrador +
                '}';
    }
}
