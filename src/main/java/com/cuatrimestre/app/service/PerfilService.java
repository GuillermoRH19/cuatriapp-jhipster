package com.cuatrimestre.app.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cuatrimestre.app.domain.Perfil;
import com.cuatrimestre.app.repository.PerfilRepository;
import com.cuatrimestre.app.repository.UserRepository;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UserRepository userRepository;

    public PerfilService(PerfilRepository perfilRepository, UserRepository userRepository) {
        this.perfilRepository = perfilRepository;
        this.userRepository = userRepository;
    }

    /**
     * Obtiene todos los perfiles.
     * Traducción: get_all()
     */
    public List<Perfil> getAll() {
        System.out.println("[DEBUG] Obteniendo todos los perfiles...");
        return perfilRepository.findAll();
    }

    /**
     * Obtiene un perfil por ID.
     * Traducción: get_by_id(id)
     */
    public Optional<Perfil> getById(Integer idPerfil) {
        System.out.println("[DEBUG] Obteniendo perfil por ID: " + idPerfil);
        return perfilRepository.findById(idPerfil);
    }

    /**
     * Guarda un nuevo perfil o actualiza uno existente.
     * Traducción: save(data)
     * 
     * Data debe contener:
     * - id (opcional, si es null = INSERT)
     * - strNombrePerfil (requerido)
     * - bitAdministrador (checkbox/boolean, convertir a 1/0)
     */
    public Map<String, Object> save(Map<String, Object> data) {
        try {
            Integer id = data.get("id") != null ? Integer.parseInt(data.get("id").toString()) : null;
            String nombre = (String) data.get("nombrePerfil");

            Boolean esAdmin = false;
            Object adminValue = data.get("administrador");
            if (adminValue != null) {
                if (adminValue instanceof Boolean) {
                    esAdmin = (Boolean) adminValue;
                } else {
                    esAdmin = "1".equals(adminValue.toString()) || 
                             "true".equalsIgnoreCase(adminValue.toString()) || 
                             "on".equalsIgnoreCase(adminValue.toString());
                }
            }

            Perfil perfil;
            String msg;

            if (id != null) {
                Optional<Perfil> existing = perfilRepository.findById(id);
                if (existing.isEmpty()) {
                    return Map.of("success", false, "msg", "Perfil no encontrado");
                }
                perfil = existing.orElseThrow();
                perfil.setNombrePerfil(nombre);
                perfil.setAdministrador(esAdmin);
                msg = "Perfil actualizado";
                System.out.println("[DEBUG] Actualizando perfil ID: " + id);
            } else {
                perfil = new Perfil(nombre, esAdmin);
                msg = "Perfil registrado";
                System.out.println("[DEBUG] Creando nuevo perfil: " + nombre);
            }

            perfilRepository.save(perfil);
            return Map.of("success", true, "msg", msg);

        } catch (Exception e) {
            System.out.println("[ERROR] Error al guardar perfil: " + e.getMessage());
            return Map.of("success", false, "msg", e.getMessage());
        }
    }

    /**
     * Elimina un perfil por ID.
     * Traducción: delete(id)
     * Error handling si está en uso
     */
    public Map<String, Object> delete(Integer idPerfil) {
        try {
            System.out.println("[DEBUG] Eliminando perfil ID: " + idPerfil);
            
            Optional<Perfil> perfil = perfilRepository.findById(idPerfil);
            if (perfil.isEmpty()) {
                return Map.of("success", false, "msg", "Perfil no encontrado");
            }

            if (userRepository.existsByPerfilId(idPerfil)) {
                return Map.of("success", false,
                    "msg", "No se puede eliminar: el perfil está asignado a uno o más usuarios");
            }


            perfilRepository.deleteById(idPerfil);
            return Map.of("success", true, "msg", "Perfil eliminado");

        } catch (Exception e) {
            System.out.println("[ERROR] Error al eliminar perfil: " + e.getMessage());
            return Map.of("success", false, "msg", "No se puede eliminar: el perfil está en uso");
        }
    }
}
