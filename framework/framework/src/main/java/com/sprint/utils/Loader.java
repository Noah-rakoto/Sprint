package com.sprint.utils;

import com.sprint.annotation.Controller;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

public class Loader {
    public List<String> loadControllers(String packageName) {
        List<String> controllerClasses = new ArrayList<>();
        loadControllerFromPackage(packageName, controllerClasses);
        return controllerClasses;
    }

    public void loadControllerFromPackage(String packageName, List<String> controllerClasses) {
        try {
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            // contveritir en list
            List<URL> resourceList = new ArrayList<>();

            while (resources.hasMoreElements()) {
                resourceList.add(resources.nextElement());
            }
            for (URL resource : resourceList) {
                File directory = new File(decode(resource.getFile()));
                if (directory.exists()) {
                    scanDirectory(packageName, directory, controllerClasses);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isController(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class);
    }

    private void scanDirectory(String packageName, File directory, List<String> controllerClasses) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(packageName + "." + file.getName(), file, controllerClasses);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (isController(clazz)) {
                            controllerClasses.add(className);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
