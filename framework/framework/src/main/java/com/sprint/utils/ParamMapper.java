package com.sprint.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Construit le tableau d'arguments d'une méthode de contrôleur à partir des
 * paramètres de la requête HTTP (le nom du paramètre Java doit correspondre au
 * nom du paramètre HTTP, d'où l'option -parameters à la compilation).
 */
public class ParamMapper {

    /**
     * Mappe les paramètres de la requête HTTP vers les arguments d'une méthode de contrôleur.
     */
    public static Object[] mapParameters(Method method, HttpServletRequest request, HttpServletResponse response,
            Modelmaison model) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();

            if (type.equals(Modelmaison.class)) {
                args[i] = model;
                continue;
            }

            if (type.equals(HttpServletRequest.class)) {
                args[i] = request;
            } else if (type.equals(HttpServletResponse.class)) {
                args[i] = response;
            } else if (type.equals(MySession.class)) {
                args[i] = MySession.fromHttpSession(request.getSession(true));
            } else if (type.equals(MapFile.class)) {
                args[i] = readFile(request, params[i].getName());
            } else if (type.equals(MapFile[].class)) {
                args[i] = readFiles(request, params[i].getName());
            } else if (isSimpleType(type)) {
                args[i] = convert(request.getParameter(params[i].getName()), type);
            } else {
                args[i] = buildBean(type, request, "");
            }
        }
        return args;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class || type == Character.class
                || type == LocalDate.class || type == LocalDateTime.class
                || type.isEnum();
    }

    /**
     * Instancie un bean et remplit ses champs avec les paramètres de requête
     * de même nom (via le setter s'il existe, sinon directement sur le champ).
     * Un champ objet est rempli récursivement depuis les paramètres "champ.sousChamp"
     * (ex : user.nom pour Employe.user.nom) ; le préfixe vaut "" au premier niveau.
     */
    private static Object buildBean(Class<?> type, HttpServletRequest request, String prefix) {
        try {
            Object bean = type.getDeclaredConstructor().newInstance();
            for (Field field : allFields(type)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                String paramName = prefix + field.getName();
                Object value;
                if (field.getType().equals(MapFile.class)) {
                    value = readFile(request, paramName);
                    if (value == null) {
                        continue;
                    }
                } else if (field.getType().equals(MapFile[].class)) {
                    MapFile[] files = readFiles(request, paramName);
                    if (files.length == 0) {
                        continue;
                    }
                    value = files;
                } else if (isSimpleType(field.getType())) {
                    String raw = request.getParameter(paramName);
                    if (raw == null) {
                        continue;
                    }
                    value = convert(raw, field.getType());
                } else if (hasParamStartingWith(request, paramName + ".")) {
                    value = buildBean(field.getType(), request, paramName + ".");
                } else {
                    continue;
                }
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

    /** Champs de la classe et de ses classes mères (getDeclaredFields ne voit pas l'héritage). */
    private static List<Field> allFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    /** Vrai si la requête contient au moins un paramètre ou un fichier dont le nom commence par start. */
    private static boolean hasParamStartingWith(HttpServletRequest request, String start) {
        for (String key : request.getParameterMap().keySet()) {
            if (key.startsWith(start)) {
                return true;
            }
        }
        if (isMultipart(request)) {
            try {
                for (Part part : request.getParts()) {
                    if (part.getName().startsWith(start)) {
                        return true;
                    }
                }
            } catch (IOException | ServletException e) {
                throw new IllegalArgumentException("Impossible de lire la requête multipart", e);
            }
        }
        return false;
    }

    /**
     * Lit le fichier envoyé sous le nom donné ; null si la requête n'est pas
     * multipart ou si aucun fichier n'a été envoyé.
     */
    private static MapFile readFile(HttpServletRequest request, String name) {
        if (!isMultipart(request)) {
            return null;
        }
        try {
            return MapFile.fromPart(request.getPart(name));
        } catch (IOException | ServletException e) {
            throw new IllegalArgumentException("Impossible de lire le fichier '" + name + "'", e);
        }
    }

    /**
     * Lit tous les fichiers envoyés sous le même nom (<input type="file" multiple>) ;
     * tableau vide si la requête n'est pas multipart ou si aucun fichier n'a été envoyé.
     */
    private static MapFile[] readFiles(HttpServletRequest request, String name) {
        if (!isMultipart(request)) {
            return new MapFile[0];
        }
        try {
            List<MapFile> files = new ArrayList<>();
            for (Part part : request.getParts()) {
                if (part.getName().equals(name)) {
                    MapFile mf = MapFile.fromPart(part);
                    if (mf != null) {
                        files.add(mf);
                    }
                }
            }
            return files.toArray(new MapFile[0]);
        } catch (IOException | ServletException e) {
            throw new IllegalArgumentException("Impossible de lire les fichiers '" + name + "'", e);
        }
    }

    private static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
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
            // Formats ISO, ceux des <input type="date"> et <input type="datetime-local">
            if (type == LocalDate.class) return LocalDate.parse(value.trim());
            if (type == LocalDateTime.class) return LocalDateTime.parse(value.trim());
            if (type.isEnum()) return toEnum(value.trim(), type);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Valeur '" + value + "' invalide pour le type " + type.getSimpleName(), e);
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object toEnum(String value, Class<?> type) {
        try {
            return Enum.valueOf((Class<Enum>) type, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Valeur '" + value + "' invalide pour l'enum " + type.getSimpleName(), e);
        }
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
