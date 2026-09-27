package com.test.controllers.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.annotation.ApiRest;
import com.test.model.Utilisateur;
import com.test.service.UtilisateurService;
import com.sprint.utils.Modelmaison;

@ApiRest(value = "/api")
@Controller
public class UserApiController {
    @Autowired
    UtilisateurService utilisateurService;

    @Methode(value = "/usersAPI", type = "GET")
    public List<Utilisateur> getAll() {
        return utilisateurService.recupererTousLesUtilisateurs(); // → JSON automatique
    }

    @Methode(value = "/users/me", type = "GET")
    public String getCurrentUser() {
        return "John Doe"; // → "John Doe" en JSON
    }

    @Methode(value = "/usersAPI/modele", type = "GET")
    public Modelmaison getModel() {
        Modelmaison model = new Modelmaison();
        model.addAttribute("message", "Hello from the model!");
        return model;
    }

    @Methode(value = "/usersAPI/test", type = "GET")
    public Modelmaison testModelSansJson(Modelmaison model) {
        List<Utilisateur> users = utilisateurService.recupererTousLesUtilisateurs();

        // 1. On stocke les données pour la JSP
        model.addAttribute("users", users);

        // 2. On définit explicitement la vue de destination !
        model.setView("usersAPI");

        return model;
    }
}
