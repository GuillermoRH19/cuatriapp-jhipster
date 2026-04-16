package com.cuatrimestre.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cuatrimestre.app.domain.Perfil;

/**
 * Spring Data JPA repository para la entidad Perfil.
 */
@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Integer> {
}
