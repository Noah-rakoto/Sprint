package com.sprint.utils;

public class Mapping {
    private String className;
    private String methodName;
    private String url;
    private String httpMethod;

    public Mapping(String className, String methodName, String url, String httpMethod) {
        this.className = className;
        this.methodName = methodName;
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}