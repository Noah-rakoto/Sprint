package com.test.controllers.test;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;

@Controller
public class MonControleur {

    @Methode("/test")
    public String test() {
        return "La méthode test (GET) a été exécutée !";
    }

    @Methode(value = "/test", type = "POST")
    public String testPost() {
        return "Données reçues en POST sur /test !";
    }

    @Methode("/accueil")
    public String home() {
        return "Bienvenue sur la page d'accueil";
    }

    public void fonctionNormale() {
    }
}