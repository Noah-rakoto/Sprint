package com.sprint;

import com.sprint.utils.Loader;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class FrontController extends HttpServlet {
    List<String> listeController = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        String scanPackage = getInitParameter("scan-package");
        if (scanPackage == null || scanPackage.isBlank()) {
            scanPackage = "com.test";
        }

        Loader loader = new Loader();
        listeController = loader.loadControllers(scanPackage);

        System.out.println("[Framework] Controllers trouvés dans " + scanPackage + " :");
        if (listeController.isEmpty()) {
            System.out.println("[Framework]   Aucun controller annoté @Controller trouvé");
        } else {
            for (String controller : listeController) {
                System.out.println("[Framework]   - " + controller);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getRequestURL().toString();
        System.out.println("[Framework] URL: " + url);
        response.getWriter().println("URL reçue: " + url);
        response.getWriter().println("Controllers détectés:");
        for (String controller : listeController) {
            response.getWriter().println(controller);
        }
    }

}
