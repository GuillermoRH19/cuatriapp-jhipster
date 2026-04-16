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

/**
 * REST controller para gestionar menús dinámicos.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MenuResource {

    private final Logger log = LoggerFactory.getLogger(MenuResource.class);

    private final MenuService menuService;
    private final UserService userService;

    public MenuResource(MenuService menuService, UserService userService) {
        this.menuService = menuService;
        this.userService = userService;
    }

    /**
     * GET /menus/sidebar : Obtiene el menú dinámico based on user perfil.
     * 
     * @return Lista de MenuDTO con la estructura jerárquica
     */
    @GetMapping("/menus/sidebar")
    public ResponseEntity<List<MenuDTO>> getSidebarMenu() {
        log.debug("REST request to get sidebar menu for current user");
        
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        
        if (currentUserLogin.isEmpty()) {
            log.warn("User is not authenticated");
            return ResponseEntity.badRequest().build();
        }

        // TODO: La lógica actual de JHipster usa Authority, no Perfil.
        // Aquí asumo idPerfil = 1 como admin por defecto.
        // Debes mapear el usuario actual a un Perfil desde la BD.
        Integer idPerfil = 1; // CAMBIAR: Obtener dinámicamente desde usuario
        
        List<MenuDTO> menuStructure = menuService.getSidebarMenu(idPerfil);
        return ResponseEntity.ok().body(menuStructure);
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
