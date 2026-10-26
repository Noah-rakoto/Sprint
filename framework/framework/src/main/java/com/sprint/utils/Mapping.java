package com.sprint.utils;

import java.lang.reflect.Method;

/**
 * Route résolue au scan : on garde directement la classe et la Method,
 * pour ne pas les rechercher (Class.forName, recherche par nom) à chaque requête.
 */
public class Mapping {
    private Class<?> controllerClass;
    private Method method;
    private String url;
    private String httpMethod;

    public Mapping(Class<?> controllerClass, Method method, String url, String httpMethod) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public String getClassName() {
        return controllerClass.getName();
    }

    public String getMethodName() {
        return method.getName();
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}
