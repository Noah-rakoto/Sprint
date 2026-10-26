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

    // Sprint 10 bis : /auth/login?nom=Alice&role=admin
    @Methode(value = "/auth/login", type = "GET")
    public Modelmaison login(String nom, String role, MySession session, Modelmaison model) {
        session.setAttribute("user", nom);
        session.setAttribute("role", role);
        model.addAttribute("message", "Connecte en tant que " + nom + " (role " + role + ")");
        model.setView("session");
        return model;
    }

    // Sprint 10 bis : 401 si pas connecte, 403 si le role n'est pas admin
    @Auth(roles = { "admin" })
    @Methode(value = "/auth/admin", type = "GET")
    public Modelmaison admin(MySession session, Modelmaison model) {
        model.addAttribute("message", "Zone admin : bienvenue " + session.getAttribute("user"));
        model.setView("session");
        return model;
    }

    // Sprint 10 bis : plusieurs roles acceptes
    @Auth(roles = { "admin", "manager" })
    @Methode(value = "/auth/gestion", type = "GET")
    public Modelmaison gestion(MySession session, Modelmaison model) {
        model.addAttribute("message", "Gestion : bienvenue " + session.getAttribute("user")
                + " (role " + session.getAttribute("role") + ")");
        model.setView("session");
        return model;
    }
}
