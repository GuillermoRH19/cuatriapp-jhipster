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
 */
@RestController
@RequestMapping("/api/perfil")
@Transactional
public class PerfilResource {

    private static final Logger log = LoggerFactory.getLogger(PerfilResource.class);

    private final PerfilService perfilService;

    public PerfilResource(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public ResponseEntity<List<Perfil>> getAllPerfiles() {
        log.debug("REST request to get all perfiles");
        return ResponseEntity.ok().body(perfilService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Perfil> getPerfilByid(@PathVariable Integer id) {
        log.debug("REST request to get Perfil : {}", id);
        Optional<Perfil> perfil = perfilService.getById(id);
        return perfil.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPerfil(@RequestBody Map<String, Object> data) {
        log.debug("REST request to create Perfil : {}", data);
        Map<String, Object> resultado = perfilService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePerfil(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        log.debug("REST request to update Perfil : {}", id);
        data.put("id", id);
        Map<String, Object> resultado = perfilService.save(data);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePerfil(@PathVariable Integer id) {
        log.debug("REST request to delete Perfil : {}", id);
        Map<String, Object> resultado = perfilService.delete(id);
        if ((Boolean) resultado.get("success")) {
            return ResponseEntity.ok().body(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }
}
