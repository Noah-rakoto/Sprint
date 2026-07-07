package com.sprint;

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

public class FrontController extends HttpServlet {
    private Map<UrlKey, Mapping> listeMapping = new HashMap<>();
    private static final String DEFAULT_SCAN_PACKAGE = "com.test";
    private static final String VIEW_BASE_PATH = "/WEB-INF/views/";

    @Override
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
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
            // 2. Résolution de la méthode du contrôleur
            Class<?> clazz = Class.forName(match.getClassName());
            Method targetMethod = findMethodByName(clazz, match.getMethodName());

            // 3. Instanciation du contrôleur et préparation du modèle
            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
            Modelmaison model = new Modelmaison(); // On crée le sac à dos par défaut

            // 4. Construction des arguments de la méthode
            Object[] methodArgs = buildMethodArguments(targetMethod, model);

            // 5. Invocation du contrôleur
            Object result = targetMethod.invoke(controllerInstance, methodArgs);

            // 6. Extraction des données du modèle vers la request standard
            transferAttributesToRequest(model, request);

            // 7. Traitement du résultat (redirection vers la vue)
            handleMethodResult(result, request, response);

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
            Map<String, Object> attributes = model.getAttributes();
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    private void sendNotFoundError(HttpServletResponse response, UrlKey routeKey) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().println(
                "Aucune route trouvée pour: [" + routeKey.getHttpMethod() + "] " + routeKey.getUrl());
    }

    private void sendInternalServerError(HttpServletResponse response, String route, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().println("Erreur lors de l'exécution de la route: " + route);
        e.printStackTrace();
    }

    private void handleMethodResult(Object result, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (result instanceof String) {
            forwardToView((String) result, request, response);
            return;
        }

        // Gestion si la méthode renvoie autre chose (ex: void ou JSON plus tard)
        response.getWriter().println("Retour de la méthode: " + String.valueOf(result));
    }

    private void forwardToView(String viewName, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String jspPath = VIEW_BASE_PATH + viewName + ".jsp";

        request.getRequestDispatcher(jspPath).forward(request, response);
    }

}
