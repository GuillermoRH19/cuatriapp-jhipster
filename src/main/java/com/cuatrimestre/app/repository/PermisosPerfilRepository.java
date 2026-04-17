package com.cuatrimestre.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cuatrimestre.app.domain.PermisosPerfil;

@Repository
public interface PermisosPerfilRepository extends JpaRepository<PermisosPerfil, Integer> {

    @Query("SELECT pp FROM PermisosPerfil pp WHERE pp.modulo.id = :moduloId AND pp.perfil.id = :perfilId")
    Optional<PermisosPerfil> findByModuloIdAndPerfilId(@Param("moduloId") Integer moduloId, @Param("perfilId") Integer perfilId);

    List<PermisosPerfil> findByPerfilId(Integer perfilId);

    boolean existsByPerfilId(Integer perfilId);
}
