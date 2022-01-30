package ru.hollowhorizon.hc.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import ru.hollowhorizon.hc.HollowCore;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HollowJavaUtils {
    public static <T> T compileAndGet(String path, String file, String classPackage, Class<T> clazz) {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, path + file + ".java");

            URLClassLoader ucl = new URLClassLoader(new URL[]{new URL("file://" + path)});
            Class<?> cls = ucl.loadClass(classPackage+"."+file);
            Object instance = cls.getConstructor().newInstance();
            if (clazz.isAssignableFrom(instance.getClass())) {
                return (T) instance;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(FilenameUtils.getName(new ResourceLocation("hc:models/mmd/kirito.zip").getPath()));
    }

    public static String unpackZipFromJar(ResourceLocation location) {
        String fileName = FilenameUtils.getName(location.getPath());

        String fileWithoutExtension = fileName.substring(0, fileName.indexOf("."));
        try {
            File targetDir = new File(Minecraft.getInstance().gameDirectory, "config/hollowcore/cache/" + fileWithoutExtension + "/" + fileName);
            InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
            HollowCore.LOGGER.info(targetDir.getParentFile());
            if(!targetDir.getParentFile().exists()) {
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

    //anti-warning system :D
    public static void nothing() {

    }

    @SuppressWarnings("unchecked")
    public static <T> T fakeInstance() {
        return (T) new Object();
    }
}
