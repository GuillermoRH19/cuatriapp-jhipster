package com.cuatrimestre.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cuatrimestre.app.domain.Menu;
import com.cuatrimestre.app.domain.Modulo;
import com.cuatrimestre.app.repository.MenuRepository;
import com.cuatrimestre.app.repository.ModuloRepository;
import com.cuatrimestre.app.repository.PermisosPerfilRepository;

/**
 * Servicio para gestionar Módulos.
 * Traducción directa de ModuloService (Python).
 * 
 * LÓGICA ESPECIAL: 
 * - Al guardar un módulo con nombreMenu, si no existe lo crea automáticamente
 * - MAGIA: Permite crear menús on-the-fly
 */
@Service
public class ModuloService {

    private final ModuloRepository moduloRepository;
    private final MenuRepository menuRepository;
    private final PermisosPerfilRepository permisosPerfilRepository;

    public ModuloService(ModuloRepository moduloRepository, MenuRepository menuRepository, PermisosPerfilRepository permisosPerfilRepository) {
        this.moduloRepository = moduloRepository;
        this.menuRepository = menuRepository;
        this.permisosPerfilRepository = permisosPerfilRepository;
    }

    /**
     * Obtiene todos los módulos con JOIN a Menu.
     * Traducción: get_all()
     */
    public List<Map<String, Object>> getAll() {
        System.out.println("[DEBUG] Obteniendo todos los módulos...");
        List<Map<String, Object>> resultado = new ArrayList<>();

        List<Modulo> modulos = moduloRepository.findAll();
        for (Modulo m : modulos) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", m.getId());
            row.put("strNombreModulo", m.getNombreModulo());
            row.put("idMenu", m.getMenu() != null ? m.getMenu().getId() : null);
            row.put("strNombreMenu", m.getMenu() != null ? m.getMenu().getNombreMenu() : "Sin menú");
            row.put("strRuta", m.getRuta());
            resultado.add(row);
        }

        return resultado;
    }

    /**
     * Obtiene un módulo por ID con JOIN a Menu.
     * Traducción: get_by_id(id)
     */
    public Optional<Map<String, Object>> getById(Integer idModulo) {
        System.out.println("[DEBUG] Obteniendo módulo ID: " + idModulo);
        Optional<Modulo> moduloOpt = moduloRepository.findById(idModulo);

        return moduloOpt.map(m -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", m.getId());
            row.put("strNombreModulo", m.getNombreModulo());
            row.put("idMenu", m.getMenu() != null ? m.getMenu().getId() : null);
            row.put("strNombreMenu", m.getMenu() != null ? m.getMenu().getNombreMenu() : "Sin menú");
            row.put("strRuta", m.getRuta());
            return row;
        });
    }

    /**
     * Guarda un módulo con MAGIA de creación de menú.
     * Traducción: save()
     * 
     * Data debe contener:
     * - id (opcional para UPDATE)
     * - strNombreModulo (requerida)
     * - nombreMenu (TEXTO del menú padre, ¡puede no existir!)
     * - strRuta (opcional)
     */
    public Map<String, Object> save(Map<String, Object> data) {
        try {
            Integer idModulo = data.get("id") != null ? Integer.parseInt(data.get("id").toString()) : null;
            String nombre = (String) data.get("strNombreModulo");
            String nombreMenu = (String) data.get("nombreMenu");
            String ruta = (String) data.get("strRuta");

            System.out.println("[DEBUG] Guardando módulo con menú: " + nombreMenu);

            if (nombreMenu == null || nombreMenu.trim().isEmpty()) {
                return Map.of("success", false, "msg", "El Menú Padre es obligatorio");
            }

            Optional<Menu> menuExistente = menuRepository.findByNombreMenu(nombreMenu);
            Integer idMenu;

            if (menuExistente.isPresent()) {
                idMenu = menuExistente.orElseThrow().getId();
                System.out.println("[DEBUG] Menú ya existe con ID: " + idMenu);
            } else {
                Menu nuevoMenu = new Menu(nombreMenu);
                Menu menuGuardado = menuRepository.save(nuevoMenu);
                idMenu = menuGuardado.getId();
                System.out.println("[DEBUG] Nuevo menú creado con ID: " + idMenu);
            }

            Modulo modulo;
            String msg;

            if (idModulo != null) {
                Optional<Modulo> existente = moduloRepository.findById(idModulo);
                if (existente.isEmpty()) {
                    return Map.of("success", false, "msg", "Módulo no encontrado");
                }

                modulo = existente.orElseThrow();
                modulo.setNombreModulo(nombre);
                // Solo actualizar la ruta si el admin la cambió explícitamente
                if (ruta != null && !ruta.trim().isEmpty()) {
                    modulo.setRuta(ruta);
                }

                Optional<Menu> menu = menuRepository.findById(idMenu);
                if (menu.isPresent()) {
                    modulo.setMenu(menu.orElseThrow());
                }

                msg = "Módulo actualizado";
                Modulo guardado = moduloRepository.save(modulo);
                System.out.println("[DEBUG] Actualizando módulo ID: " + idModulo);
                return Map.of("success", true, "msg", msg, "id", guardado.getId(), "ruta", guardado.getRuta() != null ? guardado.getRuta() : "");
            } else {
                Optional<Menu> menu = menuRepository.findById(idMenu);
                if (menu.isEmpty()) {
                    return Map.of("success", false, "msg", "Menú no encontrado");
                }

                modulo = new Modulo(nombre, menu.orElseThrow(), ruta);
                msg = "Módulo registrado";
                System.out.println("[DEBUG] Creando nuevo módulo con menú: " + nombreMenu);
                Modulo guardado = moduloRepository.save(modulo);

                // Auto-generar la ruta con el ID si no se proporcionó una
                if (ruta == null || ruta.trim().isEmpty()) {
                    guardado.setRuta("/dashboard/modulo/" + guardado.getId());
                    moduloRepository.save(guardado);
                }

                String rutaFinal = guardado.getRuta();
                return Map.of("success", true, "msg", msg, "id", guardado.getId(), "ruta", rutaFinal != null ? rutaFinal : "");
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Error al guardar módulo: " + e.getMessage());
            e.printStackTrace();
            return Map.of("success", false, "msg", e.getMessage());
        }
    }

    /**
     * Elimina un módulo.
     * Traducción: delete()
     */
    public Map<String, Object> delete(Integer idModulo) {
        try {
            System.out.println("[DEBUG] Eliminando permisos asociados al módulo ID: " + idModulo);
            
            // Eliminar dependencias primero para evitar error de FK (Foreign Key)
            permisosPerfilRepository.deleteByModuloId(idModulo);

            System.out.println("[DEBUG] Eliminando módulo ID: " + idModulo);

            Optional<Modulo> modulo = moduloRepository.findById(idModulo);
            if (modulo.isEmpty()) {
                return Map.of("success", false, "msg", "Módulo no encontrado");
            }

            moduloRepository.deleteById(idModulo);
            return Map.of("success", true, "msg", "Módulo eliminado");

        } catch (Exception e) {
            System.out.println("[ERROR] Error al eliminar módulo: " + e.getMessage());
            return Map.of("success", false, 
                "msg", "Ocurrió un error inesperado al intentar eliminar el módulo: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los menús disponibles.
     * Traducción: get_menus()
     */
    public List<Map<String, Object>> getMenus() {
        System.out.println("[DEBUG] Obteniendo todos los menús...");
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Menu menu : menuRepository.findAll()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", menu.getId());
            row.put("strNombreMenu", menu.getNombreMenu());
            resultado.add(row);
        }

        return resultado;
    }

    /**
     * Actualiza un menú.
     * Traducción: update_menu()
     */
    public Map<String, Object> updateMenu(Map<String, Object> data) {
        try {
            Integer idMenu = Integer.parseInt(data.get("id").toString());
            String nombreMenu = (String) data.get("strNombreMenu");

            System.out.println("[DEBUG] Actualizando menú ID: " + idMenu);

            Optional<Menu> menuOpt = menuRepository.findById(idMenu);
            if (menuOpt.isEmpty()) {
                return Map.of("success", false, "msg", "Menú no encontrado");
            }

            Menu menu = menuOpt.orElseThrow();
            menu.setNombreMenu(nombreMenu);
            menuRepository.save(menu);

            return Map.of("success", true, "msg", "Menú actualizado correctamente");

        } catch (Exception e) {
            System.out.println("[ERROR] Error al actualizar menú: " + e.getMessage());
            return Map.of("success", false, "msg", e.getMessage());
        }
    }

    /**
     * Elimina un menú.
     * Traducción: delete_menu()
     */
    public Map<String, Object> deleteMenu(Integer idMenu) {
        try {
            System.out.println("[DEBUG] Eliminando menú ID: " + idMenu);

            Optional<Menu> menu = menuRepository.findById(idMenu);
            if (menu.isEmpty()) {
                return Map.of("success", false, "msg", "Menú no encontrado");
            }

            if (!menu.orElseThrow().getModulos().isEmpty()) {
                return Map.of("success", false, 
                    "msg", "No se puede eliminar: el menú está siendo utilizado por uno o más módulos");
            }

            menuRepository.deleteById(idMenu);
            return Map.of("success", true, "msg", "Menú eliminado correctamente");

        } catch (Exception e) {
            System.out.println("[ERROR] Error al eliminar menú: " + e.getMessage());
            return Map.of("success", false, 
                "msg", "No se puede eliminar: el menú está siendo utilizado por uno o más módulos");
        }
    }
}
