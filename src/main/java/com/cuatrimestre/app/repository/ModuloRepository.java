package com.cuatrimestre.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cuatrimestre.app.domain.Modulo;

/**
 * Spring Data JPA repository para la entidad Modulo.
 */
@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {
}
