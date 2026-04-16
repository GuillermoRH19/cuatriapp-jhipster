package com.cuatrimestre.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cuatrimestre.app.domain.PermisosPerfil;

/**
 * Spring Data JPA repository para la entidad PermisosPerfil.
 */
@Repository
public interface PermisosPerfilRepository extends JpaRepository<PermisosPerfil, Integer> {
    
    /**
     * Encuentra un permiso específico por módulo e perfil.
     */
    Optional<PermisosPerfil> findByModuloIdAndPerfilId(Integer moduloId, Integer perfilId);
}
