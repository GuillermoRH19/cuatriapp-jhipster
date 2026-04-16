package com.cuatrimestre.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cuatrimestre.app.domain.Modulo;
import com.cuatrimestre.app.domain.Perfil;
import com.cuatrimestre.app.domain.PermisosPerfil;
import com.cuatrimestre.app.repository.ModuloRepository;
import com.cuatrimestre.app.repository.PerfilRepository;
import com.cuatrimestre.app.repository.PermisosPerfilRepository;

/**
 * Servicio para gestionar Permisos de Perfiles.
 * Traducción directa de PermisosService (Python).
 * 
 * Lógica:
 * - get_permisos_by_perfil(id): Retorna permisos del perfil (admin=all, user=solo con bitConsulta=1)
 * - get_permisos_by_viewperfil(id): LEFT JOIN de todos los módulos + sus permisos
 * - update_permiso(data): UPSERT (INSERT si no existe, UPDATE si existe)
 */
@Service
public class PermisosService {

    private final PermisosPerfilRepository permisosPerfilRepository;
    private final PerfilRepository perfilRepository;
    private final ModuloRepository moduloRepository;

    public PermisosService(PermisosPerfilRepository permisosPerfilRepository,
                          PerfilRepository perfilRepository,
                          ModuloRepository moduloRepository) {
        this.permisosPerfilRepository = permisosPerfilRepository;
        this.perfilRepository = perfilRepository;
        this.moduloRepository = moduloRepository;
    }

    /**
     * Obtiene permisos del perfil.
     * Traducción: get_permisos_by_perfil()
     * 
     * Si es Admin: retorna todos los módulos con permisos en 1
     * Si es usuario: retorna solo módulos con bitConsulta=1
     */
    public List<Map<String, Object>> getPermisosByPerfil(Integer idPerfil) {
        System.out.println("[DEBUG] Obteniendo permisos para el perfil ID: " + idPerfil);
        List<Map<String, Object>> permisosList = new ArrayList<>();

        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            System.out.println("[ERROR] El perfil ID " + idPerfil + " no existe.");
            return permisosList;
        }

        Perfil perfil = perfilOptional.get();
        boolean esAdmin = Boolean.TRUE.equals(perfil.getAdministrador());

        if (esAdmin) {
            // ADMIN: Todos los módulos con permisos en 1
            System.out.println("[DEBUG] Es Super Administrador. Asignando todos los permisos...");
            List<Modulo> todosModulos = moduloRepository.findAll();
            
            for (Modulo modulo : todosModulos) {
                Map<String, Object> perm = new HashMap<>();
                perm.put("idModulo", modulo.getId());
                perm.put("strNombreModulo", modulo.getNombreModulo());
                perm.put("idPermiso", null); // No hay ID real, es asignación dinámica
                perm.put("bitAgregar", 1);
                perm.put("bitEditar", 1);
                perm.put("bitEliminar", 1);
                perm.put("bitConsulta", 1);
                perm.put("bitDetalle", 1);
                permisosList.add(perm);
            }
        } else {
            // USUARIO NORMAL: LEFT JOIN con PermisosPerfil
            System.out.println("[DEBUG] No es Super Admin. Obteniendo permisos específicos...");
            List<Modulo> todosModulos = moduloRepository.findAll();

            for (Modulo modulo : todosModulos) {
                Optional<PermisosPerfil> permisoOpt = permisosPerfilRepository
                    .findByModuloIdAndPerfilId(modulo.getId(), idPerfil);

                Map<String, Object> perm = new HashMap<>();
                perm.put("idModulo", modulo.getId());
                perm.put("strNombreModulo", modulo.getNombreModulo());

                if (permisoOpt.isPresent()) {
                    PermisosPerfil p = permisoOpt.get();
                    perm.put("idPermiso", p.getId());
                    perm.put("bitAgregar", p.getAgregar() ? 1 : 0);
                    perm.put("bitEditar", p.getEditar() ? 1 : 0);
                    perm.put("bitEliminar", p.getEliminar() ? 1 : 0);
                    perm.put("bitConsulta", p.getConsulta() ? 1 : 0);
                    perm.put("bitDetalle", p.getDetalle() ? 1 : 0);
                } else {
                    // NULL = sin permisos
                    perm.put("idPermiso", null);
                    perm.put("bitAgregar", 0);
                    perm.put("bitEditar", 0);
                    perm.put("bitEliminar", 0);
                    perm.put("bitConsulta", 0);
                    perm.put("bitDetalle", 0);
                }
                permisosList.add(perm);
            }
        }

        return permisosList;
    }

    /**
     * Obtiene todos los módulos con sus permisos para un perfil.
     * Traducción: get_permisos_by_viewperfil()
     * Simula LEFT JOIN: todos los módulos + sus permisos
     */
    public List<Map<String, Object>> getPermisosByViewPerfil(Integer idPerfil) {
        System.out.println("[DEBUG] Obteniendo vista de permisos para perfil ID: " + idPerfil);
        
        // Validar que el perfil existe
        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        List<Modulo> todosModulos = moduloRepository.findAll();

        for (Modulo modulo : todosModulos) {
            Optional<PermisosPerfil> permisoOpt = permisosPerfilRepository
                .findByModuloIdAndPerfilId(modulo.getId(), idPerfil);

            Map<String, Object> row = new HashMap<>();
            row.put("idModulo", modulo.getId());
            row.put("strNombreModulo", modulo.getNombreModulo());

            if (permisoOpt.isPresent()) {
                PermisosPerfil p = permisoOpt.get();
                row.put("idPermiso", p.getId());
                row.put("bitAgregar", p.getAgregar() ? 1 : 0);
                row.put("bitEditar", p.getEditar() ? 1 : 0);
                row.put("bitEliminar", p.getEliminar() ? 1 : 0);
                row.put("bitConsulta", p.getConsulta() ? 1 : 0);
                row.put("bitDetalle", p.getDetalle() ? 1 : 0);
            } else {
                row.put("idPermiso", null);
                row.put("bitAgregar", 0);
                row.put("bitEditar", 0);
                row.put("bitEliminar", 0);
                row.put("bitConsulta", 0);
                row.put("bitDetalle", 0);
            }
            resultado.add(row);
        }

        return resultado;
    }

    /**
     * UPSERT: Actualiza o inserta un permiso.
     * Traducción: update_permiso()
     * 
     * Data debe contener:
     * - idModulo, idPerfil, bitAgregar, bitEditar, bitEliminar, bitConsulta, bitDetalle
     */
    public Map<String, Object> updatePermiso(Map<String, Object> data) {
        try {
            System.out.println("[DEBUG] Actualizando permiso...");
            
            Integer idModulo = Integer.parseInt(data.get("idModulo").toString());
            Integer idPerfil = Integer.parseInt(data.get("idPerfil").toString());

            // Convertir bits
            Boolean agregar = convertToBit(data.get("bitAgregar"));
            Boolean editar = convertToBit(data.get("bitEditar"));
            Boolean eliminar = convertToBit(data.get("bitEliminar"));
            Boolean consulta = convertToBit(data.get("bitConsulta"));
            Boolean detalle = convertToBit(data.get("bitDetalle"));

            Optional<PermisosPerfil> existente = permisosPerfilRepository
                .findByModuloIdAndPerfilId(idModulo, idPerfil);

            if (existente.isPresent()) {
                // UPDATE
                PermisosPerfil permiso = existente.get();
                permiso.setAgregar(agregar);
                permiso.setEditar(editar);
                permiso.setEliminar(eliminar);
                permiso.setConsulta(consulta);
                permiso.setDetalle(detalle);
                permisosPerfilRepository.save(permiso);
                System.out.println("[DEBUG] Permiso actualizado");
            } else {
                // INSERT
                Optional<Modulo> moduloOpt = moduloRepository.findById(idModulo);
                Optional<Perfil> perfilOpt = perfilRepository.findById(idPerfil);

                if (moduloOpt.isEmpty() || perfilOpt.isEmpty()) {
                    return Map.of("success", false, "msg", "Módulo o Perfil no encontrado");
                }

                PermisosPerfil permiso = new PermisosPerfil(
                    moduloOpt.get(), perfilOpt.get(), agregar, editar, eliminar, consulta, detalle
                );
                permisosPerfilRepository.save(permiso);
                System.out.println("[DEBUG] Permiso creado");
            }

            return Map.of("success", true, "msg", "Permiso guardado correctamente");

        } catch (Exception e) {
            System.out.println("[ERROR] Error al guardar permiso: " + e.getMessage());
            return Map.of("success", false, "msg", e.getMessage());
        }
    }

    /**
     * Helper: Convierte valores a Boolean (0/1/true/false)
     */
    private Boolean convertToBit(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String strVal = value.toString();
        return "1".equals(strVal) || "true".equalsIgnoreCase(strVal) || "on".equalsIgnoreCase(strVal);
    }
}
