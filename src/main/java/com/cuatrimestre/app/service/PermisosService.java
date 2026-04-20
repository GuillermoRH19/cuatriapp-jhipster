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

import com.cuatrimestre.app.domain.Menu;
import com.cuatrimestre.app.domain.Modulo;
import com.cuatrimestre.app.domain.Perfil;
import com.cuatrimestre.app.domain.PermisosPerfil;
import com.cuatrimestre.app.repository.MenuRepository;
import com.cuatrimestre.app.repository.ModuloRepository;
import com.cuatrimestre.app.repository.PerfilRepository;
import com.cuatrimestre.app.repository.PermisosPerfilRepository;

/**
 * Servicio para gestionar Permisos de Perfiles.
 */
@Service
public class PermisosService {

    private static final Logger log = LoggerFactory.getLogger(PermisosService.class);

    private final PermisosPerfilRepository permisosPerfilRepository;
    private final PerfilRepository perfilRepository;
    private final ModuloRepository moduloRepository;
    private final MenuRepository menuRepository;

    public PermisosService(PermisosPerfilRepository permisosPerfilRepository,
                          PerfilRepository perfilRepository,
                          ModuloRepository moduloRepository,
                          MenuRepository menuRepository) {
        this.permisosPerfilRepository = permisosPerfilRepository;
        this.perfilRepository = perfilRepository;
        this.moduloRepository = moduloRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public void asegurarModulosAdminExistentes() {
        log.debug("PERMISOS - Verificando existencia de módulos administrativos...");
        
        Menu adminMenu = menuRepository.findByNombreMenu("Administración")
            .orElseGet(() -> {
                Menu m = new Menu();
                m.setNombreMenu("Administración");
                return menuRepository.save(m);
            });

        registrarModuloSiFalta("Usuarios y Roles", "/dashboard/admin/user-management", adminMenu);
        registrarModuloSiFalta("Perfiles", "/dashboard/admin/perfil", adminMenu);
        registrarModuloSiFalta("Módulos", "/dashboard/admin/modulo", adminMenu);
        registrarModuloSiFalta("Permisos Perfil", "/dashboard/admin/permisos-perfil", adminMenu);
    }

    private void registrarModuloSiFalta(String nombre, String ruta, Menu menu) {
        boolean existe = moduloRepository.findAll().stream()
            .anyMatch(m -> m.getNombreModulo().equals(nombre) && m.getRuta().equals(ruta));
        
        if (!existe) {
            Modulo m = new Modulo();
            m.setNombreModulo(nombre);
            m.setRuta(ruta);
            m.setMenu(menu);
            moduloRepository.save(m);
            log.info("PERMISOS - Módulo registrado automáticamente: {}", nombre);
        }
    }

    @Transactional
    public List<Map<String, Object>> getPermisosByPerfil(Integer idPerfil) {
        asegurarModulosAdminExistentes();
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
            Map<Integer, PermisosPerfil> permisosPorModulo = permisosPerfilRepository
                .findByPerfilId(idPerfil).stream()
                .collect(Collectors.toMap(pp -> pp.getModulo().getId(), pp -> pp));

            for (Modulo modulo : todosModulos) {
                PermisosPerfil p = permisosPorModulo.get(modulo.getId());
                if (p != null) {
                    permisosList.add(buildPermiso(modulo.getId(), modulo.getNombreModulo(), p.getId(),
                        bit(p.getAgregar()), bit(p.getEditar()), bit(p.getEliminar()),
                        bit(p.getConsulta()), bit(p.getDetalle())));
                } else {
                    permisosList.add(buildPermiso(modulo.getId(), modulo.getNombreModulo(), null, 0, 0, 0, 0, 0));
                }
            }
        }

        return permisosList;
    }

    @Transactional
    public List<Map<String, Object>> getPermisosByViewPerfil(Integer idPerfil) {
        asegurarModulosAdminExistentes();
        log.debug("PERMISOS - Vista completa de permisos para perfil ID: {}", idPerfil);

        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            log.error("PERMISOS - Perfil ID {} no existe en getPermisosByViewPerfil.", idPerfil);
            return new ArrayList<>();
        }

        Perfil perfil = perfilOptional.orElseThrow();
        List<Modulo> todosModulos = moduloRepository.findAll();

        Map<Integer, PermisosPerfil> permisosPorModulo = permisosPerfilRepository
            .findByPerfilId(idPerfil).stream()
            .collect(Collectors.toMap(pp -> pp.getModulo().getId(), pp -> pp));

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
            } else {
                Optional<Modulo> moduloOpt = moduloRepository.findById(idModulo);
                Optional<Perfil> perfilOpt = perfilRepository.findById(idPerfil);

                if (moduloOpt.isEmpty() || perfilOpt.isEmpty()) {
                    return Map.of("success", false, "msg", "Módulo o Perfil no encontrado");
                }

                PermisosPerfil permiso = new PermisosPerfil(
                    moduloOpt.orElseThrow(), perfilOpt.orElseThrow(), agregar, editar, eliminar, consulta, detalle
                );
                permisosPerfilRepository.save(permiso);
            }

            return Map.of("success", true, "msg", "Permiso guardado correctamente");

        } catch (DataIntegrityViolationException e) {
            return Map.of("success", false, "msg", "El permiso para este módulo y perfil ya existe");
        } catch (Exception e) {
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
