package com.test.controllers.test;

import java.util.ArrayList;
import java.util.List;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.utils.Modelmaison;

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

    // curl -X POST http://localhost:8080/Spint5/test
    // curl -X POST http://localhost:8080/Spint5/fonctionTest
    @Methode(value = "/fonctionTest", type = "GET")
    public String fonctionTest(Modelmaison model) {

        List<String> data = new ArrayList<>();
        data.add("Donnée 1");
        data.add("Donnée 2");
        data.add("Donnée 3");

        model.addAttribute("data", data);

        return "index"; // Retourne uniquement le nom de la vue
    }
}