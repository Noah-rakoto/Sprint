package com.sprint.utils;

public class UrlMethod {
    private String url;
    private String method;

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean equals(UrlMethod other, String method) {
        return this.url.equals(other.url) && this.method.equals(other.method);
    }

    public String concatenate() {
        return this.url + "?" + this.method;
    }

}
