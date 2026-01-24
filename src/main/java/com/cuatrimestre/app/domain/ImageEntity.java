package com.cuatrimestre.app.domain;

import jakarta.persistence.*; // 👈 ESTO ERA EL ERROR (javax -> jakarta)
import java.io.Serializable;

@Entity
@Table(name = "jhi_carousel_image")
public class ImageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(name = "data", columnDefinition="LONGBLOB") // Añadido para asegurar compatibilidad
    private byte[] data;

    @Column(name = "content_type")
    private String contentType;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}