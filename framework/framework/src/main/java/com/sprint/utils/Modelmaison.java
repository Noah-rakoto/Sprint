package com.sprint.utils;

import java.util.HashMap;
import java.util.Map;

public class Modelmaison {
    private Map<String, Object> attributes = new HashMap<>();

    public Modelmaison() {
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}