package com.cuatrimestre.app.repository;

import com.cuatrimestre.app.domain.Candidato;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatoRepository extends JpaRepository<Candidato, Long> {
    // Aquí puedes agregar métodos mágicos si necesitas, ej:
    // Optional<Candidato> findByEmail(String email);
}