package com.test.model;

import java.time.LocalDateTime;

public class Utilisateur {
    private Long id;
    private String nom;
    private String email;
    private String role;
    private LocalDateTime dateInscription;

    // Constructeur vide
    public Utilisateur() {
    }

    // Constructeur pratique
    public Utilisateur(Long id, String nom, String email, String role, LocalDateTime dateInscription) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.dateInscription = dateInscription;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }
}