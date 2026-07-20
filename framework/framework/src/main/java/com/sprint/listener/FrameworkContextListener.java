package com.sprint.listener;

import com.sprint.utils.Loader;
import com.sprint.utils.Mapping;
import com.sprint.utils.RouteScanner;
import com.sprint.utils.UrlKey;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

public class FrameworkContextListener implements ServletContextListener {
    public static final String ROUTES_ATTRIBUTE = "sprint.routes";

    private final Map<UrlKey, Mapping> routes = new HashMap<>();

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String scanPackage = context.getInitParameter("scan-package");
        if (scanPackage == null || scanPackage.isBlank()) {
            scanPackage = "com.test";
        }

        try {
            initialize(context, scanPackage);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        routes.clear();
        sce.getServletContext().removeAttribute(ROUTES_ATTRIBUTE);
    }

    public void initialize(ServletContext context, String scanPackage) throws ServletException {
        routes.clear();
        RouteScanner routeScanner = new RouteScanner(new Loader());
        routeScanner.scanRoutes(scanPackage, routes);
        context.setAttribute(ROUTES_ATTRIBUTE, routes);
    }

    public Map<UrlKey, Mapping> getRoutes() {
        return routes;
    }
}
