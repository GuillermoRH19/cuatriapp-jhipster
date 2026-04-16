package com.cuatrimestre.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cuatrimestre.app.domain.Menu;

/**
 * Spring Data JPA repository para la entidad Menu.
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {
    
    /**
     * Busca un menú por su nombre exacto.
     * Necesario para la lógica de creación automática de menús.
     */
    Optional<Menu> findByNombreMenu(String nombreMenu);
}
