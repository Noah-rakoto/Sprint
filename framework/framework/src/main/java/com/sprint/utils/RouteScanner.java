package com.sprint.utils;

import com.sprint.annotation.Methode;
import jakarta.servlet.ServletException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RouteScanner {
    private final Loader loader;

    public RouteScanner(Loader loader) {
        this.loader = loader;
    }

    public List<Mapping> scanRoutes(String packageName) throws ServletException {
        List<String> listeController = loader.loadControllers(packageName);
        List<Mapping> mappings = new ArrayList<>();
        Map<String, String> routeIndex = new HashMap<>();

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
                    String routeKey = buildRouteKey(url, httpMethod);
                    String signature = clazz.getName() + "." + method.getName() + "()";
                    String previous = routeIndex.putIfAbsent(routeKey, signature);

                    if (previous != null) {
                        throw new ServletException(
                                "Erreur: Deux méthodes sont mappées sur la même route [" + httpMethod + "] " + url
                                        + " : " + previous + " et " + signature);
                    }

                    Mapping mapping = new Mapping(controller, method.getName(), url, httpMethod);
                    mappings.add(mapping);

                    System.out.println("[Framework] Route enregistrée : [" + httpMethod + "] " + url + " -> "
                            + clazz.getSimpleName() + "." + method.getName() + "()");
                }
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException(
                        "Erreur lors du scan du controller: " + controller, e);
            }
        }

        return mappings;
    }

    private String buildRouteKey(String url, String httpMethod) {
        return url + "#" + httpMethod.toUpperCase(Locale.ROOT);
    }
}
