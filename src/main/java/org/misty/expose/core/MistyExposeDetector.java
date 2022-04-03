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

    public static List<MistyExpose> findBySPIAndCheckDuplicate() {
        return findBySPIAndCheckDuplicate(MistyExposeDetector::thrownDuplicateException);
    }

    public static List<MistyExpose> findBySPIAndCheckDuplicate(Consumer<Map<String, List<MistyExpose>>> duplicateConsumer) {
        List<MistyExpose> mistyExposeList = findBySPI();
        checkDuplicate(mistyExposeList, duplicateConsumer);
        return mistyExposeList;
    }

    public static List<MistyExpose> findBySPIAndCheckDuplicate(ClassLoader classLoader) {
        return findBySPIAndCheckDuplicate(classLoader, MistyExposeDetector::thrownDuplicateException);
    }

    public static List<MistyExpose> findBySPIAndCheckDuplicate(
            ClassLoader classLoader,
            Consumer<Map<String, List<MistyExpose>>> duplicateConsumer) {
        List<MistyExpose> mistyExposeList = findBySPI(classLoader);
        checkDuplicate(mistyExposeList, duplicateConsumer);
        return mistyExposeList;
    }

    public static void checkDuplicate(List<MistyExpose> mistyExposeList, Consumer<Map<String, List<MistyExpose>>> duplicateConsumer) {
        Map<String, List<MistyExpose>> duplicateMap = mistyExposeList.stream()
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

        if (!duplicateMap.isEmpty()) {
            duplicateConsumer.accept(duplicateMap);
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

    private static void thrownDuplicateException(Map<String, List<MistyExpose>> duplicateMap) {
        throw new IllegalArgumentException("There are duplicate name of MistyExpose following " + duplicateMap);
    }

}
