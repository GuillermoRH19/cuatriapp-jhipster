package com.cuatrimestre.app.web.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cuatrimestre.app.service.PermisosService;

/**
 * REST controller para gestionar Permisos de Perfiles.
 */
@RestController
@RequestMapping("/api/permisos_perfil")
@Transactional
public class PermisosResource {

    private static final Logger log = LoggerFactory.getLogger(PermisosResource.class);

    private final PermisosService permisosService;

    public PermisosResource(PermisosService permisosService) {
        this.permisosService = permisosService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Map<String, Object>>> getPermisosByPerfil(@PathVariable Integer id) {
        log.debug("REST request to get permisos for perfil : {}", id);
        return ResponseEntity.ok().body(permisosService.getPermisosByPerfil(id));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<List<Map<String, Object>>> getPermisosByViewPerfil(@PathVariable Integer id) {
        log.debug("REST request to get permisos view for perfil : {}", id);
        return ResponseEntity.ok().body(permisosService.getPermisosByViewPerfil(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> updatePermiso(@RequestBody Map<String, Object> data) {
        log.debug("REST request to update permiso : {}", data);
        Map<String, Object> resultado = permisosService.updatePermiso(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> updatePermisosBulk(@RequestBody Map<String, Object> data) {
        log.debug("REST request bulk update permisos for perfil : {}", data.get("idPerfil"));
        try {
            Integer idPerfil = Integer.parseInt(data.get("idPerfil").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> listaPermisos = (List<Map<String, Object>>) data.get("permisos");

            if (listaPermisos == null || listaPermisos.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "msg", "Lista de permisos vacía"));
            }

            int successCount = 0;
            for (Map<String, Object> perm : listaPermisos) {
                perm.put("idPerfil", idPerfil);
                Map<String, Object> resultado = permisosService.updatePermiso(perm);
                if ((Boolean) resultado.get("success")) {
                    successCount++;
                }
            }

            log.debug("Bulk update: {} de {} permisos actualizados", successCount, listaPermisos.size());
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "msg", "Se actualizaron " + successCount + " módulos con éxito"
            ));

        } catch (Exception e) {
            log.error("Error en bulk update de permisos: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "msg", "Error: " + e.getMessage()));
        }
    }
}
