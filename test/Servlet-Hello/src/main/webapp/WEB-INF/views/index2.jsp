<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<% for (String item : (List<String>) request.getAttribute("donnees")) { %>
    <p><%= item %></p>
<% } %>