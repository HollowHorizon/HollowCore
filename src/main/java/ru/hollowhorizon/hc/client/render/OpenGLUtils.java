package ru.hollowhorizon.hc.client.render;

import net.minecraftforge.client.event.ModelRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import ru.hollowhorizon.hc.HollowCore;

import java.util.function.BooleanSupplier;

public class OpenGLUtils {

    private static final Logger logger = LogManager.getLogger();

    public static boolean openGL20 = true;
    public static boolean openGL21 = true;
    public static boolean openGL32;
    public static boolean openGL40;
    public static boolean openGL43;
    public static boolean openGL44;
    public static boolean openGL45;
    public static boolean openGL46;

    public static void onModelRegistryEvent(ModelRegistryEvent event) {
        GLCapabilities caps = GL.getCapabilities();
        //openGL20 = caps.OpenGL20;
        //openGL21 = caps.OpenGL21;
        openGL20 = true;
        openGL21 = true;
        openGL32 = caps.OpenGL32;
        openGL40 = caps.OpenGL40;
        openGL43 = caps.OpenGL43;
        openGL44 = caps.OpenGL44;
        openGL45 = caps.OpenGL45;
        openGL46 = caps.OpenGL46;
    }

    private static boolean tryGet(BooleanSupplier sup, String log) {
        try {
            return sup.getAsBoolean();
        } catch (Throwable ignored) {
            logger.info(log);
            return false;
        }
    }
}

