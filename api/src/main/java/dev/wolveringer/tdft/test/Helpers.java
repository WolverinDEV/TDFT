package dev.wolveringer.tdft.test;

import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Comparator;

public interface Helpers {
    <T> Class<T> resolveClass(String path);

    <T> T createInstance(String klass, Object... arguments);
    <T> T createInstance(Class<T> klass, Object... arguments);

    <T> Method resolveMethod(Class<T> klass, String name, int modifiers, Class<?> result, Class<?>... arguments);
    default <T> Method resolveMethod(String className, String name, int modifiers, Class<?> result, Class<?>... arguments) {
        return resolveMethod(resolveClass(className), name, modifiers, result, arguments);
    }
    default <T> Method resolveMethod(T obj, String name, int modifiers, Class<?> result, Class<?>... arguments) {
        return resolveMethod(obj.getClass(), name, modifiers, result, arguments);
    }

    <T> void executeWithExpect(@NonNull Object obj, @NonNull String name, int modifiers, T result, Comparator<T> resultCmp, Object... arguments);
    <T> void executeWithExpect(@NonNull Object obj, @NonNull Method method, T result, Comparator<T> resultCmp, Object... arguments);
    default <T> void executeV(@NonNull Object obj, @NonNull String name, int modifiers, T result, Comparator<T> resultCmp, Object... arguments) {
        this.executeWithExpect(obj, name, modifiers, Void.TYPE, (a, b) -> 0, arguments);
    }

    void ensureImplements(@NonNull Class<?> klass, Class<?> other);
}
