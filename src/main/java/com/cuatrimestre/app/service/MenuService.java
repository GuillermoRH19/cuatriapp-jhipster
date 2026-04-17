package com.cuatrimestre.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cuatrimestre.app.domain.Menu;
import com.cuatrimestre.app.domain.Modulo;
import com.cuatrimestre.app.domain.Perfil;
import com.cuatrimestre.app.domain.PermisosPerfil;
import com.cuatrimestre.app.repository.MenuRepository;
import com.cuatrimestre.app.repository.PerfilRepository;
import com.cuatrimestre.app.repository.PermisosPerfilRepository;
import com.cuatrimestre.app.service.dto.MenuDTO;
import com.cuatrimestre.app.service.dto.ModuloDTO;

/**
 * Servicio para gestionar menús dinámicos basados en perfiles y permisos.
 * Adaptación de HomeService (Python) a Spring Boot.
 */
@Service
public class MenuService {

    private final Logger log = LoggerFactory.getLogger(MenuService.class);

    private final MenuRepository menuRepository;
    private final PerfilRepository perfilRepository;
    private final PermisosPerfilRepository permisosPerfilRepository;

    public MenuService(MenuRepository menuRepository, PerfilRepository perfilRepository, 
                       PermisosPerfilRepository permisosPerfilRepository) {
        this.menuRepository = menuRepository;
        this.perfilRepository = perfilRepository;
        this.permisosPerfilRepository = permisosPerfilRepository;
    }

    /**
     * Obtiene la estructura de menús y submenús basada en los permisos del perfil.
     * Si el perfil es administrador, mapea todos los módulos automáticamente.
     * 
     * @param idPerfil ID del perfil del usuario
     * @return Lista de MenuDTO estructurada jerárquicamente
     */
    @Transactional(readOnly = true)
    public List<MenuDTO> getSidebarMenu(Integer idPerfil) {
        log.debug("Cargando menú para el perfil ID: {}", idPerfil);

        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            log.error("PERMISOS - Perfil ID {} no existe en base de datos. Menú vacío.", idPerfil);
            return Collections.emptyList();
        }

        Perfil perfil = perfilOptional.orElseThrow();
        boolean esAdmin = Boolean.TRUE.equals(perfil.getAdministrador());
        log.info("PERMISOS - Perfil '{}' (id={}) esAdmin={}", perfil.getNombrePerfil(), idPerfil, esAdmin);

        // Carga todos los permisos del perfil en UNA sola query (evita N+1)
        Map<Integer, PermisosPerfil> permisosPorModulo = Collections.emptyMap();
        if (!esAdmin) {
            List<PermisosPerfil> permisos = permisosPerfilRepository.findByPerfilId(idPerfil);
            log.info("PERMISOS - Perfil '{}': {} registros en permisos_perfil cargados",
                perfil.getNombrePerfil(), permisos.size());

            if (permisos.isEmpty()) {
                log.warn("PERMISOS - Perfil '{}' (id={}) NO tiene permisos configurados en base de datos. " +
                    "El menú estará vacío. Asigna permisos en el módulo de administración.",
                    perfil.getNombrePerfil(), idPerfil);
                return Collections.emptyList();
            }

            permisosPorModulo = permisos.stream()
                .collect(Collectors.toMap(pp -> pp.getModulo().getId(), pp -> pp));
        }

        List<Menu> todosLosMenus = menuRepository.findAll();
        List<MenuDTO> resultado = new ArrayList<>();

        for (Menu menu : todosLosMenus) {
            MenuDTO menuDTO = new MenuDTO(menu.getId(), menu.getNombreMenu());
            Set<Modulo> modulos = menu.getModulos();

            if (modulos != null) {
                for (Modulo modulo : modulos) {
                    boolean tieneAcceso;

                    if (esAdmin) {
                        tieneAcceso = true;
                        log.debug("PERMISOS - Admin: acceso automático a módulo '{}' (id={})",
                            modulo.getNombreModulo(), modulo.getId());
                    } else {
                        PermisosPerfil permiso = permisosPorModulo.get(modulo.getId());
                        tieneAcceso = permiso != null && Boolean.TRUE.equals(permiso.getConsulta());
                        log.debug("PERMISOS - Módulo '{}' (id={}): permiso={}, consulta={}",
                            modulo.getNombreModulo(), modulo.getId(),
                            permiso != null ? "encontrado" : "NO encontrado",
                            permiso != null ? permiso.getConsulta() : false);
                    }

                    if (tieneAcceso) {
                        menuDTO.getSubmodulos().add(new ModuloDTO(
                            modulo.getId(), modulo.getNombreModulo(), modulo.getRuta()));
                    }
                }
            }

            if (!menuDTO.getSubmodulos().isEmpty()) {
                resultado.add(menuDTO);
            }
        }

        log.info("PERMISOS - Menú final para perfil '{}': {} secciones, {} módulos totales",
            perfil.getNombrePerfil(), resultado.size(),
            resultado.stream().mapToInt(m -> m.getSubmodulos().size()).sum());

        if (resultado.isEmpty() && !esAdmin) {
            log.warn("PERMISOS - Perfil '{}' tiene permisos en BD pero ninguno con consulta=true. " +
                "El menú está vacío. Revisa los bits de consulta en permisos_perfil.",
                perfil.getNombrePerfil());
        }

        return resultado;
    }

    /**
     * Devuelve todos los menús con todos sus módulos (sin filtrar por permisos).
     * Usado para usuarios ROLE_ADMIN sin perfil asignado.
     */
    public List<MenuDTO> getAllMenus() {
        List<Menu> todosLosMenus = menuRepository.findAll();
        List<MenuDTO> resultado = new ArrayList<>();
        for (Menu menu : todosLosMenus) {
            MenuDTO menuDTO = new MenuDTO(menu.getId(), menu.getNombreMenu());
            Set<Modulo> modulos = menu.getModulos();
            if (modulos != null) {
                for (Modulo modulo : modulos) {
                    menuDTO.getSubmodulos().add(new ModuloDTO(
                        modulo.getId(), modulo.getNombreModulo(), modulo.getRuta()
                    ));
                }
            }
            if (!menuDTO.getSubmodulos().isEmpty()) {
                resultado.add(menuDTO);
            }
        }
        return resultado;
    }

    /**
     * Obtiene todos los permisos de un perfil.
     * 
     * @param idPerfil ID del perfil
     * @return Map de permisos por módulo
     */
    public Map<String, Object> getPermisosByPerfil(Integer idPerfil) {
        log.info("[DEBUG] Obteniendo permisos para el perfil ID: {}", idPerfil);

        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            log.error("[ERROR] El perfil ID {} no existe.", idPerfil);
            return Collections.emptyMap();
        }

        Perfil perfil = perfilOptional.orElseThrow();
        Map<String, Object> resultado = new HashMap<>();

        if (Boolean.TRUE.equals(perfil.getAdministrador())) {
            resultado.put("esAdmin", true);
            resultado.put("permisos", perfil.getPermisos());
        } else {
            resultado.put("esAdmin", false);
            resultado.put("permisos", perfil.getPermisos());
        }

        return resultado;
    }
}
