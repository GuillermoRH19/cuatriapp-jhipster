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

import com.cuatrimestre.app.domain.Perfil;
import com.cuatrimestre.app.service.PerfilService;

/**
 * REST controller para gestionar Perfiles.
 * Traducción directa del Python: perfil_controller.py
 * 
 * Endpoints:
 * GET    /api/perfil          - Obtener todos
 * GET    /api/perfil/{id}     - Obtener por ID
 * POST   /api/perfil          - Crear nuevo
 * PUT    /api/perfil/{id}     - Actualizar
 * DELETE /api/perfil/{id}     - Eliminar
 */
@RestController
@RequestMapping("/api/perfil")
@Transactional
public class PerfilResource {

    private final Logger log = LoggerFactory.getLogger(PerfilResource.class);
    private final PerfilService perfilService;

    public PerfilResource(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * GET /api/perfil : Obtener todos los perfiles.
     */
    @GetMapping
    public ResponseEntity<List<Perfil>> getAllPerfiles() {
        System.out.println("[REST] GET /api/perfil");
        List<Perfil> perfiles = perfilService.getAll();
        return ResponseEntity.ok().body(perfiles);
    }

    /**
     * GET /api/perfil/{id} : Obtener un perfil por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Perfil> getPerfilByid(@PathVariable Integer id) {
        System.out.println("[REST] GET /api/perfil/" + id);
        Optional<Perfil> perfil = perfilService.getById(id);
        if (perfil.isPresent()) {
            return ResponseEntity.ok().body(perfil.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/perfil : Crear un nuevo perfil.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPerfil(@RequestBody Map<String, Object> data) {
        System.out.println("[REST] POST /api/perfil");
        Map<String, Object> resultado = perfilService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    /**
     * PUT /api/perfil/{id} : Actualizar un perfil.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePerfil(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        System.out.println("[REST] PUT /api/perfil/" + id);
        data.put("id", id); // Asegurar que el ID sea el de la URL
        Map<String, Object> resultado = perfilService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    /**
     * DELETE /api/perfil/{id} : Eliminar un perfil.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePerfil(@PathVariable Integer id) {
        System.out.println("[REST] DELETE /api/perfil/" + id);
        Map<String, Object> resultado = perfilService.delete(id);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }
}
