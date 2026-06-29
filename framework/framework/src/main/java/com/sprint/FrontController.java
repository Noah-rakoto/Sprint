package com.sprint;

import com.sprint.utils.Loader;
import com.sprint.utils.Mapping; // Import de ta nouvelle classe utilitaire
import com.sprint.annotation.Methode;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

public class FrontController extends HttpServlet {
    List<Mapping> listeMapping = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        String scanPackage = getInitParameter("scan-package");
        if (scanPackage == null || scanPackage.isBlank()) {
            scanPackage = "com.test";
        }

        Loader loader = new Loader();
        List<String> listeController = loader.loadControllers(scanPackage);

        // --- NOUVEAUTÉ SPRINT 3 : ON DEVIENT INTELLIGENT AU DÉMARRAGE ---
        System.out.println("[Framework] Scan des méthodes des controllers...");
        for (String controller : listeController) {
            try {
                Class<?> clazz = Class.forName(controller);
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Methode.class)) {
                        Methode annotation = method.getAnnotation(Methode.class);

                        // On extrait l'URL et le type (GET/POST) de l'annotation
                        String url = annotation.value();
                        String httpMethod = annotation.type(); // ex: "GET" ou "POST"

                        // On enregistre cette route complète dans notre liste
                        Mapping mapping = new Mapping(controller, method.getName(), url, httpMethod);
                        listeMapping.add(mapping);

                        System.out.println("[Framework] Route enregistrée : [" + httpMethod + "] " + url + " -> "
                                + clazz.getSimpleName() + "." + method.getName() + "()");
                    }
                }
            } catch (Exception e) {
                System.out.println("[Framework] Erreur lors du scan du controller: " + controller);
                e.printStackTrace();
            }
        }
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
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String route = uri.substring(contextPath.length());

        String webMethod = request.getMethod(); // Renvoie "GET" ou "POST"

        System.out.println("[Framework] Requête reçue: " + webMethod + " " + uri);

        if (serveStaticResource(request, response, route)) {
            return;
        }

        response.setContentType("text/plain;charset=UTF-8");

        Mapping match = null;
        for (Mapping mapping : listeMapping) {
            if (mapping.getUrl().equals(route) && mapping.getHttpMethod().equalsIgnoreCase(webMethod)) {
                match = mapping;
                break;
            }
        }

        // Si on a trouvé un match, on l'exécute
        if (match != null) {
            try {
                Class<?> clazz = Class.forName(match.getClassName());
                Method method = clazz.getDeclaredMethod(match.getMethodName());

                response.getWriter()
                        .println("Exécution de la méthode: " + clazz.getSimpleName() + "." + method.getName() + "()");
                response.getWriter().println("Route associée: " + match.getUrl() + " [" + match.getHttpMethod() + "]");
                return; // On arrête la fonction ici, tout s'est bien passé !

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Erreur lors de l'exécution de la route: " + route);
                e.printStackTrace();
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().println("Aucune route trouvée pour: [" + webMethod + "] " + route);
    }

    private boolean serveStaticResource(HttpServletRequest request, HttpServletResponse response, String route)
            throws ServletException, IOException {
        String targetRoute = route;

        if ("/".equals(route)) {
            targetRoute = "/index.html";
        }

        boolean looksLikeStaticFile = targetRoute.contains(".");
        if (!looksLikeStaticFile && !"/".equals(route)) {
            return false;
        }

        if (request.getServletContext().getResource(targetRoute) == null) {
            return false;
        }

        RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("default");
        if (dispatcher != null) {
            dispatcher.forward(request, response);
            return true;
        }

        request.getRequestDispatcher(targetRoute).forward(request, response);
        return true;
    }
}
