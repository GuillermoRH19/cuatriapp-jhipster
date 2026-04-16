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
 * Entidad PermisosPerfil - Mapea permisos de módulos a perfiles.
 */
@Entity
@Table(name = "permisos_perfil")
public class PermisosPerfil implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modulo", nullable = false)
    @JsonIgnoreProperties("permisos")
    private Modulo modulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perfil", nullable = false)
    @JsonIgnoreProperties("permisos")
    private Perfil perfil;

    @Column(name = "bit_agregar", nullable = false)
    private Boolean agregar = false;

    @Column(name = "bit_editar", nullable = false)
    private Boolean editar = false;

    @Column(name = "bit_eliminar", nullable = false)
    private Boolean eliminar = false;

    @Column(name = "bit_consulta", nullable = false)
    private Boolean consulta = false;

    @Column(name = "bit_detalle", nullable = false)
    private Boolean detalle = false;

    // --- CONSTRUCTORES ---
    public PermisosPerfil() {}

    public PermisosPerfil(Modulo modulo, Perfil perfil, Boolean agregar, Boolean editar, 
                          Boolean eliminar, Boolean consulta, Boolean detalle) {
        this.modulo = modulo;
        this.perfil = perfil;
        this.agregar = agregar;
        this.editar = editar;
        this.eliminar = eliminar;
        this.consulta = consulta;
        this.detalle = detalle;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Boolean getAgregar() {
        return agregar;
    }

    public void setAgregar(Boolean agregar) {
        this.agregar = agregar;
    }

    public Boolean getEditar() {
        return editar;
    }

    public void setEditar(Boolean editar) {
        this.editar = editar;
    }

    public Boolean getEliminar() {
        return eliminar;
    }

    public void setEliminar(Boolean eliminar) {
        this.eliminar = eliminar;
    }

    public Boolean getConsulta() {
        return consulta;
    }

    public void setConsulta(Boolean consulta) {
        this.consulta = consulta;
    }

    public Boolean getDetalle() {
        return detalle;
    }

    public void setDetalle(Boolean detalle) {
        this.detalle = detalle;
    }

    @Override
    public String toString() {
        return "PermisosPerfil{" +
                "id=" + id +
                ", agregar=" + agregar +
                ", editar=" + editar +
                ", eliminar=" + eliminar +
                ", consulta=" + consulta +
                ", detalle=" + detalle +
                '}';
    }
}
