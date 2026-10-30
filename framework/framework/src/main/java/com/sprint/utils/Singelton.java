package com.sprint.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Sprint 11 : registre des contrôleurs singleton.
 * La clé est le nom complet de la classe (getName), car deux contrôleurs peuvent
 * porter le même nom simple. FrontController le remplit dans init() uniquement.
 */
public class Singelton {
    private Map<String, Object> singeltonData = new HashMap<>();

    public Singelton() {}

    public Map<String, Object> getSingeltonData() {
        return singeltonData;
    }

    public Object getAttribute(String key) {
        return singeltonData.get(key);
    }

    public void setAttribute(String key, Object value) {
        singeltonData.put(key, value);
    }

    public void removeAttribute(String key) {
        singeltonData.remove(key);
    }

    public Boolean containsAttribute(String key) {
        return singeltonData.containsKey(key);
    }
}
