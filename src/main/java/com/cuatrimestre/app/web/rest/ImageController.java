package com.cuatrimestre.app.web.rest;

import com.cuatrimestre.app.domain.ImageEntity;
import com.cuatrimestre.app.repository.ImageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Transactional
public class ImageController {

    private final ImageRepository imageRepository;

    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    // Guardar imagen
    @PostMapping("/images")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 👇👇👇 VALIDACIÓN DE SEGURIDAD (BACKEND) 👇👇👇
            String contentType = file.getContentType();
            // Si no tiene tipo o no empieza con "image/", rechazamos
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Error: El archivo debe ser una imagen válida.");
            }
            // 👆👆👆 FIN VALIDACIÓN 👆👆👆

            ImageEntity img = new ImageEntity();
            img.setTitle(file.getOriginalFilename());
            img.setContentType(file.getContentType());
            img.setData(file.getBytes());
            
            imageRepository.save(img);
            return ResponseEntity.ok("Imagen guardada con ID: " + img.getId());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // Obtener imágenes para el carrusel
    @GetMapping("/images")
    public List<ImageDTO> getImages() {
        return imageRepository.findAll().stream().map(img -> {
            String base64 = Base64.getEncoder().encodeToString(img.getData());
            String src = "data:" + img.getContentType() + ";base64," + base64;
            return new ImageDTO(src, img.getTitle(), "Imagen subida");
        }).collect(Collectors.toList());
    }

    // DTO simple interno
    static class ImageDTO {
        public String src;
        public String title;
        public String desc;

        public ImageDTO(String src, String title, String desc) {
            this.src = src;
            this.title = title;
            this.desc = desc;
        }
    }
}