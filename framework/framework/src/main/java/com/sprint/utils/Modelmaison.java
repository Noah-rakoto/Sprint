package com.sprint.utils;

import java.util.HashMap;
import java.util.Map;

public class Modelmaison {
    private String view;
    private Map<String, Object> attributes = new HashMap<>();

    public Modelmaison() {
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() { // <-- Ne pas oublier le getter pour le FrontController !
        return view;
    }
}