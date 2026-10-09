# Sprint 7 bis — Objets personnalisés en argument

## Objectif
Une méthode de controller peut recevoir un bean ; le framework le construit à partir des
paramètres de requête portant le nom de ses attributs.

```java
@Methode(value = "/addEmploye", type = "GET")
public Modelmaison addEmploye(Employe emp, Modelmaison model) { ... }
```

## Changements dans le code

### Modifié : `framework/.../com/sprint/utils/ParamMapper.java`
- `isSimpleType(Class)` : primitifs, `String`, `Number`, `Boolean`, `Character`.
- Dans `mapParameters` : type simple → `convert(request.getParameter(nom))` (Sprint 7) ;
  sinon → `buildBean(type, request)`.
- `buildBean(Class, HttpServletRequest)` :
  1. instancie via le constructeur sans argument (obligatoire) ;
  2. pour chaque champ non statique de type simple, lit `request.getParameter(<nom du champ>)` ;
  3. convertit avec `convert(...)` ;
  4. appelle `setXxx(...)` si le setter existe, sinon écrit le champ par réflexion (`setAccessible`) ;
  5. champ absent de la requête → laissé à sa valeur par défaut.
- Limites : pas de beans imbriqués ni de collections.

### Projet de test
- Nouveau modèle : `com.test.model.Employe` (`nom`, `mail`, `numero` + getters/setters).
- `MonControleur` : ajout de `addEmploye(Employe emp, Modelmaison model)`.
- `deploy.sh` : `APP_NAME="Sprint7bis"`.

## Routes créées

| Méthode | URL | Controller | Vue |
|---|---|---|---|
| GET | `/addEmploye?nom=Bob&mail=bob@test.com&numero=42` | `MonControleur.addEmploye(Employe emp, Modelmaison model)` | `hello` |

Résultat attendu : `Employe ajoute : Bob <bob@test.com> #42`.
La route `/hello` du Sprint 7 reste disponible.

## Vues
Aucune nouvelle vue : `addEmploye` réutilise `WEB-INF/views/hello.jsp` (affiche `${message}`).

## Déploiement
```bash
cd framework/framework && mvn clean package
cd ../../test/Servlet-Hello && ./deploy.sh   # WAR Sprint7bis → /opt/tomcat11/webapps
# http://localhost:8080/Sprint7bis/addEmploye?nom=Bob&mail=bob@test.com&numero=42
```
