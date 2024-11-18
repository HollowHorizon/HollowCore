package ru.hollowhorizon.loader;

import org.objectweb.asm.*;

public class HCTransformer {
    public static byte[] transformResolver(byte[] classfileBuffer) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        classReader.accept(new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (name.equals("finish") && descriptor.equals("(Ljava/lang/module/Configuration;)Ljava/util/Map;")) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String methodName,
                                                    String methodDescriptor, boolean isInterface) {
                            // Убираем вызов checkExportSuppliers(graph)
                            if (methodName.equals("checkExportSuppliers")) {
                                // Пропускаем вызов метода
                                return;
                            }
                            super.visitMethodInsn(opcode, owner, methodName, methodDescriptor, isInterface);
                        }
                    };
                }
                return mv;
            }
        }, 0);
        return classWriter.toByteArray();
    }

    public static byte[] transformLayer(byte[] classfileBuffer) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        classReader.accept(new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (name.equals("checkForDuplicatePkgs")) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            // Начало метода
                            super.visitCode();
                            // Добавляем инструкцию возврата
                            super.visitInsn(Opcodes.RETURN);
                            // Завершаем метод
                            super.visitMaxs(0, 0);
                            super.visitEnd();
                        }
                    };
                }
                return mv;
            }
        }, 0);
        return classWriter.toByteArray();
    }

}
