package com.test.controllers.test;

import java.util.ArrayList;
import java.util.List;

import com.sprint.annotation.Controller;
import com.test.model.Employe;
import com.test.model.EmployeTest;
import com.test.model.Utilisateur;
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

    // curl -X POST http://localhost:8080/Spint5_bis/fonctionTest2
    @Methode(value = "/fonctionTest", type = "GET")
    public Modelmaison fonctionTest(Modelmaison model) {

        List<String> data = new ArrayList<>();
        data.add("Donnée 1");
        data.add("Donnée 2");
        data.add("Donnée 3");

        model.addAttribute("data", data);
        model.setView("index"); // ✅ Définir la vue avec setView()

        return model;
    }

    @Methode(value = "/fonctionTest2", type = "POST")
    public Modelmaison fonctionTest2(Modelmaison model) {

        List<String> data = new ArrayList<>();
        data.add("Donnée 1");
        data.add("Donnée 2");
        data.add("Donnée 3");

        model.addAttribute("donnees", data);
        model.setView("index2"); // ✅ Définir la vue avec setView()

        return model;
    }

    // Sprint 7 : /hello?nom=Alice&age=25
    @Methode(value = "/hello", type = "GET")
    public Modelmaison hello(String nom, int age, Modelmaison model) {
        model.addAttribute("message", "Bonjour " + nom + ", tu as " + age + " ans");
        model.setView("hello");
        return model;
    }

    // Sprint 7 bis : /addEmploye?nom=Bob&mail=bob@test.com&numero=42
    @Methode(value = "/addEmploye", type = "GET")
    public Modelmaison addEmploye(Employe emp, Modelmaison model) {
        model.addAttribute("message", "Employe ajoute : " + emp.getNom() + " <" + emp.getMail() + "> #" + emp.getNumero());
        model.setView("hello");
        return model;
    }

    // Sprint 7 bis : /addEmp?nom=Alya&mail=Alya@gmail.com&numero=42
    @Methode(value = "/addEmp", type = "GET")
    public Modelmaison addEmp(EmployeTest emp, Modelmaison model) {
        // Le framework laisse wife a null : on la cree a la main pour tester emp.getWife().getNom()
        if (emp.getWife() == null) {
            Employe wife = new Employe();
            wife.setNom("Marie");
            wife.setMail("wife@gmail.com");
            wife.setNumero(123);
            emp.setWife(wife);
        }
        // String wifeInfo = emp.getWife().getNom() + " <" + emp.getWife().getMail() + "> #" + emp.getWife().getNumero();
        String wifeInfo2 = emp.getWife().getNom() + emp.getWife().getMail() + emp.getWife().getNumero();
        model.addAttribute("message", "Employe ajoute : " + emp.getNom() + " <" + emp.getMail() + "> #" + emp.getNumero()
                + " | epouse : " + wifeInfo2);
        model.setView("hello");
        return model;
    }

    @Methode(value = "/form" , type = "GET")
    public Modelmaison form(Modelmaison model){
        model.setView("form");
        return model;
    }
    // Sprint 7 bis : /addEmpUser?nom=Alya&mail=Alya@gmail.com&numero=42
    @Methode(value = "/addEmpUser", type = "GET")
    public Modelmaison addEmpUser(Employe emp, Modelmaison model) {
        // Le framework laisse wife a null : on la cree a la main pour tester emp.getWife().getNom()
        if (emp.getUser() == null) {
            Utilisateur user = new Utilisateur(); 
            user.setNom("Marie");
            user.setEmail("user@gmail.com");
            user.setRole("amdin");
            user.setDateInscription(null);
            emp.setUser(user);
        }
        // String wifeInfo = emp.getWife().getNom() + " <" + emp.getWife().getMail() + "> #" + emp.getWife().getNumero();
        String userInfo = emp.getUser().getNom() + emp.getUser().getEmail() + emp.getUser().getRole();
        model.addAttribute("message", "Employe ajoute : " + emp.getNom() + " <" + emp.getMail() + "> #" + emp.getNumero()
                + " | user : " + userInfo);
        model.setView("testUser");
        return model;
    }

}
