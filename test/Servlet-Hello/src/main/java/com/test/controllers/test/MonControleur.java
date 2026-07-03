package com.test.controllers.test;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;

@Controller
public class MonControleur {

    @Methode(value = "/test", type = "POST")
    public String test() {
        return "La méthode test (GET) a été exécutée !";
    }

    @Methode(value = "/test", type = "GET")
    public String testGet() {
        return "Données reçues en GET sur /test !";
    }

    @Methode("/Noah")
    public String Noah() {
        return "Bienvenue sur la page d'accueil";
    }

    public void fonctionNormale() {
    }
    // curl -X POST http://localhost:8080/Spint3/test
}