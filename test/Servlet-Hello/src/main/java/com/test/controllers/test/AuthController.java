package com.test.controllers.test;

import com.sprint.annotation.Auth;
import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.utils.Modelmaison;
import com.sprint.utils.MySession;

@Controller
public class AuthController {

    // Sprint 10 : accessible à tout le monde
    @Methode(value = "/auth/public", type = "GET")
    public Modelmaison publique(Modelmaison model) {
        model.addAttribute("message", "Page publique : pas besoin d'etre connecte");
        model.setView("session");
        return model;
    }

    // Sprint 10 : 401 si personne n'est connecte (voir /session/login?nom=Bob)
    @Auth
    @Methode(value = "/auth/prive", type = "GET")
    public Modelmaison prive(MySession session, Modelmaison model) {
        model.addAttribute("message", "Page privee : bienvenue " + session.getAttribute("user"));
        model.setView("session");
        return model;
    }
}
