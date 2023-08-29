package ru.hollowhorizon.hc.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public interface ReflectionMethod {

    Class<?> getDeclaringClass();

    String getName();

    int getModifiers();

    Class<?> getReturnType();

    Class<?>[] getParameterTypes();

    Class[] getExceptionTypes();

    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    Type getGenericReturnType();

    Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    Method getMethod();
}
