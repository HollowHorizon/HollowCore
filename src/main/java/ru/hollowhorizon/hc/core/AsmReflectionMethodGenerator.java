/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.core;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AsmReflectionMethodGenerator {

    final private static String PLAIN_PROCEDURE_INTERNAL_NAME = Type.getInternalName(PlainReflectionMethod.class);

    public static synchronized ReflectionMethod generateMethod(Method refMethod)
            throws SecurityException, NoSuchMethodException {
        return generateMethod(refMethod, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", ReflectionMethod.class, true, true);
    }

    private static synchronized ReflectionMethod generateMethod(Method refMethod, String name, String desc,
                                                                Class interfaceClass, boolean argsParams,
                                                                boolean returnValue) throws SecurityException, NoSuchMethodException {
        final Class declaringClass = refMethod.getDeclaringClass();
        String ownerClassName = declaringClass.getName();

        Method[] declaredMethods = declaringClass.getDeclaredMethods();
        int methodIndex = 0;
        for (; methodIndex < declaredMethods.length; ++methodIndex) {
            if (declaredMethods[methodIndex].equals(refMethod))
                break;
        }
        String className = ownerClassName + "MethodReflection" + name + methodIndex;

        try {
            Class definedClass;
            try { // checks if was already loaded
                definedClass = declaringClass.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) // need to build a new class
            {
                String classInternalName = className.replace('.', '/'); // build internal name for ASM

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC,
                        classInternalName, null, PLAIN_PROCEDURE_INTERNAL_NAME,
                        new String[]{Type.getInternalName(interfaceClass)});

                createCtor(cw);
                createMethod(refMethod.getDeclaringClass(), refMethod.getName(), refMethod, cw,
                        name, desc, argsParams, returnValue, refMethod.getParameterTypes());

                cw.visitEnd();

                byte[] b = cw.toByteArray();
                definedClass = defineClass(declaringClass.getClassLoader(), className, b);
            }

            return (ReflectionMethod) definedClass.getConstructor(Method.class).newInstance(refMethod);
        } catch (Exception e) {
            NoSuchMethodException ex = new NoSuchMethodException("Can't create ASM method reflection helper for [" + refMethod + "]");
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * Creates the class constructor which delegates the input to the super.
     */
    private static void createCtor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(Ljava/lang/reflect/Method;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(PlainReflectionMethod.class),
                "<init>", "(Ljava/lang/reflect/Method;)V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * Creates the method invoking wrapper method.
     */
    private static void createMethod(Class clazz, String name,
                                     Method refMethod, ClassWriter cw, String methodName, String desc,
                                     boolean argsParams, boolean returnValue, Class... parameterTypes) {

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_VARARGS,
                methodName, desc, null, null);

        boolean isStatic = Modifier.isStatic(refMethod.getModifiers());
        boolean isInteface = Modifier.isInterface(refMethod.getDeclaringClass().getModifiers());

        final int invokeCode;
        if (isStatic) {
            invokeCode = Opcodes.INVOKESTATIC;
        } else {
            invokeCode = isInteface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clazz));
        }

        if (argsParams) {
            for (int i = 0; i < parameterTypes.length; ++i) {
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitIntInsn(Opcodes.BIPUSH, i);
                mv.visitInsn(Opcodes.AALOAD);
                prepareParameter(mv, Type.getType(parameterTypes[i]));
            }
        } else {
            for (int i = 0; i < parameterTypes.length; ++i) {
                mv.visitVarInsn(Opcodes.ALOAD, i + 2);
                prepareParameter(mv, Type.getType(parameterTypes[i]));
            }
        }

        mv.visitMethodInsn(invokeCode, Type.getInternalName(clazz),
                name, Type.getMethodDescriptor(refMethod));

        if (returnValue) {
            prepareResult(mv, refMethod);
            mv.visitInsn(Opcodes.ARETURN);
        } else {
            mv.visitInsn(Opcodes.RETURN);
        }

        mv.visitMaxs(1, 1); // ignored since ClassWriter set as ClassWriter.COMPUTE_MAXS
        mv.visitEnd();
    }

    /**
     * Box the result if needed.
     */
    private static void prepareResult(MethodVisitor mv, Method refMethod) {

        Type type = Type.getReturnType(refMethod);
        switch (type.getSort()) {
            case Type.VOID:
                mv.visitInsn(Opcodes.ACONST_NULL); // nothing to return the original method returns void
                break;
            case Type.BOOLEAN:
                callBoxer(mv, "(Z)Ljava/lang/Object;");
                break;
            case Type.BYTE:
                callBoxer(mv, "(B)Ljava/lang/Object;");
                break;
            case Type.CHAR:
                callBoxer(mv, "(C)Ljava/lang/Object;");
                break;
            case Type.SHORT:
                callBoxer(mv, "(S)Ljava/lang/Object;");
                break;
            case Type.INT:
                callBoxer(mv, "(I)Ljava/lang/Object;");
                break;
            case Type.LONG:
                callBoxer(mv, "(J)Ljava/lang/Object;");
                break;
            case Type.FLOAT:
                callBoxer(mv, "(F)Ljava/lang/Object;");
                break;
            case Type.DOUBLE:
                callBoxer(mv, "(D)Ljava/lang/Object;");
                break;
        }
    }

    private static void callBoxer(MethodVisitor mv, String desc) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Boxer.INTERNAL_NAME, "box", desc);
    }

    /**
     * Unbox the input parameters
     */
    private static void prepareParameter(MethodVisitor mv, Type type) {

        switch (type.getSort()) {
            case Type.BOOLEAN:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                break;
            case Type.BYTE:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
                break;
            case Type.CHAR:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
                break;
            case Type.SHORT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
                break;
            case Type.INT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
                break;
            case Type.LONG:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
                break;
            case Type.FLOAT:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
                break;
            case Type.DOUBLE:
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
                break;
            default:
                mv.visitTypeInsn(Opcodes.CHECKCAST, type.getInternalName());
                break;
        }
    }

    private static Class defineClass(ClassLoader loader, String name, byte[] b) throws Exception {
        return ASMMethodBuilder.INSTANCE.getLOADER().define(name, b);
    }

}
