package com.sprint.utils;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/**
 * Fichier envoyé par un formulaire multipart : nom, extension, type MIME et contenu brut.
 */
public class MapFile {
    private String name;
    private String extension;
    private String contentType;
    private byte[] bytes;

    public MapFile() {}

    public byte[] getBytes() {
        return bytes;
    }
    public String getExtension() {
        return extension;
    }
    public String getName() {
        return name;
    }
    public String getContentType() {
        return contentType;
    }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    public void setExtension(String extension) {
        this.extension = extension;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /** Nom complet du fichier, ex : "photo.png". */
    public String getFileName() {
        return extension.isEmpty() ? name : name + "." + extension;
    }

    /**
     * Construit un MapFile depuis une Part ; null si la Part n'est pas un fichier
     * ou si aucun fichier n'a été choisi dans le formulaire.
     */
    public static MapFile fromPart(Part part) throws IOException {
        if (part == null) {
            return null;
        }
        String submitted = part.getSubmittedFileName();
        if (submitted == null || submitted.isEmpty() || part.getSize() == 0) {
            return null;
        }
        // Certains navigateurs envoient le chemin complet ("C:\...\photo.png")
        String fileName = submitted.substring(Math.max(submitted.lastIndexOf('/'), submitted.lastIndexOf('\\')) + 1);

        int dot = fileName.lastIndexOf('.');
        MapFile mf = new MapFile();
        mf.setName(dot > 0 ? fileName.substring(0, dot) : fileName);
        mf.setExtension(dot > 0 ? fileName.substring(dot + 1) : "");
        mf.setContentType(part.getContentType());
        try (InputStream is = part.getInputStream()) {
            mf.setBytes(is.readAllBytes());
        }
        return mf;
    }
}
