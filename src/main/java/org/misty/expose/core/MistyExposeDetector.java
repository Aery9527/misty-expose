package org.misty.expose.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

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

    public static List<MistyExpose> findBySPIAndCheckRepeated() {
        return findBySPIAndCheckRepeated(MistyExposeDetector::thrownRepeatedException);
    }

    public static List<MistyExpose> findBySPIAndCheckRepeated(Consumer<Map<String, List<MistyExpose>>> repeatedConsumer) {
        List<MistyExpose> mistyExposeList = findBySPI();
        checkRepeated(mistyExposeList, repeatedConsumer);
        return mistyExposeList;
    }

    public static List<MistyExpose> findBySPIAndCheckRepeated(ClassLoader classLoader) {
        return findBySPIAndCheckRepeated(classLoader, MistyExposeDetector::thrownRepeatedException);
    }

    public static List<MistyExpose> findBySPIAndCheckRepeated(
            ClassLoader classLoader,
            Consumer<Map<String, List<MistyExpose>>> repeatedConsumer) {
        List<MistyExpose> mistyExposeList = findBySPI(classLoader);
        checkRepeated(mistyExposeList, repeatedConsumer);
        return mistyExposeList;
    }

    public static void checkRepeated(List<MistyExpose> mistyExposeList, Consumer<Map<String, List<MistyExpose>>> repeatedConsumer) {
        Map<String, List<MistyExpose>> repeatedMap = mistyExposeList.stream()
                .map(mistyExpose -> Collections.singletonMap(mistyExpose.getName(), mistyExpose))
                .reduce(new HashMap<String, List<MistyExpose>>(), (m1, m2) -> {
                    m2.forEach((key, value) -> m1.computeIfAbsent(key, name -> new ArrayList<>()).add(value));
                    return m1;
                }, (m1, m2) -> null).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .reduce(new HashMap<>(), (map, entry) -> {
                    map.put(entry.getKey(), entry.getValue());
                    return map;
                }, (l1, l2) -> null);

        if (!repeatedMap.isEmpty()) {
            repeatedConsumer.accept(repeatedMap);
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

        return new MistyExpose(name, version, description, mistyExposer.getClass());
    }

    private static void thrownRepeatedException(Map<String, List<MistyExpose>> repeatedMap) {
        throw new IllegalArgumentException("There are repeated name of MistyExpose following " + repeatedMap);
    }

}
