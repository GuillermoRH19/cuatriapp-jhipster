package com.cuatrimestre.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public List<MenuDTO> getSidebarMenu(Integer idPerfil) {
        log.debug("Cargando menú para el perfil ID: {}", idPerfil);

        Optional<Perfil> perfilOptional = perfilRepository.findById(idPerfil);
        if (perfilOptional.isEmpty()) {
            log.error("El perfil ID {} no existe en la base de datos.", idPerfil);
            return Collections.emptyList();
        }

        Perfil perfil = perfilOptional.get();
        boolean esAdmin = Boolean.TRUE.equals(perfil.getAdministrador());

        log.debug("Perfil: {} - ¿Es administrador? {}", perfil.getNombrePerfil(), esAdmin);

        List<Menu> todosLosMenus = menuRepository.findAll();

        List<MenuDTO> resultado = new ArrayList<>();

        for (Menu menu : todosLosMenus) {
            MenuDTO menuDTO = new MenuDTO(menu.getId(), menu.getNombreMenu());
            Set<Modulo> modulos = menu.getModulos();

            if (modulos != null) {
                for (Modulo modulo : modulos) {
                    boolean tieneAcceso = false;

                    if (esAdmin) {
                        log.debug("Admin '{}': acceso automático al módulo '{}'", perfil.getNombrePerfil(), modulo.getNombreModulo());
                        tieneAcceso = true;
                    } else {
                        Optional<PermisosPerfil> permiso = permisosPerfilRepository.findByModuloIdAndPerfilId(
                            modulo.getId(), idPerfil);
                        if (permiso.isPresent() && Boolean.TRUE.equals(permiso.get().getConsulta())) {
                            log.debug("Perfil '{}': permiso consulta en módulo '{}'", perfil.getNombrePerfil(), modulo.getNombreModulo());
                            tieneAcceso = true;
                        }
                    }

                    if (tieneAcceso) {
                        menuDTO.getSubmodulos().add(new ModuloDTO(
                            modulo.getId(),
                            modulo.getNombreModulo(),
                            modulo.getRuta()
                        ));
                    }
                }
            }

            if (!menuDTO.getSubmodulos().isEmpty()) {
                resultado.add(menuDTO);
            }
        }

        log.debug("Menú generado: {} secciones para perfil {}", resultado.size(), perfil.getNombrePerfil());
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

        Perfil perfil = perfilOptional.get();
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
