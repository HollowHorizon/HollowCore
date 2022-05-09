package ru.hollowhorizon.hc.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import ru.hollowhorizon.hc.HollowCore;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HollowJavaUtils {
    public static InputStream getFileFromJar(Path pathToJar) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(pathToJar.toFile());

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equals("mcmod.info")) {
                    return zipFile.getInputStream(entry);
                }
            }
            throw new FileNotFoundException("mcmod.info not found!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("mcmod.info invalid!");
        }
    }

    public static void updateJarFile(File srcJarFile, String path, InputStream data) {

        URI uri = srcJarFile.toURI();
        System.out.println(uri);

        try (FileSystem zipfs = FileSystems.newFileSystem(srcJarFile.toPath(), null)) {
            Path pathInZipfile = zipfs.getPath(path);

            Files.copy(data, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T compileAndGet(String path, String file, String classPackage, Class<T> clazz) {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, path + file + ".java");

            URLClassLoader ucl = new URLClassLoader(new URL[]{new URL("file://" + path)});
            Class<?> cls = ucl.loadClass(classPackage + "." + file);
            Object instance = cls.getConstructor().newInstance();
            if (clazz.isAssignableFrom(instance.getClass())) {
                return (T) instance;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String unpackZipFromJar(ResourceLocation location) {
        String fileName = FilenameUtils.getName(location.getPath());

        String fileWithoutExtension = fileName.substring(0, fileName.indexOf("."));
        try {
            File targetDir = new File(Minecraft.getInstance().gameDirectory, "config/hollowcore/cache/" + fileWithoutExtension + "/" + fileName);
            InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
            HollowCore.LOGGER.info(targetDir.getParentFile());
            if (!targetDir.getParentFile().exists()) {
                targetDir.getParentFile().mkdirs();
                Files.copy(stream, targetDir.getAbsoluteFile().toPath());

                unpackZip(targetDir);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return fileWithoutExtension;
    }

    public static void unpackZip(File zip) {

        try (java.util.zip.ZipFile zipFile = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(zip.getParentFile(), entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(in, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        zip.delete();
    }

    public static boolean hasFile(ResourceLocation location) {
        try {
            Minecraft.getInstance().getResourceManager().getResource(location);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void ensureIndex(ArrayList<?> a, int i) {

        while (a.size() <= i) {
            a.add(null);
        }

    }

    public static InputStream getResource(ResourceLocation location) {
        try {
            return Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
        } catch (Exception e) {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/" + location.getNamespace() + "/" + location.getPath());
        }
    }

    //anti-warning system :D
    public static void nothing() {

    }

    @SuppressWarnings("unchecked")
    public static <T> T fakeInstance() {
        return (T) new Object();
    }

    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    public static int[] listIntToArray(List<Integer> list) {
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }

    public static <R, K extends R> K castDarkMagic(R original) {
        return (K) original;
    }
}
