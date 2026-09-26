package com.sprint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sprint.annotation.ApiRest;
import com.sprint.listener.FrameworkContextListener;
import com.sprint.utils.Mapping;
import com.sprint.utils.Modelmaison;
import com.sprint.utils.UrlKey;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.context.*;

public class FrontController extends HttpServlet {
    private Map<UrlKey, Mapping> listeMapping = new HashMap<>();
    private static final String DEFAULT_SCAN_PACKAGE = "com.test";
    private static final String VIEW_BASE_PATH = "/WEB-INF/views/";
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void init() throws ServletException {
        super.init();
        String scanPackage = getInitParameter("scan-package");
        if (scanPackage == null || scanPackage.isBlank()) {
            scanPackage = DEFAULT_SCAN_PACKAGE;
        }

        ServletContext context = getServletContext();
        FrameworkContextListener frameworkContextListener = new FrameworkContextListener();
        frameworkContextListener.initialize(context, scanPackage);
        listeMapping = frameworkContextListener.getRoutes();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // 1. Extraction et recherche de la route
        UrlKey routeKey = extractRouteKey(request);
        Mapping match = listeMapping.get(routeKey);

        System.out.println("[Framework] Requête reçue: " + routeKey.getHttpMethod() + " " + routeKey.getUrl());

        if (match == null) {
            sendNotFoundError(response, routeKey);
            return;
        }

        try {
            // 2. Résolution de la classe du contrôleur
            Class<?> clazz = Class.forName(match.getClassName());
            Method targetMethod = findMethodByName(clazz, match.getMethodName());

            // 3. Changement majeur : Instanciation manuelle ET injection Spring après coup
            // Étape 3.a : On crée l'objet nous-mêmes par réflexion
            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

            // Étape 3.b : On récupère le contexte Spring
            ServletContext servletContext = getServletContext();
            WebApplicationContext springContext = org.springframework.web.context.support.WebApplicationContextUtils
                    .getRequiredWebApplicationContext(servletContext);

            // Étape 3.c : LA LIGNE MAGIQUE qui va injecter ton UtilisateurService (avec son
            // @Autowired)
            springContext.getAutowireCapableBeanFactory().autowireBean(controllerInstance);

            // 4. Préparation du modèle
            Modelmaison model = new Modelmaison();

            // 5. Construction des arguments de la méthode
            Object[] methodArgs = buildMethodArguments(targetMethod, model);

            // 6. Invocation du contrôleur
            Object result = targetMethod.invoke(controllerInstance, methodArgs);

            // 7. Traitement du résultat (redirection vers la vue)
            handleMethodResult(result, model, clazz, request, response);

        } catch (Exception e) {
            sendInternalServerError(response, routeKey.getUrl(), e);
        }
    }

    private UrlKey extractRouteKey(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String route = uri.substring(contextPath.length());
        String webMethod = request.getMethod();
        return new UrlKey(route, webMethod);
    }

    private Method findMethodByName(Class<?> clazz, String methodName) throws NoSuchMethodException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        throw new NoSuchMethodException("Méthode " + methodName + " introuvable dans " + clazz.getName());
    }

    private Object[] buildMethodArguments(Method method, Modelmaison model) {
        int paramCount = method.getParameterCount();
        Object[] methodArgs = new Object[paramCount];
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < paramCount; i++) {
            if (parameterTypes[i].equals(Modelmaison.class)) {
                methodArgs[i] = model; // On injecte notre modèle s'il est attendu
            }
        }
        return methodArgs;
    }

    private void transferAttributesToRequest(Modelmaison model, HttpServletRequest request) {
        if (model != null && model.getAttributes() != null) {
            // On transfère TOUT sans exception
            for (Map.Entry<String, Object> entry : model.getAttributes().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    private void sendNotFoundError(HttpServletResponse response, UrlKey routeKey) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(
                "Aucune route trouvée pour: [" + routeKey.getHttpMethod() + "] " + routeKey.getUrl());
    }

    private void sendInternalServerError(HttpServletResponse response, String route, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("Erreur lors de l'exécution de la route: " + route);
        e.printStackTrace();
    }

    private void handleMethodResult(Object result, Modelmaison injectedModel, Class<?> controllerClass,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // -- Cas @ApiRest : toujours du JSON, peu importe le type de retour --
        if (controllerClass.isAnnotationPresent(ApiRest.class)) {
            Object payload;
            if (result instanceof Modelmaison) {
                // On expose les attributs du modèle comme objet JSON
                payload = ((Modelmaison) result).getAttributes();
            } else {
                // String, Integer, liste, objet métier… tout passe par Jackson
                payload = result;
            }
            String json = objectMapper.writeValueAsString(payload);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);
            return;
        }

        // -- Comportement MVC classique (non-@ApiRest) --

        // Si la méthode renvoie un String (ex: return "users";)
        if (result instanceof String) {
            transferAttributesToRequest(injectedModel, request);
            forwardToView((String) result, request, response);
            return;
        }

        // Si la méthode renvoie le Modelmaison (comme ton getAll)
        if (result instanceof Modelmaison) {
            Modelmaison returnedModel = (Modelmaison) result;
            transferAttributesToRequest(returnedModel, request);

            // LA MODIFICATION : On utilise le getter propre !
            String viewName = returnedModel.getView();

            if (viewName != null && !viewName.isBlank()) {
                forwardToView(viewName, request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Le Modelmaison retourné doit avoir une vue définie via setView().");
            return;
        }

        // Si la méthode renvoie null ou void
        if (result == null) {
            transferAttributesToRequest(injectedModel, request);

            // On regarde si la méthode a modifié le modèle injecté en paramètre
            String viewName = injectedModel.getView();
            if (viewName != null && !viewName.isBlank()) {
                forwardToView(viewName, request, response);
            }
            return;
        }

        response.getWriter().println("Retour de la méthode: " + String.valueOf(result));
    }

    private void forwardToView(String viewName, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String jspPath = VIEW_BASE_PATH + viewName + ".jsp";

        request.getRequestDispatcher(jspPath).forward(request, response);
    }

}
