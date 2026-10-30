package com.test.controllers.test;

import com.sprint.annotation.Controller;
import com.sprint.annotation.Methode;
import com.sprint.utils.Modelmaison;
import com.test.service.UtilisateurService;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class SingletonController {
    @Autowired
    UtilisateurService utilisateurService;

    // Sprint 11 : uniquement pour le test, prouve que l'instance survit entre les requêtes.
    // Un vrai contrôleur ne garde pas ce genre d'état.
    private final AtomicInteger compteur = new AtomicInteger();

    // Sprint 11 : le numero doit rester le meme a chaque appel
    @Methode(value = "/singleton/instance", type = "GET")
    public Modelmaison instance(Modelmaison model) {
        model.addAttribute("message", "Instance numero " + System.identityHashCode(this));
        model.setView("session");
        return model;
    }

    // Sprint 11 : autre route, meme classe : doit afficher le meme numero que /singleton/instance
    @Methode(value = "/singleton/instance2", type = "GET")
    public Modelmaison instance2(Modelmaison model) {
        model.addAttribute("message", "Instance numero " + System.identityHashCode(this));
        model.setView("session");
        return model;
    }

    // Sprint 11 : avec une nouvelle instance par requete, on resterait toujours a 1
    @Methode(value = "/singleton/compteur", type = "GET")
    public Modelmaison compteur(Modelmaison model) {
        model.addAttribute("message", "Appel numero " + compteur.incrementAndGet());
        model.setView("session");
        return model;
    }

    // Sprint 11 : l'injection faite dans init() doit etre encore la
    @Methode(value = "/singleton/service", type = "GET")
    public Modelmaison service(Modelmaison model) {
        model.addAttribute("message", "Service injecte : " + (utilisateurService != null));
        model.setView("session");
        return model;
    }
}
