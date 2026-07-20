package com.test.service;

import com.test.model.Utilisateur;
import com.test.repo.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public boolean inscrireUtilisateur(Utilisateur utilisateur) {
        if (utilisateur.getNom() == null || utilisateur.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide.");
        }
        if (utilisateur.getEmail() == null || !utilisateur.getEmail().contains("@")) {
            throw new IllegalArgumentException("L'adresse email est invalide.");
        }

        int rowsAffected = utilisateurRepository.save(utilisateur);

        return rowsAffected > 0;
    }

    public List<Utilisateur> recupererTousLesUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur chercherParEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        try {
            return utilisateurRepository.findByEmail(email);
        } catch (Exception e) {
            // JdbcTemplate lève une exception si aucun élément n'est trouvé
            System.out.println("Aucun utilisateur trouvé avec l'email : " + email);
            return null;
        }
    }
}