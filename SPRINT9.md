# Sprint 9 — Gestion de la session (MySession)

## 1. Problème de départ

Jusqu'ici, un contrôleur ne peut rien garder d'une requête à l'autre : chaque appel repart de zéro.
Pour faire un login, un panier ou un compteur de visites, il faut la **session HTTP**.

On pourrait donner `HttpSession` au contrôleur, mais il dépendrait alors de l'API Servlet.
Le but du framework est justement de cacher cette API (comme pour `Modelmaison` et `MapFile`).

## 2. Objectif

Le contrôleur demande simplement un `MySession` en argument :

```java
@Methode(value = "/session/login", type = "GET")
public Modelmaison login(String nom, MySession session, Modelmaison model) {
    session.setAttribute("user", nom);
    model.setView("session");
    return model;
}

@Methode(value = "/session/logout", type = "GET")
public Modelmaison logout(MySession session, Modelmaison model) {
    session.destroy();
    model.setView("session");
    return model;
}
```

## 3. Comment ça marche

```
requête ──► ParamMapper : HttpSession ──copie──► MySession
                                                   │
                         contrôleur : get / set / remove / destroy
                                                   │
           FrontController : MySession ──recopie──► HttpSession ──► vue (JSP)
```

### 3.1 `MySession` (framework, `utils/MySession.java`)

Une simple `Map<String, Object>` :

| Méthode | Rôle |
|---|---|
| `getAttribute(key)` | lire une valeur |
| `setAttribute(key, value)` | écrire une valeur |
| `removeAttribute(key)` | supprimer une valeur |
| `destroy()` | tout vider (déconnexion) |
| `fromHttpSession(HttpSession)` | **framework** : construit un `MySession` avec les attributs de la vraie session |
| `applyTo(HttpSession)` | **framework** : recopie le contenu dans la vraie session |

### 3.2 Avant l'appel — `ParamMapper`

```java
if (type.equals(MySession.class)) {
    args[i] = MySession.fromHttpSession(request.getSession(true));
}
```

`getSession(true)` crée la session si elle n'existe pas encore : Tomcat envoie alors le cookie `JSESSIONID`
au navigateur, qui le renverra à chaque requête suivante.

### 3.3 Après l'appel — `FrontController`

```java
for (Object arg : methodArgs) {
    if (arg instanceof MySession) {
        ((MySession) arg).applyTo(request.getSession(true));
    }
}
```

`applyTo` fait deux choses :
1. supprime de la `HttpSession` les attributs que le contrôleur a retirés (`removeAttribute`, `destroy`) ;
2. écrit tous les attributs restants.

Sans l'étape 1, un `destroy()` ne déconnecterait personne : les anciennes valeurs resteraient dans la vraie session.

**Pourquoi après l'appel et avant la vue ?** La JSP lit la vraie session (`${sessionScope.user}`) :
elle doit déjà contenir les changements du contrôleur au moment du `forward`.

## 4. Projet test

| Route | Effet |
|---|---|
| `/session/login?nom=Bob` | met `user=Bob` et `visites=0` en session |
| `/session/profil` | affiche l'utilisateur et incrémente `visites` |
| `/session/logout` | `destroy()` : vide la session |

La vue `session.jsp` affiche le message du contrôleur **et** la vraie session (`${sessionScope.user}`),
ce qui prouve que la recopie fonctionne.

## 5. Tests

```bash
J=cookies.txt   # garde le cookie JSESSIONID comme un navigateur
curl -b $J -c $J "http://localhost:8081/Sprint9/session/login?nom=Bob"   # Connecte en tant que Bob
curl -b $J -c $J  http://localhost:8081/Sprint9/session/profil           # visite numero 1
curl -b $J -c $J  http://localhost:8081/Sprint9/session/profil           # visite numero 2
curl -b $J -c $J  http://localhost:8081/Sprint9/session/logout           # Deconnecte
curl -b $J -c $J  http://localhost:8081/Sprint9/session/profil           # Personne n'est connecte
curl              http://localhost:8081/Sprint9/session/profil           # sans cookie : Personne n'est connecte
```

## 6. Pièges

- Sans cookie (curl sans `-b/-c`, navigation privée), chaque requête a une nouvelle session.
- Les valeurs sont des `Object` : il faut caster en lisant (`(Integer) session.getAttribute("visites")`).
- Un `MySession` créé à la main avec `new MySession()` n'est **pas** relié à la session : seul celui
  injecté par le framework est recopié.
