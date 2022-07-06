package ru.hollowhorizon.hc.client.utils;

import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.ResourceLocation;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class DynamicJavaCompiler {
    public static void main(String[] args) throws Exception {
        DynamicJavaCompiler.run(new ResourceLocation(MODID, "c.java"));
    }

    public static void run(ResourceLocation location) throws Exception {
        StringBuilder program = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(HollowJavaUtils.getResource(location)));
            String str;
            while ((str = in.readLine()) != null) {
                program.append(str);
            }
            in.close();
        } catch (IOException e) {
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Iterable<? extends JavaFileObject> fileObjects;
        fileObjects = getJavaSourceFromString(program.toString());

        compiler.getTask(null, null, null, null, null, fileObjects).call();

        Class<?> clazz = Class.forName("src/C");
        Method m = clazz.getMethod("main", new Class[]{String[].class});
        Object[] _args = new Object[]{new String[0]};
        m.invoke(null, _args);
    }

    static Iterable<JavaSourceFromString> getJavaSourceFromString(String code) {
        final JavaSourceFromString jsfs;
        jsfs = new JavaSourceFromString("code", code);
        return new Iterable<JavaSourceFromString>() {
            public Iterator<JavaSourceFromString> iterator() {
                return new Iterator<JavaSourceFromString>() {
                    boolean isNext = true;

                    public boolean hasNext() {
                        return isNext;
                    }

                    public JavaSourceFromString next() {
                        if (!isNext)
                            throw new NoSuchElementException();
                        isNext = false;
                        return jsfs;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}

class JavaSourceFromString extends SimpleJavaFileObject {
    final String code;

    JavaSourceFromString(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}