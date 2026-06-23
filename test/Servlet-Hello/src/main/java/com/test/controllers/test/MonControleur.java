package com.test.controllers.test.MonControleur;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.utils.GetterMethode;

@Controller
public class MonControleur {

    @Methode("/test")
    public String test() {
        return "La méthode test a été exécutée !";
    }

    @Methode("/accueil")
    public String home() {
        return "Bienvenue sur la page d'accueil";
    }

    public void fonctionNormale() {
    }

    // public static void main(String[] args) {
    // GetterMethode scanner = new GetterMethode();
    // scanner.scannerUneClasse(MonControleur.class);
    // }
}
