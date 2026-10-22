package com.test.model;

import com.sprint.utils.MapFile;

public class Employe {
    private String nom;
    private String mail;
    private int numero;
    private Utilisateur user;
    private MapFile photo;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public Utilisateur getUser(){ return user; }
    public void setUser(Utilisateur user){this.user = user;}
    public MapFile getPhoto() { return photo; }
    public void setPhoto(MapFile photo) { this.photo = photo; }
}
