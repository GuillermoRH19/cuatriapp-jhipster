package com.cuatrimestre.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Logger log = LoggerFactory.getLogger(PermisosService.class);

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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPermisosByPerfil(Integer idPerfil) {
        log.debug("PERMISOS - Cargando permisos para perfil ID: {}", idPerfil);

        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            log.error("PERMISOS - Perfil ID {} no existe.", idPerfil);
            return new ArrayList<>();
        }

        Perfil perfil = perfilOptional.orElseThrow();
        boolean esAdmin = Boolean.TRUE.equals(perfil.getAdministrador());
        List<Modulo> todosModulos = moduloRepository.findAll();
        List<Map<String, Object>> permisosList = new ArrayList<>();

        if (esAdmin) {
            log.info("PERMISOS - Perfil '{}' es administrador: todos los permisos en 1", perfil.getNombrePerfil());
            for (Modulo modulo : todosModulos) {
                permisosList.add(buildPermiso(modulo.getId(), modulo.getNombreModulo(), null, 1, 1, 1, 1, 1));
            }
        } else {
            // Una sola query para todos los permisos del perfil
            Map<Integer, PermisosPerfil> permisosPorModulo = permisosPerfilRepository
                .findByPerfilId(idPerfil).stream()
                .collect(Collectors.toMap(pp -> pp.getModulo().getId(), pp -> pp));

            log.info("PERMISOS - Perfil '{}': {} permisos configurados de {} módulos disponibles",
                perfil.getNombrePerfil(), permisosPorModulo.size(), todosModulos.size());

            if (permisosPorModulo.isEmpty()) {
                log.warn("PERMISOS - Perfil '{}' (id={}) NO tiene permisos configurados en BD.",
                    perfil.getNombrePerfil(), idPerfil);
            }

            for (Modulo modulo : todosModulos) {
                PermisosPerfil p = permisosPorModulo.get(modulo.getId());
                if (p != null) {
                    log.debug("PERMISOS - Módulo '{}': agregar={} editar={} eliminar={} consulta={} detalle={}",
                        modulo.getNombreModulo(), p.getAgregar(), p.getEditar(),
                        p.getEliminar(), p.getConsulta(), p.getDetalle());
                    permisosList.add(buildPermiso(modulo.getId(), modulo.getNombreModulo(), p.getId(),
                        bit(p.getAgregar()), bit(p.getEditar()), bit(p.getEliminar()),
                        bit(p.getConsulta()), bit(p.getDetalle())));
                } else {
                    log.debug("PERMISOS - Módulo '{}' sin permiso asignado para perfil '{}'",
                        modulo.getNombreModulo(), perfil.getNombrePerfil());
                    permisosList.add(buildPermiso(modulo.getId(), modulo.getNombreModulo(), null, 0, 0, 0, 0, 0));
                }
            }
        }

        return permisosList;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPermisosByViewPerfil(Integer idPerfil) {
        log.debug("PERMISOS - Vista completa de permisos para perfil ID: {}", idPerfil);

        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            log.error("PERMISOS - Perfil ID {} no existe en getPermisosByViewPerfil.", idPerfil);
            return new ArrayList<>();
        }

        Perfil perfil = perfilOptional.orElseThrow();
        List<Modulo> todosModulos = moduloRepository.findAll();

        // Una sola query para todos los permisos del perfil
        Map<Integer, PermisosPerfil> permisosPorModulo = permisosPerfilRepository
            .findByPerfilId(idPerfil).stream()
            .collect(Collectors.toMap(pp -> pp.getModulo().getId(), pp -> pp));

        log.info("PERMISOS - ViewPerfil '{}': {} permisos en BD, {} módulos totales",
            perfil.getNombrePerfil(), permisosPorModulo.size(), todosModulos.size());

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Modulo modulo : todosModulos) {
            PermisosPerfil p = permisosPorModulo.get(modulo.getId());
            if (p != null) {
                resultado.add(buildPermiso(modulo.getId(), modulo.getNombreModulo(), p.getId(),
                    bit(p.getAgregar()), bit(p.getEditar()), bit(p.getEliminar()),
                    bit(p.getConsulta()), bit(p.getDetalle())));
            } else {
                resultado.add(buildPermiso(modulo.getId(), modulo.getNombreModulo(), null, 0, 0, 0, 0, 0));
            }
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
    @Transactional
    public Map<String, Object> updatePermiso(Map<String, Object> data) {
        try {
            Integer idModulo = Integer.parseInt(data.get("idModulo").toString());
            Integer idPerfil = Integer.parseInt(data.get("idPerfil").toString());

            Boolean agregar = convertToBit(data.get("bitAgregar"));
            Boolean editar = convertToBit(data.get("bitEditar"));
            Boolean eliminar = convertToBit(data.get("bitEliminar"));
            Boolean consulta = convertToBit(data.get("bitConsulta"));
            Boolean detalle = convertToBit(data.get("bitDetalle"));

            Optional<PermisosPerfil> existente = permisosPerfilRepository
                .findByModuloIdAndPerfilId(idModulo, idPerfil);

            if (existente.isPresent()) {
                PermisosPerfil permiso = existente.orElseThrow();
                permiso.setAgregar(agregar);
                permiso.setEditar(editar);
                permiso.setEliminar(eliminar);
                permiso.setConsulta(consulta);
                permiso.setDetalle(detalle);
                permisosPerfilRepository.save(permiso);
                log.info("PERMISOS - Actualizado permiso módulo={} perfil={}", idModulo, idPerfil);
            } else {
                Optional<Modulo> moduloOpt = moduloRepository.findById(idModulo);
                Optional<Perfil> perfilOpt = perfilRepository.findById(idPerfil);

                if (moduloOpt.isEmpty() || perfilOpt.isEmpty()) {
                    log.error("PERMISOS - Módulo {} o Perfil {} no encontrado al crear permiso", idModulo, idPerfil);
                    return Map.of("success", false, "msg", "Módulo o Perfil no encontrado");
                }

                PermisosPerfil permiso = new PermisosPerfil(
                    moduloOpt.orElseThrow(), perfilOpt.orElseThrow(), agregar, editar, eliminar, consulta, detalle
                );
                permisosPerfilRepository.save(permiso);
                log.info("PERMISOS - Creado permiso módulo={} perfil={}", idModulo, idPerfil);
            }

            return Map.of("success", true, "msg", "Permiso guardado correctamente");

        } catch (DataIntegrityViolationException e) {
            log.error("PERMISOS - Conflicto de integridad al guardar permiso: {}", e.getMessage());
            return Map.of("success", false, "msg", "El permiso para este módulo y perfil ya existe");
        } catch (Exception e) {
            log.error("PERMISOS - Error al guardar permiso: {}", e.getMessage());
            return Map.of("success", false, "msg", e.getMessage());
        }
    }

    private Boolean convertToBit(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String strVal = value.toString();
        return "1".equals(strVal) || "true".equalsIgnoreCase(strVal) || "on".equalsIgnoreCase(strVal);
    }

    private int bit(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private Map<String, Object> buildPermiso(Integer idModulo, String nombreModulo, Integer idPermiso,
                                              int agregar, int editar, int eliminar, int consulta, int detalle) {
        Map<String, Object> perm = new HashMap<>();
        perm.put("idModulo", idModulo);
        perm.put("strNombreModulo", nombreModulo);
        perm.put("idPermiso", idPermiso);
        perm.put("bitAgregar", agregar);
        perm.put("bitEditar", editar);
        perm.put("bitEliminar", eliminar);
        perm.put("bitConsulta", consulta);
        perm.put("bitDetalle", detalle);
        return perm;
    }
}
