package com.sprint;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ProcessRequest extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String url = request.getRequestURL().toString();
        System.out.println("[Framework] URL: " + url);
        response.getWriter().println("URL reçue: " + url);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String url = request.getRequestURL().toString();
        System.out.println("[Framework] URL: " + url);
        response.getWriter().println("URL reçue: " + url);
    }
}