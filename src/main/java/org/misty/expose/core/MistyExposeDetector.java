package org.misty.expose.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class MistyExposeDetector {

    public static List<MistyExpose> findBySPI() {
        ServiceLoader<MistyExpose> mistyExposeServiceLoader = ServiceLoader.load(MistyExpose.class);

        List<MistyExpose> list = new ArrayList<>();
        mistyExposeServiceLoader.forEach(list::add);
        return list;
    }

    public static List<MistyExpose> findBySPI(ClassLoader classLoader) {
        try {
            Class<?> mistyExposeClass = classLoader.loadClass("org.misty.expose.core.MistyExpose");
            ServiceLoader<?> mistyExposeServiceLoader = ServiceLoader.load(mistyExposeClass, classLoader);

            List<MistyExpose> list = new ArrayList<>();
            mistyExposeServiceLoader.forEach((mistyExpose) -> {
                list.add(buildWithSwitchContextClassLoader(mistyExposeClass, mistyExpose));
            });
            return list;

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static MistyExpose buildWithSwitchContextClassLoader(Class<?> mistyExposeClass, Object mistyExposer) {
        String name;
        String version;
        String description;

        Thread currentThread = Thread.currentThread();
        ClassLoader originalContextClassLoader = currentThread.getContextClassLoader();
        try {
            Method getName = mistyExposeClass.getDeclaredMethod("getName");
            Method getVersion = mistyExposeClass.getDeclaredMethod("getVersion");
            Method getDescription = mistyExposeClass.getDeclaredMethod("getDescription");

            currentThread.setContextClassLoader(mistyExposeClass.getClassLoader());

            name = (String) getName.invoke(mistyExposer);
            version = (String) getVersion.invoke(mistyExposer);
            description = (String) getDescription.invoke(mistyExposer);

        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);

        } finally {
            currentThread.setContextClassLoader(originalContextClassLoader);
        }

        return new MistyExpose(name, version, description);
    }

}
