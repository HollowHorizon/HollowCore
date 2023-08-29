package ru.hollowhorizon.hc.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


public class PlainReflectionMethod<T> implements ReflectionMethod {

    private final Method method;

    public PlainReflectionMethod(Method method) {
        this.method = method;
    }

    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    public String getName() {
        return method.getName();
    }

    public int getModifiers() {
        return method.getModifiers();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Class[] getExceptionTypes() {
        return method.getExceptionTypes();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return method.getAnnotation(annotationType);
    }

    public Type getGenericReturnType() {
        return method.getGenericReturnType();
    }

    public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(obj, args);
    }

    public Method getMethod() {
        return method;
    }
}