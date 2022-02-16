package ru.hollowhorizon.hc.client.render.shader;

import net.minecraft.client.Minecraft;
import ru.hollowhorizon.hc.HollowCore;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class ShaderManager {
    public static ShaderManager INSTANCE = new ShaderManager();
    private final ConcurrentMap<WeakReference<ShaderProgram>, ShaderDeletionHandler> shaders = new ConcurrentHashMap<>();

    private ShaderManager() {
    }

    public void initialize() {
        Minecraft.getInstance().execute(new ClearingRunnable(this));
    }

    public void registerShader(final ShaderProgram shaderProgram) throws IOException {
        shaders.put(new WeakReference<>(shaderProgram), shaderProgram.init());
        HollowCore.LOGGER.info("Created Shader: " + shaderProgram.getProgramID());
    }

    @Override
    public String toString() {
        return "ShaderManager{" +
                "shaders=" + shaders +
                '}';
    }

    /**
     * Clears up the GPU when VBOs and VAOs are no longer needed.
     */
    private static final class ClearingRunnable implements Runnable {

        private final ShaderManager managerToHandle;

        public ClearingRunnable(final ShaderManager managerToHandle) {
            this.managerToHandle = managerToHandle;
        }

        @Override
        public void run() {
            final List<Map.Entry<WeakReference<ShaderProgram>, ShaderDeletionHandler>> removedShaders =
                    managerToHandle.shaders.entrySet().stream().filter(entry -> entry.getKey().get() == null).collect(Collectors.toList());

            removedShaders.forEach(weakReferenceIntegerEntry -> {
                HollowCore.LOGGER.info("Deleting Shader: " + weakReferenceIntegerEntry.getValue().getProgramId());
                managerToHandle.shaders.remove(weakReferenceIntegerEntry.getKey());
                weakReferenceIntegerEntry.getValue().run();
            });

            final Thread rescheduleThread = new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException ignored) {
                }

                try {
                    Minecraft.getInstance().execute(this);
                } catch (final Exception ex) {
                    HollowCore.LOGGER.error("Failed to reregister the ShaderManager. GPU memory leaks will occur.", ex);
                }
            });
            rescheduleThread.start();
        }

        @Override
        public String toString() {
            return "ClearingRunnable{" +
                    "managerToHandle=" + managerToHandle +
                    '}';
        }
    }
}
