<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
    <h1>${message}</h1>
    <%-- Lu directement dans la HttpSession : prouve que MySession a bien ete recopiee --%>
    <p>Session : user = ${sessionScope.user}, visites = ${sessionScope.visites}</p>
    <a href="${pageContext.request.contextPath}/session/login?nom=Bob">Login</a> |
    <a href="${pageContext.request.contextPath}/session/profil">Profil</a> |
    <a href="${pageContext.request.contextPath}/session/logout">Logout</a>
</body>
</html>
