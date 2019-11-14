package dev.wolveringer.tdft;

import dev.wolveringer.tdft.test.Helpers;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
class SimpleHelper implements Helpers {
    @NonNull
    private final TestContext context;

    @Override
    public <T> Class<T> resolveClass(@NonNull String path, int modifiers) {
        Class<T> klass;
        try {
            klass = (Class<T>) Class.forName(path, true, context.getClassLoader());
        } catch(Exception ex) {
            throw new RuntimeException("Failed to resolve class " + path + ". Maybe missing?", ex);
        }

        if(modifiers >= 0) {
            Validate.isTrue((klass.getModifiers() & modifiers) == modifiers, "Required modifiers for class " + path + " are not given (required: " + Modifier.toString(modifiers) + "; given: " + Modifier.toString(klass.getModifiers()) + ")");
        }
        return klass;
    }

    @Override
    public <T> T createInstance(String klass, Object... arguments) {
        return createInstance(this.resolveClass(klass), arguments);
    }

    @Override
    public <T> T createInstance(Class<T> klass, Object... arguments) {
        if(arguments.length != 0)
            throw new RuntimeException("TODO: Implement me!");

        //TODO: Test if constructor exists and if its accessible
        try {
            return klass.newInstance();
        } catch(Exception ex) {
            throw new RuntimeException("Failed to create a new instance of class " + klass.getName() + ".", ex);
        }
    }

    @Override
    public Field resolveField(Class<?> klass, String name, int modifiers) {
        for(Field f : klass.getDeclaredFields()) {
            if(f.getName().equals(name)) {
                if(modifiers >= 0) {
                    Validate.isTrue((f.getModifiers() & modifiers) == modifiers, "Required modifiers for field " + name + " in class " + klass.getName() +" are not given (required: " + Modifier.toString(modifiers) + "; given: " + Modifier.toString(f.getModifiers()) + ")");
                }
                return f;
            }
        }
        throw new RuntimeException("Missing field " + name + " in class " + klass.getName());
    }

    private boolean similarParameters(Class<?> a, Class<?> b) {
        if(a.isArray() || b.isArray()) {
            if(!a.isArray() || !b.isArray())
                return false;
            return similarParameters(a.getComponentType(), b.getComponentType());
        } else if(a == b)
            return true;
        else if(byte.class == a)
            return similarParameters(b, Byte.class);
        else if(char.class == a)
            return similarParameters(b, Character.class);
        else if(short.class == a)
            return similarParameters(b, Short.class);
        else if(int.class == a)
            return similarParameters(b, Integer.class);
        else if(long.class == a)
            return similarParameters(b, Long.class);
        else if(double.class == a)
            return similarParameters(b, Double.class);
        else if(float.class == a)
            return similarParameters(b, Float.class);
        else if(b.isPrimitive())
            return similarParameters(b, a);
        return false;
    }

    private String describeMethod(String name, int modifiers, Class<?> result, Class<?>... arguments) {
        StringBuilder str = new StringBuilder();

        if(modifiers > 0)
            str.append(Modifier.toString(modifiers));
        else
            str.append("public");
        str.append(" ");
        if(result == void.class)
            str.append("void");
        else
            str.append(result.getSimpleName());
        str.append(" ");
        str.append(name);
        str.append("(");
        for(int i = 0; i < arguments.length; i++) {
            str.append(arguments[i].getSimpleName());
            str.append(" a");
            str.append(i);
            if(i + 1 < arguments.length)
                str.append(", ");
        }
        str.append(")");

        return str.toString();
    }

    @Override
    public <T> Method resolveMethod(@NonNull Class<T> klass, @NonNull String name, int modifiers, Class<?> result, Class<?>... arguments) {
        Optional<Method> method;

        /* resolve the method */
        try {
            method = Stream.of(
                    klass.getMethods(),
                    klass.getDeclaredMethods()
            ).flatMap(Stream::of)
                    .filter(e -> e.getName().equals(name))
                    .filter(e -> similarParameters(e.getReturnType(), result))
                    .filter(e -> e.getParameterCount() == arguments.length)
                    .filter(e -> {
                        Class<?>[] paramTypes = e.getParameterTypes();
                        for(int i = 0; i < paramTypes.length; i++)
                            if(!similarParameters(paramTypes[i], arguments[i]))
                                return false;
                        return true;
                    }).findFirst();
        } catch(Exception ex) {
            throw new RuntimeException("failed resolve method " + name, ex);
        }
        Method m = method.orElseThrow(() -> new RuntimeException("failed to resolve method \"" + describeMethod(name, modifiers, result, arguments) + "\" in " + klass.getName()));

        /* validate arguments */
        if(modifiers >= 0) {
            Validate.isTrue((m.getModifiers() & modifiers) == modifiers, "Required modifiers for method " + name + " in class " + klass.getName() + " are not given (required: " + Modifier.toString(modifiers) + "; given: " + Modifier.toString(m.getModifiers()) + ")");
        }

        /* check for parameter similarity and log a warning */
        if(m.getReturnType() != result) {
            this.context.getLogger().warning("Method " + name + " in class " + klass.getName() + " has a not correct return type. Expecting: " + result.getSimpleName() + " Got: " + m.getReturnType().getSimpleName() + ". You should may change this!");
        }
        for(int i = 0; i < m.getParameterCount(); i++) {
            Class<?> paramClass = m.getParameterTypes()[i];
            if(paramClass != arguments[i])
                this.context.getLogger().warning("Method " + name + " in class " + klass.getName() + " has at parameter " + (i + 1) + " a not expected type. Expecting: " + arguments[i].getSimpleName() + " Got: " + paramClass.getSimpleName() + ". You should may change this!");
        }
        return m;
    }

    @Override
    public <T> void executeWithExpect(@NonNull Object obj, @NonNull String name, int modifiers, T result, ResultComparator<T> resultCmp, Object... arguments) {
        Class<?>[] args = Stream.of(arguments).map(Object::getClass).toArray(Class<?>[]::new);
        Method m = this.resolveMethod(obj.getClass(), name, modifiers, result == Void.TYPE ? void.class : result.getClass(), args);

        this.executeWithExpect(obj, m, result, resultCmp, arguments);
    }

    private Object p2oArray(Object val){
        if(val == null || !val.getClass().isArray())
            return val;

        if (val instanceof Object[])
            return (Object[])val;

        int len = Array.getLength(val);
        Object[] res = new Object[len];
        for(int i = 0; i < len; ++i)
            res[i] = p2oArray(Array.get(val, i));
        return res;
    }

    @Override
    public <T> void executeWithExpect(@NonNull Object obj, @NonNull Method method, T result, ResultComparator<T> resultCmp, Object... arguments) {
        Object res;
        try {
            res = method.invoke(obj, (Object[]) arguments);
        } catch(Exception ex) {
            throw new RuntimeException("failed to invoke method " + method.getName(), ex);
        }

        if(result != Void.TYPE) {
            T cr;
            try {
                res = p2oArray(res);
                cr = (T) res;
            } catch(ClassCastException ex) {
                throw new RuntimeException("Result is not the expected type!", ex);
            }
            if(resultCmp.compare(result, cr) != 0) {
                String expected = result == null ? "null" : result + "";
                if(result != null && result.getClass().isArray())
                    expected = ArrayUtils.toString(result);

                String received = cr == null ? "null" : cr + "";
                if(cr != null && cr.getClass().isArray())
                    received = ArrayUtils.toString(cr);
                throw new RuntimeException("received result isn't equal to the expected.\nExpected: " + expected + "\nReceived: " + received);
            }
        }
    }

    @Override
    public void ensureImplements(@NonNull Class<?> klass, @NonNull Class<?> other) {
        if(other == Object.class)
            return;

        final Class<?> orgKlass = klass;
        while(klass != Object.class) {
            for(Class interf : klass.getInterfaces()) {
                while(interf != Object.class && interf.isInterface()) {
                    if(interf == other)
                        return;
                    interf = interf.getSuperclass();
                }
            }

            klass = klass.getSuperclass();
        }
        throw new RuntimeException("Class " + orgKlass.getName() + " does not implement " + other.getName());
    }

    @Override
    public void ensureExtends(@NonNull Class<?> klass, @NonNull Class<?> other) {
        if(other == Object.class)
            return;

        final Class<?> orgKlass = klass;
        while(klass != Object.class) {
            if(klass == other)
                return;
            klass = klass.getSuperclass();
        }
        throw new RuntimeException("Class " + orgKlass.getName() + " does not extends " + other.getName());
    }
}