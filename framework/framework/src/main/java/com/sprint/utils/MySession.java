package com.sprint.utils;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Session vue par le contrôleur : une simple Map, sans dépendance à HttpSession.
 * Le framework la remplit avant l'appel du contrôleur et recopie ses changements
 * dans la vraie HttpSession après l'appel.
 */
public class MySession {
    private Map<String, Object> sessionData = new HashMap<>();

    public MySession() {}

    public Map<String, Object> getSessionData() {
        return sessionData;
    }

    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public void setAttribute(String key, Object value) {
        sessionData.put(key, value);
    }

    public void removeAttribute(String key) {
        sessionData.remove(key);
    }

    /** Vide la session (ex : déconnexion). */
    public void destroy() {
        sessionData.clear();
    }

    /** Copie tous les attributs de la HttpSession dans un nouveau MySession. */
    public static MySession fromHttpSession(HttpSession session) {
        MySession mySession = new MySession();
        for (String name : Collections.list(session.getAttributeNames())) {
            mySession.setAttribute(name, session.getAttribute(name));
        }
        return mySession;
    }

    /**
     * Recopie le contenu dans la HttpSession : les attributs retirés par le
     * contrôleur sont supprimés, les autres sont (re)écrits.
     */
    public void applyTo(HttpSession session) {
        for (String name : Collections.list(session.getAttributeNames())) {
            if (!sessionData.containsKey(name)) {
                session.removeAttribute(name);
            }
        }
        for (Map.Entry<String, Object> entry : sessionData.entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}
