package com.sprint.utils;

import java.util.Locale;
import java.util.Objects;

public class UrlKey {
    private final String url;
    private final String httpMethod;

    public UrlKey(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod == null ? null : httpMethod.toUpperCase(Locale.ROOT);
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UrlKey other)) {
            return false;
        }
        return Objects.equals(url, other.url) && Objects.equals(httpMethod, other.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }
}
