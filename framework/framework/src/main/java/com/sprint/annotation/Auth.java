package com.sprint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Sprint 10 : la méthode n'est accessible qu'à un utilisateur connecté
// Sprint 10 bis : roles non vide => le rôle en session doit faire partie de la liste
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
    String[] roles() default {};
}
