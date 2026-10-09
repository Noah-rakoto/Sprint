# Sprint 7 — Paramètres de requête injectés dans les méthodes du controller

## Objectif
Une méthode de controller peut déclarer des arguments simples ; le framework les remplit
à partir des paramètres HTTP (même nom), en convertissant vers le bon type.

```java
@Methode(value = "/hello", type = "GET")
public Modelmaison hello(String nom, int age, Modelmaison model) { ... }
```

## Changements dans le code

### Nouveau : `framework/.../com/sprint/utils/ParamMapper.java`
- `mapParameters(Method, HttpServletRequest, Modelmaison)` : parcourt `method.getParameters()`
  et construit le `Object[]` d'arguments.
  - `Modelmaison` → injecte le modèle du framework (comportement conservé).
  - Autre type → `request.getParameter(<nom du paramètre Java>)` puis `convert(...)`.
- `convert(String, Class)` : `String`, `int`, `long`, `double`, `float`, `short`, `byte`,
  `boolean`, `char` et wrappers.
  - Paramètre absent ou vide : `null` (objet) ou valeur par défaut du primitif (0, `false`, `'\0'`).
  - Valeur invalide (ex. `age=abc`) : `IllegalArgumentException` → réponse 500.
- Le nom des paramètres Java est lu par réflexion : requiert `<parameters>true</parameters>`
  dans le `maven-compiler-plugin` (déjà présent dans les deux `pom.xml`).

### Modifié : `FrontController.java`
- Suppression de `buildMethodArguments` (ne gérait que `Modelmaison`).
- L'étape 5 appelle `ParamMapper.mapParameters(targetMethod, request, model)`.

### Projet de test
- `MonControleur` : ajout de la méthode `hello`.
- `deploy.sh` : `APP_NAME="Sprint7"`.

## Routes créées

| Méthode | URL | Controller | Vue |
|---|---|---|---|
| GET | `/hello?nom=Alice&age=25` | `MonControleur.hello(String nom, int age, Modelmaison model)` | `hello` |

Résultat attendu : `Bonjour Alice, tu as 25 ans`.
Sans `age` : `age = 0`. Avec `age=abc` : erreur 500.

## Vues
- Nouveau : `test/Servlet-Hello/src/main/webapp/WEB-INF/views/hello.jsp`
  affiche `${message}` (attribut ajouté au modèle par `hello`).

## Déploiement
```bash
cd framework/framework && mvn clean package
cd ../../test/Servlet-Hello && ./deploy.sh   # WAR Sprint7 → /opt/tomcat11/webapps
# http://localhost:8080/Sprint7/hello?nom=Alice&age=25
```
