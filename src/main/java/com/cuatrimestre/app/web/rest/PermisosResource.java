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
 * Traducción directa del Python: permisos_controller.py
 * 
 * Endpoints:
 * GET    /api/permisos_perfil/{id}    - Obtener permisos de un perfil
 * GET    /api/permisos_perfil/view/{id} - Vista completa (LEFT JOIN)
 * POST   /api/permisos_perfil          - Actualizar permisos (UPSERT o Bulk)
 */
@RestController
@RequestMapping("/api/permisos_perfil")
@Transactional
public class PermisosResource {

    private final Logger log = LoggerFactory.getLogger(PermisosResource.class);
    private final PermisosService permisosService;

    public PermisosResource(PermisosService permisosService) {
        this.permisosService = permisosService;
    }

    /**
     * GET /api/permisos_perfil/{id} : Obtener permisos de un perfil específico.
     * 
     * Si es Admin: retorna todos los módulos con permisos=1
     * Si es Usuario: retorna solo módulos con bitConsulta=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<Map<String, Object>>> getPermisosByPerfil(@PathVariable Integer id) {
        System.out.println("[REST] GET /api/permisos_perfil/" + id);
        List<Map<String, Object>> permisos = permisosService.getPermisosByPerfil(id);
        return ResponseEntity.ok().body(permisos);
    }

    /**
     * GET /api/permisos_perfil/view/{id} : Obtener vista completa (LEFT JOIN).
     * Retorna TODOS los módulos + sus permisos del perfil (aunque sean null/0).
     */
    @GetMapping("/view/{id}")
    public ResponseEntity<List<Map<String, Object>>> getPermisosByViewPerfil(@PathVariable Integer id) {
        System.out.println("[REST] GET /api/permisos_perfil/view/" + id);
        List<Map<String, Object>> permisos = permisosService.getPermisosByViewPerfil(id);
        return ResponseEntity.ok().body(permisos);
    }

    /**
     * POST /api/permisos_perfil : Guardar/actualizar un permiso (UPSERT).
     * 
     * Body esperado (un único permiso):
     * {
     *   "idModulo": 1,
     *   "idPerfil": 2,
     *   "bitAgregar": 1,
     *   "bitEditar": 0,
     *   "bitEliminar": 0,
     *   "bitConsulta": 1,
     *   "bitDetalle": 1
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> updatePermiso(@RequestBody Map<String, Object> data) {
        System.out.println("[REST] POST /api/permisos_perfil - Single update");
        Map<String, Object> resultado = permisosService.updatePermiso(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    /**
     * POST /api/permisos_perfil/bulk : Actualización masiva de permisos (Bulk).
     * 
     * Body esperado:
     * {
     *   "idPerfil": 2,
     *   "permisos": [
     *     {
     *       "idModulo": 1,
     *       "bitAgregar": 1,
     *       "bitEditar": 1,
     *       ...
     *     },
     *     {
     *       "idModulo": 2,
     *       "bitAgregar": 0,
     *       ...
     *     }
     *   ]
     * }
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> updatePermisosBulk(@RequestBody Map<String, Object> data) {
        System.out.println("[REST] POST /api/permisos_perfil/bulk - Bulk update");
        
        try {
            Integer idPerfil = Integer.parseInt(data.get("idPerfil").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> listaPermisos = (List<Map<String, Object>>) data.get("permisos");

            if (listaPermisos == null || listaPermisos.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "msg", "Lista de permisos vacía"));
            }

            // Actualizar cada permiso (UPSERT individual)
            int successCount = 0;
            for (Map<String, Object> perm : listaPermisos) {
                perm.put("idPerfil", idPerfil); // Asegurar que tiene el ID del perfil
                Map<String, Object> resultado = permisosService.updatePermiso(perm);
                if ((Boolean) resultado.get("success")) {
                    successCount++;
                }
            }

            System.out.println("[REST] Actualizados " + successCount + " permisos de " + listaPermisos.size());
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "msg", "Se actualizaron " + successCount + " módulos con éxito"
            ));

        } catch (Exception e) {
            System.out.println("[ERROR] Error en bulk update: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "msg", "Error: " + e.getMessage()));
        }
    }
}
