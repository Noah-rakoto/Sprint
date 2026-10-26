# Sprint 10 bis — Autorisation par rôle (@Auth(roles = ...))

## 1. Problème de départ

Le Sprint 10 distingue seulement « connecté » / « pas connecté ».
Mais une page d'administration ne doit pas être ouverte à n'importe quel utilisateur connecté :
il faut aussi vérifier **son rôle**.

## 2. Objectif

```java
@Auth(roles = { "admin" })
@Methode(value = "/auth/admin", type = "GET")
public Modelmaison admin(MySession session, Modelmaison model) { ... }

@Auth(roles = { "admin", "manager" })   // un des rôles suffit
@Methode(value = "/auth/gestion", type = "GET")
public Modelmaison gestion(MySession session, Modelmaison model) { ... }
```

| Situation | Réponse |
|---|---|
| pas de `@Auth` | accès libre |
| `@Auth`, pas connecté | **401 Unauthorized** |
| `@Auth` sans rôles, connecté | accès |
| `@Auth(roles=...)`, connecté mais rôle absent de la liste | **403 Forbidden** |
| `@Auth(roles=...)`, rôle dans la liste | accès |

401 = « on ne sait pas qui tu es », 403 = « on sait qui tu es, mais tu n'as pas le droit ».

## 3. Comment ça marche

### 3.1 `@Auth` (framework, `annotation/Auth.java`)

```java
public @interface Auth {
    String[] roles() default {};
}
```

`default {}` : un `@Auth` tout court garde le comportement du Sprint 10.

### 3.2 Clé de session du rôle — `web.xml` du projet test

Comme pour l'utilisateur, le nom de l'attribut est choisi par le projet :

```xml
<context-param>
    <param-name>auth.role-session-key</param-name>
    <param-value>role</param-value>
</context-param>
```

Obligatoire : s'il manque, `FrontController.init()` fait échouer le déploiement.

### 3.3 Contrôle dans `FrontController`

```java
Auth auth = targetMethod.getAnnotation(Auth.class);
if (auth != null) {
    if (!isAuthenticated(request))           → 401
    if (!hasRequiredRole(request, auth.roles())) → 403
}

private boolean hasRequiredRole(HttpServletRequest request, String[] roles) {
    if (roles.length == 0) return true;
    Object role = request.getSession(false).getAttribute(roleSessionKey);
    return role != null && Arrays.asList(roles).contains(role.toString());
}
```

L'ordre compte : on vérifie d'abord la connexion (401), ensuite le rôle (403).

## 4. Projet test

| Route | Effet |
|---|---|
| `/auth/login?nom=Alice&role=admin` | met `user` et `role` en session |
| `/auth/prive` | `@Auth` : n'importe quel connecté |
| `/auth/admin` | `@Auth(roles={"admin"})` |
| `/auth/gestion` | `@Auth(roles={"admin","manager"})` |
| `/session/logout` | (Sprint 9) vide la session, rôle compris |

## 5. Tests

```bash
J=cookies.txt; B=http://localhost:8081/Sprint10bis
curl -b $J -c $J -i  $B/auth/admin                              # 401
curl -b $J -c $J    "$B/auth/login?nom=Bob&role=user"
curl -b $J -c $J -i  $B/auth/prive                              # 200
curl -b $J -c $J -i  $B/auth/admin                              # 403
curl -b $J -c $J    "$B/auth/login?nom=Carl&role=manager"
curl -b $J -c $J -i  $B/auth/gestion                            # 200
curl -b $J -c $J -i  $B/auth/admin                              # 403
curl -b $J -c $J    "$B/auth/login?nom=Alice&role=admin"
curl -b $J -c $J -i  $B/auth/admin                              # 200
curl -b $J -c $J     $B/session/logout
curl -b $J -c $J -i  $B/auth/admin                              # 401
```

## 6. Pièges

- La comparaison est sensible à la casse : `Admin` ≠ `admin`.
- Le rôle est comparé via `toString()` : on peut stocker une `String` ou un enum en session.
