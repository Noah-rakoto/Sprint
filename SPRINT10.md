# Sprint 10 — Authentification (@Auth)

## 1. Problème de départ

Depuis le Sprint 9, on sait qui est connecté (`user` en session).
Mais toutes les routes restent accessibles à tout le monde : pour protéger une page,
chaque contrôleur devrait tester la session lui-même, à la main, partout.

## 2. Objectif

Le développeur marque simplement la méthode avec `@Auth` :

```java
@Auth
@Methode(value = "/auth/prive", type = "GET")
public Modelmaison prive(MySession session, Modelmaison model) {
    model.addAttribute("message", "Page privee : bienvenue " + session.getAttribute("user"));
    model.setView("session");
    return model;
}
```

Si personne n'est connecté, le framework répond **401 Unauthorized** et la méthode n'est **jamais** appelée.

## 3. Comment ça marche

```
requête ──► route trouvée ──► @Auth ? ──non──────────────────────► invocation
                                 │
                                oui ──► session[user-session-key] != null ? ──oui──► invocation
                                                    │
                                                   non ──► 401
```

### 3.1 `@Auth` (framework, `annotation/Auth.java`)

Annotation de méthode (`@Target(METHOD)`), sans attribut pour l'instant.

### 3.2 Clé de session configurable — `web.xml` du projet test

Le framework ne décide pas du nom de l'attribut « utilisateur connecté » : c'est le projet qui le dit.

```xml
<context-param>
    <param-name>auth.user-session-key</param-name>
    <param-value>user</param-value>
</context-param>
```

Lu dans `FrontController.init()`. S'il manque, le déploiement échoue avec un message clair
(mieux vaut planter au démarrage que laisser passer tout le monde).

### 3.3 Contrôle dans `FrontController`

Juste après avoir trouvé la méthode, **avant** d'instancier le contrôleur :

```java
if (targetMethod.isAnnotationPresent(Auth.class) && !isAuthenticated(request)) {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vous devez être connecté ...");
    return;
}

private boolean isAuthenticated(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return session != null && session.getAttribute(userSessionKey) != null;
}
```

`getSession(false)` : on ne crée pas une session vide juste pour vérifier.

## 4. Projet test

| Route | Effet |
|---|---|
| `/auth/public` | toujours accessible |
| `/auth/prive` | `@Auth` : 401 sans connexion, sinon « bienvenue Bob » |
| `/session/login?nom=Bob` | (Sprint 9) met `user=Bob` en session |
| `/session/logout` | (Sprint 9) vide la session |

## 5. Tests

```bash
J=cookies.txt
curl -b $J -c $J -i  http://localhost:8081/Sprint10/auth/public            # 200
curl -b $J -c $J -i  http://localhost:8081/Sprint10/auth/prive             # 401
curl -b $J -c $J    "http://localhost:8081/Sprint10/session/login?nom=Bob"  # connexion
curl -b $J -c $J -i  http://localhost:8081/Sprint10/auth/prive             # 200 : bienvenue Bob
curl -b $J -c $J     http://localhost:8081/Sprint10/session/logout         # déconnexion
curl -b $J -c $J -i  http://localhost:8081/Sprint10/auth/prive             # 401
```

## 6. Suite : Sprint 10 bis

Ajouter les rôles : `@Auth(roles = {"admin"})`, une seconde clé `auth.role-session-key`,
et **403 Forbidden** si l'utilisateur est connecté mais n'a pas le bon rôle.
