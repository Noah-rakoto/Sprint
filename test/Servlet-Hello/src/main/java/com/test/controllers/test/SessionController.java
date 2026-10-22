package com.test.controllers.test;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.utils.Modelmaison;
import com.sprint.utils.MySession;

@Controller
public class SessionController {

    // Sprint 9 : /session/login?nom=Bob
    @Methode(value = "/session/login", type = "GET")
    public Modelmaison login(String nom, MySession session, Modelmaison model) {
        session.setAttribute("user", nom);
        session.setAttribute("visites", 0);
        model.addAttribute("message", "Connecte en tant que " + nom);
        model.setView("session");
        return model;
    }

    // Sprint 9 : lit la session et compte les visites
    @Methode(value = "/session/profil", type = "GET")
    public Modelmaison profil(MySession session, Modelmaison model) {
        Object user = session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "Personne n'est connecte");
        } else {
            int visites = (Integer) session.getAttribute("visites") + 1;
            session.setAttribute("visites", visites);
            model.addAttribute("message", "Bonjour " + user + ", visite numero " + visites);
        }
        model.setView("session");
        return model;
    }

    // Sprint 9 : vide la session
    @Methode(value = "/session/logout", type = "GET")
    public Modelmaison logout(MySession session, Modelmaison model) {
        session.destroy();
        model.addAttribute("message", "Deconnecte");
        model.setView("session");
        return model;
    }
}
