package com.cuatrimestre.app.web.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cuatrimestre.app.security.SecurityUtils;
import com.cuatrimestre.app.service.MenuService;
import com.cuatrimestre.app.service.UserService;
import com.cuatrimestre.app.service.dto.MenuDTO;
import com.cuatrimestre.app.security.AuthoritiesConstants;

/**
 * REST controller para gestionar menús dinámicos.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MenuResource {

    private static final Logger log = LoggerFactory.getLogger(MenuResource.class);

    private final MenuService menuService;
    private final UserService userService;

    public MenuResource(MenuService menuService, UserService userService) {
        this.menuService = menuService;
        this.userService = userService;
    }

    /**
     * GET /menus/sidebar : Obtiene el menú dinámico del usuario autenticado según su Perfil.
     *
     * @return Lista de MenuDTO con la estructura jerárquica
     */
    @GetMapping("/menus/sidebar")
    public ResponseEntity<List<MenuDTO>> getSidebarMenu() {
        log.debug("REST request to get sidebar menu for current user");

        return userService.getUserWithAuthorities()
            .map(user -> {
                List<MenuDTO> menuStructure;
                if (user.getPerfil() == null) {
                    if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
                        log.debug("Admin user {} has no perfil, returning all menus", user.getLogin());
                        menuStructure = menuService.getAllMenus();
                    } else {
                        log.warn("User {} has no perfil assigned, returning empty menu", user.getLogin());
                        menuStructure = java.util.Collections.emptyList();
                    }
                } else {
                    Integer idPerfil = user.getPerfil().getId();
                    log.debug("Loading menu for user {} with perfil id {}", user.getLogin(), idPerfil);
                    menuStructure = menuService.getSidebarMenu(idPerfil);
                }
                return ResponseEntity.ok(menuStructure);
            })
            .orElseGet(() -> {
                log.warn("No authenticated user found");
                return ResponseEntity.badRequest().build();
            });
    }

    /**
     * GET /menus/sidebar/{perfilId} : Obtiene el menú para un perfil específico (admin).
     * 
     * @param perfilId ID del perfil
     * @return Lista de MenuDTO
     */
    @GetMapping("/menus/sidebar/{perfilId}")
    public ResponseEntity<List<MenuDTO>> getSidebarMenuByPerfil(@PathVariable Integer perfilId) {
        log.debug("REST request to get sidebar menu for perfil: {}", perfilId);
        
        List<MenuDTO> menuStructure = menuService.getSidebarMenu(perfilId);
        return ResponseEntity.ok().body(menuStructure);
    }
}
