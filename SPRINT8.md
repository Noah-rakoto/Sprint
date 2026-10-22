# Sprint 8 — Upload de fichiers (MapFile)

> La base (un fichier, `MapFile`) a été livrée dans la PR #13 sous le nom « Sprint 7 File ».
> Ce sprint la complète : type MIME et envoi de plusieurs fichiers d'un coup.

## 1. Objectif

Le développeur reçoit un fichier envoyé par un formulaire sans toucher à `Part` ni à `HttpServletRequest` :

```java
@Methode(value = "/upload", type = "POST")
public Modelmaison upload(MapFile fichier, Modelmaison model) { ... }

@Methode(value = "/addEmpPhoto", type = "POST")
public Modelmaison addEmpPhoto(Employe emp, Modelmaison model) { ... }  // Employe a un champ MapFile photo

@Methode(value = "/uploadMulti", type = "POST")
public Modelmaison uploadMulti(MapFile[] fichiers, Modelmaison model) { ... }  // <input type="file" multiple>
```

## 2. MapFile (framework)

```java
public class MapFile {
    private String name;       // "photo"
    private String extension;    // "png"
    private String contentType;  // "image/png"
    private byte[] bytes;        // contenu réel du fichier
}
```

`byte[]` contient le contenu du fichier : sans lui on ne peut ni l'écrire sur disque, ni le mettre en base.
Le navigateur ne garde rien, le framework doit lire le fichier pendant la requête.

`MapFile.fromPart(Part)` construit l'objet : il enlève le chemin éventuel (`C:\...\photo.png`),
sépare nom et extension, récupère le type MIME (`part.getContentType()`), puis lit les octets. Il renvoie `null` si aucun fichier n'a été choisi.

## 3. Ce qui a changé dans le framework

| Fichier | Changement |
|---|---|
| `FrontController` | `@MultipartConfig` : active `request.getPart()` |
| `MapFile` | `fromPart(Part)` + `getFileName()` + **`contentType`** (Sprint 8) |
| `ParamMapper` | un argument `MapFile` est lu avec `request.getPart(nomDuParametre)` |
| `ParamMapper.buildBean` | un champ `MapFile` d'un objet est lu avec `request.getPart(nomDuChamp)` |
| `ParamMapper.readFiles` | **Sprint 8** : un argument ou un champ `MapFile[]` reçoit tous les fichiers de même nom |

### Plusieurs fichiers (`MapFile[]`)

Avec `<input type="file" name="fichiers" multiple>`, le navigateur envoie **une Part par fichier**,
toutes avec le même nom. `request.getPart(name)` n'en renverrait qu'une, donc on parcourt toutes les Parts :

```java
for (Part part : request.getParts()) {
    if (part.getName().equals(name)) {
        MapFile mf = MapFile.fromPart(part);
        if (mf != null) files.add(mf);
    }
}
```

Si aucun fichier n'est envoyé, on renvoie un **tableau vide** (pas `null`) : le contrôleur peut faire
`for (MapFile f : fichiers)` sans vérifier.

Comme pour les autres paramètres, c'est le **nom** qui fait le lien :
`<input type="file" name="photo">` → champ `photo` ou paramètre `MapFile photo`.

## 4. Formulaire (projet test : `formFile.jsp`)

```html
<form action="${pageContext.request.contextPath}/addEmpPhoto" method="post" enctype="multipart/form-data">
    <input type="text" name="nom">
    <input type="file" name="photo">
    <button type="submit">Envoyer</button>
</form>
```

## 5. Sauvegarde côté développeur

```java
Files.write(Path.of("uploads", file.getFileName()), file.getBytes());          // sur disque
ps.setBytes(2, emp.getPhoto().getBytes());                                       // en base (BYTEA / BLOB)
```

## 6. Pièges

- Sans `enctype="multipart/form-data"` et `method="post"`, le navigateur n'envoie que le nom du fichier : le `MapFile` reste `null`.
- En multipart, `request.getParameter(...)` marche toujours pour les champs texte.
- Sans `@MultipartConfig` (ou `<multipart-config/>` dans le `web.xml`), `getPart()` lève une exception.

## 7. Tests

Formulaires : `http://localhost:8081/Sprint8/formFile`

```bash
curl -F fichier=@photo.png http://localhost:8081/Sprint8/upload
# Fichier recu : photo.png (image/png, ... octets)
curl -F fichiers=@a.txt -F fichiers=@b.png http://localhost:8081/Sprint8/uploadMulti
# 2 fichier(s) recu(s) : a.txt (text/plain) b.png (image/png)
curl -F nom=Bob -F mail=bob@test.com -F numero=42 -F photo=@a.txt http://localhost:8081/Sprint8/addEmpPhoto
```
