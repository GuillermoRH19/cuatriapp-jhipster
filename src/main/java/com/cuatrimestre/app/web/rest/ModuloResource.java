package com.cuatrimestre.app.web.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cuatrimestre.app.service.ModuloService;

/**
 * REST controller para gestionar Módulos.
 * Traducción directa del Python: modulo_controller.py
 * 
 * Endpoints:
 * GET    /api/modulo          - Obtener todos
 * GET    /api/modulo/{id}     - Obtener por ID
 * POST   /api/modulo          - Crear nuevo (con MAGIA de creación de menú)
 * PUT    /api/modulo/{id}     - Actualizar
 * DELETE /api/modulo/{id}     - Eliminar
 * GET    /api/menu            - Obtener todos los menús
 * PUT    /api/menu/{id}       - Actualizar menú
 * DELETE /api/menu/{id}       - Eliminar menú
 */
@RestController
@RequestMapping("/api/modulo")
@Transactional
public class ModuloResource {

    private final ModuloService moduloService;

    public ModuloResource(ModuloService moduloService) {
        this.moduloService = moduloService;
    }

    /**
     * GET /api/modulo : Obtener todos los módulos.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllModulos() {
        System.out.println("[REST] GET /api/modulo");
        List<Map<String, Object>> modulos = moduloService.getAll();
        return ResponseEntity.ok().body(modulos);
    }

    /**
     * GET /api/modulo/{id} : Obtener un módulo por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getModuloById(@PathVariable Integer id) {
        System.out.println("[REST] GET /api/modulo/" + id);
        Optional<Map<String, Object>> modulo = moduloService.getById(id);
        if (modulo.isPresent()) {
            return ResponseEntity.ok().body(modulo.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/modulo : Crear un nuevo módulo (con MAGIA de menú).
     * 
     * Body esperado:
     * {
     *   "strNombreModulo": "Nombre del módulo",
     *   "nombreMenu": "Nombre del menú padre (se crea si no existe)",
     *   "strRuta": "/ruta/opcional"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createModulo(@RequestBody Map<String, Object> data) {
        System.out.println("[REST] POST /api/modulo");
        Map<String, Object> resultado = moduloService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    /**
     * PUT /api/modulo/{id} : Actualizar un módulo.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateModulo(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        System.out.println("[REST] PUT /api/modulo/" + id);
        data.put("id", id);
        Map<String, Object> resultado = moduloService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    /**
     * DELETE /api/modulo/{id} : Eliminar un módulo.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteModulo(@PathVariable Integer id) {
        System.out.println("[REST] DELETE /api/modulo/" + id);
        Map<String, Object> resultado = moduloService.delete(id);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    /**
     * GET /api/menu : Obtener todos los menús disponibles.
     */
    @GetMapping("/lista/menus")
    public ResponseEntity<List<Map<String, Object>>> getMenus() {
        System.out.println("[REST] GET /api/menu");
        List<Map<String, Object>> menus = moduloService.getMenus();
        return ResponseEntity.ok().body(menus);
    }

    /**
     * PUT /api/menu/{id} : Actualizar un menú.
     */
    @PutMapping("/menu/{id}")
    public ResponseEntity<Map<String, Object>> updateMenu(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        System.out.println("[REST] PUT /api/menu/" + id);
        data.put("id", id);
        Map<String, Object> resultado = moduloService.updateMenu(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    /**
     * DELETE /api/menu/{id} : Eliminar un menú.
     */
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Integer id) {
        System.out.println("[REST] DELETE /api/menu/" + id);
        Map<String, Object> resultado = moduloService.deleteMenu(id);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }
}
