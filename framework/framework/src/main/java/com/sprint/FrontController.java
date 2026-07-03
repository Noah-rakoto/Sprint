package com.sprint;

import com.sprint.listener.FrameworkContextListener;
import com.sprint.utils.Mapping;
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

    @Override
    public void init() throws ServletException {
        super.init();
        String scanPackage = getInitParameter("scan-package");
        if (scanPackage == null || scanPackage.isBlank()) {
            scanPackage = "com.test";
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
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String route = uri.substring(contextPath.length());

        String webMethod = request.getMethod(); // Renvoie "GET" ou "POST"
        UrlKey routeKey = new UrlKey(route, webMethod);

        System.out.println("[Framework] Requête reçue: " + webMethod + " " + uri);

        response.setContentType("text/plain;charset=UTF-8");

        Mapping match = listeMapping.get(routeKey);

        // Si on a trouvé un match, on l'exécute
        if (match != null) {
            try {
                Class<?> clazz = Class.forName(match.getClassName());
                Method method = clazz.getDeclaredMethod(match.getMethodName());
                Object result = method.invoke(clazz.getDeclaredConstructor().newInstance());

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter()
                        .println("Exécution de la méthode: " + clazz.getSimpleName() + "." + method.getName() + "()");
                response.getWriter().println("Route associée: " + match.getUrl() + " [" + match.getHttpMethod() + "]");
                response.getWriter().println("Retour de la méthode: " + String.valueOf(result));
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

}
