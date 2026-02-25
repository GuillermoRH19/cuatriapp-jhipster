package com.cuatrimestre.app.web.rest;

import com.cuatrimestre.app.domain.Candidato;
import com.cuatrimestre.app.repository.CandidatoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Transactional
public class CandidatoResource {

    private final Logger log = LoggerFactory.getLogger(CandidatoResource.class);
    private static final String ENTITY_NAME = "candidato";

    @Value("${jhipster.clientApp.name:jhipsterApp}")
    private String applicationName;

    private final CandidatoRepository candidatoRepository;

    public CandidatoResource(CandidatoRepository candidatoRepository) {
        this.candidatoRepository = candidatoRepository;
    }

    /**
     * {@code POST  /candidatoes} : Create a new candidato.
     */
    @PostMapping("/candidatoes")
    public ResponseEntity<Candidato> createCandidato(@RequestBody Candidato candidato) throws URISyntaxException {
        log.debug("REST request to save Candidato : {}", candidato);
        if (candidato.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Candidato result = candidatoRepository.save(candidato);
        return ResponseEntity
            .created(new URI("/api/candidatoes/" + result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /candidatoes/:id} : Updates an existing candidato.
     * ESTE METODO FALTABA Y ES NECESARIO PARA EDITAR
     */
    @PutMapping("/candidatoes/{id}")
    public ResponseEntity<Candidato> updateCandidato(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Candidato candidato
    ) throws URISyntaxException {
        log.debug("REST request to update Candidato : {}, {}", id, candidato);

        if (candidato.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!Objects.equals(id, candidato.getId())) {
            return ResponseEntity.badRequest().build();
        }

        if (!candidatoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Candidato result = candidatoRepository.save(candidato);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code PATCH  /candidatoes/:id} : Partial updates given fields.
     */
    @PatchMapping(value = "/candidatoes/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Candidato> partialUpdateCandidato(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Candidato candidato
    ) throws URISyntaxException {
        log.debug("REST request to partial update Candidato partially : {}, {}", id, candidato);

        if (candidato.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!Objects.equals(id, candidato.getId())) {
            return ResponseEntity.badRequest().build();
        }

        if (!candidatoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Optional<Candidato> result = candidatoRepository
            .findById(candidato.getId())
            .map(existingCandidato -> {
                if (candidato.getNombre() != null) {
                    existingCandidato.setNombre(candidato.getNombre());
                }
                if (candidato.getEmail() != null) {
                    existingCandidato.setEmail(candidato.getEmail());
                }
                if (candidato.getFechaNacimiento() != null) {
                    existingCandidato.setFechaNacimiento(candidato.getFechaNacimiento());
                }
                if (candidato.getDepartamento() != null) {
                    existingCandidato.setDepartamento(candidato.getDepartamento());
                }
                if (candidato.getSalario() != null) {
                    existingCandidato.setSalario(candidato.getSalario());
                }
                return existingCandidato;
            })
            .map(candidatoRepository::save);

        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@code GET  /candidatoes} : get all the candidatoes.
     */
    @GetMapping("/candidatoes")
    public List<Candidato> getAllCandidatoes() {
        log.debug("REST request to get all Candidatoes");
        return candidatoRepository.findAll();
    }

    /**
     * {@code GET  /candidatoes/:id} : get the "id" candidato.
     */
    @GetMapping("/candidatoes/{id}")
    public ResponseEntity<Candidato> getCandidato(@PathVariable Long id) {
        log.debug("REST request to get Candidato : {}", id);
        Optional<Candidato> candidato = candidatoRepository.findById(id);
        return candidato.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@code DELETE  /candidatoes/:id} : delete the "id" candidato.
     * ESTE METODO FALTABA Y ES NECESARIO PARA ELIMINAR
     */
    @DeleteMapping("/candidatoes/{id}")
    public ResponseEntity<Void> deleteCandidato(@PathVariable Long id) {
        log.debug("REST request to delete Candidato : {}", id);
        candidatoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}