# Sprint 7 — Paramètres de requête injectés dans les méthodes du controller

## 1. Problème de départ

Avant ce sprint, le `FrontController` ne savait fournir qu'**un seul type d'argument** à une
méthode de controller : le `Modelmaison`. Pour lire `?nom=Alice&age=25`, le développeur devait
écrire `request.getParameter("nom")` lui-même, mais le controller n'a même pas accès à la
`HttpServletRequest`. Il n'y avait donc aucun moyen propre de récupérer des paramètres.

L'ancien code (supprimé) :

```java
// FrontController.buildMethodArguments : ne gérait que Modelmaison, le reste = null
for (int i = 0; i < params.length; i++) {
    if (params[i].getType().equals(Modelmaison.class)) args[i] = model;
}
```

## 2. Objectif

Le développeur déclare simplement les arguments dont il a besoin, le framework les remplit :

```java
@Methode(value = "/hello", type = "GET")
public Modelmaison hello(String nom, int age, Modelmaison model) {
    model.addAttribute("message", "Bonjour " + nom + ", tu as " + age + " ans");
    model.setView("hello");
    return model;
}
```

`GET /hello?nom=Alice&age=25` → `nom = "Alice"`, `age = 25` (un vrai `int`, pas une `String`).

## 3. Fonctionnement : le flux d'une requête

```
GET /hello?nom=Alice&age=25
   │
   ▼
FrontController.processRequest
   ├─ 1. extractRouteKey      → UrlKey("/hello", GET)
   ├─ 2. listeMapping.get()   → Mapping(MonControleur, "hello")
   ├─ 3. newInstance + autowire Spring
   ├─ 4. new Modelmaison()
   ├─ 5. ParamMapper.mapParameters(method, request, model)   ← NOUVEAU
   │        ["Alice", 25, model]
   ├─ 6. method.invoke(controller, args)
   └─ 7. handleMethodResult   → forward vers /WEB-INF/views/hello.jsp
```

Le point clé : `Method.invoke` attend un `Object[]` **dans l'ordre exact de la signature**.
`ParamMapper` construit ce tableau en se basant sur la signature, pas sur l'ordre des
paramètres dans l'URL.

## 4. Le code, et pourquoi on l'a fait comme ça

### 4.1 `ParamMapper.mapParameters` — construire le tableau d'arguments

Fichier : `framework/.../com/sprint/utils/ParamMapper.java`

```java
public static Object[] mapParameters(Method method, HttpServletRequest request, Modelmaison model) {
    Parameter[] params = method.getParameters();
    Object[] args = new Object[params.length];

    for (int i = 0; i < params.length; i++) {
        Class<?> type = params[i].getType();

        if (type.equals(Modelmaison.class)) {
            args[i] = model;               // comportement historique conservé
            continue;
        }
        args[i] = convert(request.getParameter(params[i].getName()), type);
    }
    return args;
}
```

| Choix | Pourquoi |
|---|---|
| On itère sur `method.getParameters()` et non sur `request.getParameterMap()` | La **signature est la source de vérité** : l'ordre et le nombre d'arguments viennent de la méthode, pas de l'URL. Un paramètre HTTP en trop est ignoré, un manquant donne une valeur par défaut. |
| `Modelmaison` traité en premier, avant toute conversion | Il ne vient pas de la requête mais du framework. Sans ce `continue`, on essaierait de convertir `request.getParameter("model")` en `Modelmaison`. |
| `request.getParameter(nom)` | Retourne la 1re valeur : suffisant pour des types simples. (`getParameterMap()` renvoie des `String[]`, inutile ici.) |
| Une classe dédiée `ParamMapper` plutôt qu'une méthode privée du `FrontController` | Sépare la logique d'URL/routage de la logique de conversion. Le `FrontController` reste lisible et `ParamMapper` est testable seul. |

### 4.2 Lire le nom des paramètres Java par réflexion

`params[i].getName()` ne retourne `nom` / `age` **que si** le code a été compilé avec
`-parameters`. Sinon Java renvoie `arg0`, `arg1`... et plus rien ne correspond à l'URL.

Activé dans les deux `pom.xml` (framework **et** projet test, car c'est le projet test qui
compile les controllers) :

```xml
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <parameters>true</parameters>
  </configuration>
</plugin>
```

> Symptôme si oublié : tous les paramètres arrivent à `null` / `0` sans aucune erreur.
> Premier réflexe en cas de bug : vérifier ce flag.

### 4.3 `convert` — passer de `String` au type attendu

```java
static Object convert(String value, Class<?> type) {
    if (value == null || (value.isEmpty() && type != String.class)) {
        return defaultValue(type);
    }
    try {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value.trim());
        if (type == double.class || type == Double.class) return Double.parseDouble(value.trim());
        // ... long, float, short, byte, boolean, char
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Valeur '" + value + "' invalide pour le type " + type.getSimpleName(), e);
    }
    return null;
}
```

Types supportés : `String`, `int`, `long`, `double`, `float`, `short`, `byte`, `boolean`,
`char` et leurs wrappers.

**Paramètre absent ou vide → `defaultValue(type)`**

```java
static Object defaultValue(Class<?> type) {
    if (!type.isPrimitive()) return null;      // Integer, String, objets → null
    if (type == boolean.class) return false;
    if (type == int.class) return 0;           // etc.
    ...
}
```

Pourquoi ? `Method.invoke` avec `null` pour un paramètre **primitif** lève
`IllegalArgumentException`. Donc pour `int age` absent il faut fournir `0`, pas `null`.
Pour un `Integer age` on garde `null`, ce qui permet de distinguer « non fourni » de « 0 ».

**Valeur invalide (`age=abc`) → `IllegalArgumentException` → 500**

On préfère une erreur franche à un `0` silencieux : renvoyer 0 pour `abc` masquerait une
faute du client. Le message cite la valeur et le type pour faciliter le debug. L'exception
est attrapée par le `catch (Exception e)` de `processRequest` qui appelle
`sendInternalServerError`.

`boolean` : `Boolean.parseBoolean` ne lève jamais d'exception (tout ce qui n'est pas
`"true"` donne `false`) — comportement standard Java, conservé tel quel.

### 4.4 `FrontController` — branchement

```java
// 5. Construction des arguments de la méthode
Object[] methodArgs = ParamMapper.mapParameters(targetMethod, request, model);

// 6. Invocation du contrôleur
Object result = targetMethod.invoke(controllerInstance, methodArgs);
```

On supprime `buildMethodArguments` : `ParamMapper` fait strictement plus (il gère aussi
`Modelmaison`), garder les deux aurait créé deux sources de vérité.

## 5. Projet de test

- `MonControleur.hello(String nom, int age, Modelmaison model)` (voir §2).
- Vue `WEB-INF/views/hello.jsp` : `<h1>${message}</h1>`.
- `deploy.sh` : `APP_NAME="Sprint7"`.

## 6. Cas de test

| Requête | Résultat |
|---|---|
| `/hello?nom=Alice&age=25` | `Bonjour Alice, tu as 25 ans` |
| `/hello?nom=Alice` | `Bonjour Alice, tu as 0 ans` (défaut du primitif) |
| `/hello?age=25` | `Bonjour null, tu as 25 ans` (`String` absente → `null`) |
| `/hello?nom=Alice&age=abc` | 500, `Valeur 'abc' invalide pour le type int` dans les logs |
| `/hello?age=25&nom=Alice` | identique au 1er : l'ordre dans l'URL n'a pas d'importance |

## 7. Limites connues

- Les noms Java doivent être identiques aux noms HTTP (pas d'annotation `@Param("autre-nom")`).
- Pas de tableaux / listes (`?id=1&id=2`) : seule la première valeur est lue.
- `FrontController.findMethodByName` retrouve la méthode **par son nom** : deux surcharges
  d'une même méthode de controller ne sont pas supportées.

## 8. Déploiement

```bash
cd framework/framework && mvn clean package          # produit target/sprint-framework.jar
cd ../../test/Servlet-Hello && ./deploy.sh           # WAR Sprint7 → /opt/tomcat11/webapps
curl "http://localhost:8080/Sprint7/hello?nom=Alice&age=25"
```
