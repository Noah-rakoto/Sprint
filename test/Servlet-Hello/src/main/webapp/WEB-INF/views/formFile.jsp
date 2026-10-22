<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
    <h2>Fichier seul</h2>
    <form action="${pageContext.request.contextPath}/upload" method="post" enctype="multipart/form-data">
        <input type="file" name="fichier">
        <button type="submit">Envoyer</button>
    </form>

    <h2>Plusieurs fichiers</h2>
    <form action="${pageContext.request.contextPath}/uploadMulti" method="post" enctype="multipart/form-data">
        <input type="file" name="fichiers" multiple>
        <button type="submit">Envoyer</button>
    </form>

    <h2>Employe avec photo</h2>
    <form action="${pageContext.request.contextPath}/addEmpPhoto" method="post" enctype="multipart/form-data">
        <input type="text" name="nom" placeholder="nom">
        <input type="email" name="mail" placeholder="mail">
        <input type="number" name="numero" placeholder="numero">
        <input type="file" name="photo">
        <button type="submit">Envoyer</button>
    </form>
</body>
</html>
