package com.sprint.utils;

import com.sprint.annotation.Methode;
import java.lang.reflect.Method;

public class GetterMethode {

    public void scannerUneClasse(Class<?> clazz) {
        Method[] methodes = clazz.getDeclaredMethods();

        for (Method m : methodes) {
            if (m.isAnnotationPresent(Methode.class)) {
                Methode annotation = m.getAnnotation(Methode.class);
                String methodeName = m.getName();
                String route = annotation.value();
                System.out.println("-> Fonction trouvée : " + methodeName + "()");
                System.out.println(" Route associée : " + route);
            }
        }
    }
}