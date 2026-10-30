package com.test.controllers.test;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.utils.Modelmaison;

@Controller
public class SingletonAutreController {

    // Sprint 11 : une instance par classe : le numero doit etre different de /singleton/instance
    @Methode(value = "/singleton/autre", type = "GET")
    public Modelmaison autre(Modelmaison model) {
        model.addAttribute("message", "Instance numero " + System.identityHashCode(this));
        model.setView("session");
        return model;
    }
}
