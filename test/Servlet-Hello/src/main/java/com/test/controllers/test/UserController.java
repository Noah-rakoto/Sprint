package com.test.controllers.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.utils.Modelmaison;
import com.test.service.UtilisateurService;

@Controller
public class UserController {
    @Autowired
    UtilisateurService utilisateurService;

    @Methode(value = "/users", type = "GET")
    public Modelmaison getAll(Modelmaison model) {
        List<String> users = new ArrayList<>();
        utilisateurService.recupererTousLesUtilisateurs().forEach(user -> users.add(user.getNom()));

        // 1. On stocke les données pour la JSP
        model.addAttribute("users", users);

        // 2. On définit explicitement la vue de destination !
        model.setView("users");

        return model;
    }
}
