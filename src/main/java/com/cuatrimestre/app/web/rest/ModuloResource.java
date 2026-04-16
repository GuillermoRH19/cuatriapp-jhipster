package com.cuatrimestre.app.web.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
@RestController
@RequestMapping("/api/modulo")
@Transactional
public class ModuloResource {

    private static final Logger log = LoggerFactory.getLogger(ModuloResource.class);

    private final ModuloService moduloService;

    public ModuloResource(ModuloService moduloService) {
        this.moduloService = moduloService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllModulos() {
        log.debug("REST request to get all modulos");
        return ResponseEntity.ok().body(moduloService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getModuloById(@PathVariable Integer id) {
        log.debug("REST request to get Modulo : {}", id);
        Optional<Map<String, Object>> modulo = moduloService.getById(id);
        return modulo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createModulo(@RequestBody Map<String, Object> data) {
        log.debug("REST request to create Modulo : {}", data);
        Map<String, Object> resultado = moduloService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateModulo(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        log.debug("REST request to update Modulo : {}", id);
        data.put("id", id);
        Map<String, Object> resultado = moduloService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteModulo(@PathVariable Integer id) {
        log.debug("REST request to delete Modulo : {}", id);
        Map<String, Object> resultado = moduloService.delete(id);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @GetMapping("/lista/menus")
    public ResponseEntity<List<Map<String, Object>>> getMenus() {
        log.debug("REST request to get all menus");
        return ResponseEntity.ok().body(moduloService.getMenus());
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<Map<String, Object>> updateMenu(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        log.debug("REST request to update Menu : {}", id);
        data.put("id", id);
        Map<String, Object> resultado = moduloService.updateMenu(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Integer id) {
        log.debug("REST request to delete Menu : {}", id);
        Map<String, Object> resultado = moduloService.deleteMenu(id);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }
}
