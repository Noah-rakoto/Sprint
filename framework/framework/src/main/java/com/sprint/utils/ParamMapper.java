package com.sprint.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * Construit le tableau d'arguments d'une méthode de contrôleur à partir des
 * paramètres de la requête HTTP (le nom du paramètre Java doit correspondre au
 * nom du paramètre HTTP, d'où l'option -parameters à la compilation).
 */
public class ParamMapper {

    public static Object[] mapParameters(Method method, HttpServletRequest request, Modelmaison model) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();

            if (type.equals(Modelmaison.class)) {
                args[i] = model;
                continue;
            }

            if (isSimpleType(type)) {
                args[i] = convert(request.getParameter(params[i].getName()), type);
            } else {
                args[i] = buildBean(type, request);
            }
        }
        return args;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class || type == Character.class;
    }

    /**
     * Instancie un bean et remplit ses champs avec les paramètres de requête
     * de même nom (via le setter s'il existe, sinon directement sur le champ).
     */
    private static Object buildBean(Class<?> type, HttpServletRequest request) {
        try {
            Object bean = type.getDeclaredConstructor().newInstance();
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !isSimpleType(field.getType())) {
                    continue;
                }
                String raw = request.getParameter(field.getName());
                if (raw == null) {
                    continue;
                }
                Object value = convert(raw, field.getType());
                String setterName = "set" + Character.toUpperCase(field.getName().charAt(0))
                        + field.getName().substring(1);
                try {
                    type.getMethod(setterName, field.getType()).invoke(bean, value);
                } catch (NoSuchMethodException e) {
                    field.setAccessible(true);
                    field.set(bean, value);
                }
            }
            return bean;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Impossible de construire " + type.getName() + " depuis la requête", e);
        }
    }

    /** Convertit une chaîne vers le type attendu ; valeur par défaut si absente. */
    static Object convert(String value, Class<?> type) {
        if (value == null || (value.isEmpty() && type != String.class)) {
            return defaultValue(type);
        }
        try {
            if (type == String.class) return value;
            if (type == int.class || type == Integer.class) return Integer.parseInt(value.trim());
            if (type == long.class || type == Long.class) return Long.parseLong(value.trim());
            if (type == double.class || type == Double.class) return Double.parseDouble(value.trim());
            if (type == float.class || type == Float.class) return Float.parseFloat(value.trim());
            if (type == short.class || type == Short.class) return Short.parseShort(value.trim());
            if (type == byte.class || type == Byte.class) return Byte.parseByte(value.trim());
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value.trim());
            if (type == char.class || type == Character.class) return value.charAt(0);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Valeur '" + value + "' invalide pour le type " + type.getSimpleName(), e);
        }
        return null;
    }

    static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0f;
        if (type == short.class) return (short) 0;
        return (byte) 0;
    }
}
