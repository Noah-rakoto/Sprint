package com.sprint.utils;

import com.sprint.annotation.Methode;
import jakarta.servlet.ServletException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class RouteScanner {
    private final Loader loader;

    public RouteScanner(Loader loader) {
        this.loader = loader;
    }

    public void scanRoutes(String packageName, Map<UrlKey, Mapping> mappings) throws ServletException {
        List<String> listeController = loader.loadControllers(packageName);

        System.out.println("[Framework] Scan des méthodes des controllers...");
        for (String controller : listeController) {
            try {
                Class<?> clazz = Class.forName(controller);
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(Methode.class)) {
                        continue;
                    }

                    Methode annotation = method.getAnnotation(Methode.class);
                    String url = annotation.value();
                    String httpMethod = annotation.type();
                    UrlKey routeKey = new UrlKey(url, httpMethod);
                    String signature = clazz.getName() + "." + method.getName() + "()";
                    Mapping previous = mappings.putIfAbsent(routeKey,
                            new Mapping(controller, method.getName(), url, httpMethod));

                    if (previous != null) {
                        throw new ServletException(
                                "Erreur: Deux méthodes sont mappées sur la même route [" + httpMethod + "] " + url
                                        + " : " + previous.getClassName() + "." + previous.getMethodName() + "() et "
                                        + signature);
                    }

                    System.out.println("[Framework] Route enregistrée : [" + httpMethod + "] " + url + " -> "
                            + clazz.getSimpleName() + "." + method.getName() + "()");
                }
            } catch (Exception e) {
                if (e instanceof ServletException) {
                    throw (ServletException) e;
                }
                throw new ServletException("Erreur lors du scan du controller: " + controller, e);
            }
        }
    }
}
