<%@ page import="java.util.List" %>
<%@ page import="com.test.model.Utilisateur" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Liste des Utilisateurs</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>Liste des Utilisateurs</h1>
    
    <% List<Utilisateur> users = (List<Utilisateur>) request.getAttribute("users"); %>
    
    <% if (users != null && !users.isEmpty()) { %>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nom</th>
                    <th>Email</th>
                    <th>Rôle</th>
                    <th>Date d'inscription</th>
                </tr>
            </thead>
            <tbody>
                <% for (Utilisateur user : users) { %>
                    <tr>
                        <td><%= user.getId() %></td>
                        <td><%= user.getNom() %></td>
                        <td><%= user.getEmail() %></td>
                        <td><%= user.getRole() %></td>
                        <td><%= user.getDateInscription() != null ? user.getDateInscription() : "N/A" %></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    <% } else { %>
        <p>Aucun utilisateur trouvé.</p>
    <% } %>
</body>
</html>