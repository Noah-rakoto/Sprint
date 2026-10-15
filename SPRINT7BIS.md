# Sprint 7 bis — Objets personnalisés en argument

## 1. Problème de départ

Le Sprint 7 gère `hello(String nom, int age)`. Dès qu'un formulaire a 10 champs, la signature
devient ingérable (`addEmploye(String nom, String mail, int numero, ...)`). On veut passer
**un seul objet** et laisser le framework le remplir.

## 2. Objectif

```java
@Methode(value = "/addEmploye", type = "GET")
public Modelmaison addEmploye(Employe emp, Modelmaison model) {
    model.addAttribute("message", "Employe ajoute : " + emp.getNom() + " <" + emp.getMail() + "> #" + emp.getNumero());
    model.setView("hello");
    return model;
}
```

`GET /addEmploye?nom=Bob&mail=bob@test.com&numero=42` équivaut à :

```java
Employe emp = new Employe();
emp.setNom("Bob");
emp.setMail("bob@test.com");
emp.setNumero(42);
```

## 3. Le code, et pourquoi on l'a fait comme ça

Fichier : `framework/.../com/sprint/utils/ParamMapper.java` (seul fichier du framework modifié).

### 3.1 Distinguer « type simple » et « bean »

```java
private static boolean isSimpleType(Class<?> type) {
    return type.isPrimitive() || type == String.class
            || Number.class.isAssignableFrom(type)
            || type == Boolean.class || type == Character.class;
}
```

La boucle de `mapParameters` devient :

```java
if (type.equals(Modelmaison.class)) {
    args[i] = model;
} else if (isSimpleType(type)) {
    args[i] = convert(request.getParameter(params[i].getName()), type);   // Sprint 7
} else {
    args[i] = buildBean(type, request);                                   // Sprint 7 bis
}
```

**Pourquoi une liste « simple » et pas « tout le reste = bean » en dur ?** Un type est soit
convertible depuis une `String` (feuille), soit à construire champ par champ (bean). Ce test
sert aussi dans `buildBean` pour ignorer les champs qu'on ne sait pas remplir. L'ordre des
`if` compte : `Modelmaison` n'est ni simple ni un bean venant de la requête, il est testé en 1er.

### 3.2 `buildBean` — construire l'objet par réflexion

```java
private static Object buildBean(Class<?> type, HttpServletRequest request) {
    try {
        Object bean = type.getDeclaredConstructor().newInstance();       // (1)
        for (Field field : type.getDeclaredFields()) {                   // (2)
            if (Modifier.isStatic(field.getModifiers()) || !isSimpleType(field.getType())) {
                continue;
            }
            String raw = request.getParameter(field.getName());         // (3)
            if (raw == null) continue;                                   // (4)

            Object value = convert(raw, field.getType());                // (5)
            String setterName = "set" + Character.toUpperCase(field.getName().charAt(0))
                    + field.getName().substring(1);
            try {
                type.getMethod(setterName, field.getType()).invoke(bean, value);   // (6)
            } catch (NoSuchMethodException e) {
                field.setAccessible(true);
                field.set(bean, value);                                  // (7)
            }
        }
        return bean;
    } catch (ReflectiveOperationException e) {
        throw new IllegalArgumentException(
                "Impossible de construire " + type.getName() + " depuis la requête", e);
    }
}
```

| # | Décision | Pourquoi |
|---|---|---|
| 1 | Constructeur **sans argument** obligatoire | Le framework ne peut pas deviner quels paramètres passer à un constructeur. Même convention que JavaBean / Spring / Jackson. Sans lui : `IllegalArgumentException` → 500 avec le nom de la classe. |
| 2 | On itère sur les **champs de la classe**, pas sur les paramètres HTTP | Même principe qu'au Sprint 7 : la classe cible dicte ce qui est rempli. Un paramètre HTTP inconnu (`?admin=true`) est ignoré, il ne peut pas écrire dans un champ qui n'existe pas. |
| 2 | Champs `static` sautés | Un champ statique appartient à la classe, pas à l'instance : le modifier depuis une requête partagerait l'état entre tous les utilisateurs. |
| 3 | Nom du champ = nom du paramètre HTTP | Convention simple, pas besoin du flag `-parameters` ici (les noms de champs sont toujours disponibles). |
| 4 | Paramètre absent → champ non touché | Le champ garde sa valeur initiale (`null`, `0`, ou valeur définie dans la classe). On n'écrase pas une valeur par défaut volontaire par `null`. |
| 5 | Réutilise `convert` du Sprint 7 | Une seule logique de conversion : `numero=42` → `int` marche pareil pour un argument et pour un champ de bean. |
| 6 | Setter en priorité | Respecte l'encapsulation : si le setter valide ou transforme la valeur, cette logique est exécutée. |
| 7 | Repli sur le champ privé (`setAccessible(true)`) | Permet un bean sans setters. Contourne l'encapsulation, d'où le setter d'abord. |

### 3.3 Le bean de test

```java
package com.test.model;

public class Employe {
    private String nom;
    private String mail;
    private int numero;

    // constructeur par défaut implicite (obligatoire pour buildBean)
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    // ... getMail/setMail, getNumero/setNumero
}
```

## 4. Mélanger les types dans une même signature

```java
public Modelmaison m(Employe emp, String source, Modelmaison model)
```

Chaque argument est traité indépendamment par `mapParameters` :
`emp` ← bean, `source` ← `?source=...`, `model` ← injecté par le framework.
Attention : un même nom HTTP peut alimenter à la fois un argument simple et un champ de bean.
Avec `m(Employe emp, String nom)`, `?nom=Bob` remplit `emp.nom` **et** l'argument `nom`.

## 5. Cas de test

| Requête | Résultat |
|---|---|
| `/addEmploye?nom=Bob&mail=bob@test.com&numero=42` | `Employe ajoute : Bob <bob@test.com> #42` |
| `/addEmploye?nom=Bob` | `Employe ajoute : Bob <null> #0` (champs absents laissés par défaut) |
| `/addEmploye?nom=Bob&numero=abc` | 500, `Valeur 'abc' invalide pour le type int` |
| `/addEmploye?nom=Bob&role=admin` | `role` ignoré (pas de champ `role`) |
| `/hello?nom=Alice&age=25` | route du Sprint 7, inchangée |

## 6. Limites connues

- Pas de beans imbriqués (`adresse.ville`) ni de collections : les champs non simples sont
  ignorés silencieusement.
- Les champs des classes parentes ne sont pas remplis : `getDeclaredFields()` ne remonte pas
  l'héritage.
- Aucune validation (champ obligatoire, format d'e-mail...).
- Les routes sont en `GET` pour faciliter les tests dans le navigateur. En `POST`
  (`application/x-www-form-urlencoded`) `getParameter` fonctionne de la même manière.

## 7. Déploiement

```bash
cd framework/framework && mvn clean package
cd ../../test/Servlet-Hello && ./deploy.sh      # WAR Sprint7bis → /opt/tomcat11/webapps
curl "http://localhost:8080/Sprint7bis/addEmploye?nom=Bob&mail=bob@test.com&numero=42"
```
