package ru.hollowhorizon.hc.client.render.game;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import ru.hollowhorizon.hc.HollowCore;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class GPUMemoryManager {
    public static GPUMemoryManager INSTANCE = new GPUMemoryManager();

    private final ConcurrentMap<WeakReference<VBO>, Integer> VBOs = new ConcurrentHashMap();
    private final ConcurrentMap<WeakReference<VAO>, Integer> VAOs = new ConcurrentHashMap();

    private GPUMemoryManager()
    {
    }

    public void initialize()
    {
        Minecraft.getInstance().execute(new ClearingRunnable(this));
    }

    public VAO createVAO() {
        final int id = GL30.glGenVertexArrays();
        final VAO vao = new VAO(id);

        VAOs.put(new WeakReference<>(vao), id);

        return vao;
    }

    public VBO createVBO(final int type){
        final int id = GL15.glGenBuffers();
        final VBO vbo = new VBO(id, type);

        VBOs.put(new WeakReference<>(vbo), id);

        return vbo;
    }

    private static final class ClearingRunnable implements Runnable {

        private final GPUMemoryManager managerToHandle;

        public ClearingRunnable(final GPUMemoryManager managerToHandle) {this.managerToHandle = managerToHandle;}

        @Override
        public void run() {
            final List<Map.Entry<WeakReference<VBO>, Integer>> removedVBOs =
                    managerToHandle.VBOs.entrySet().stream().filter(entry -> entry.getKey().get() == null).collect(Collectors.toList());
            final List<Map.Entry<WeakReference<VAO>, Integer>> removedVAOs =
                    managerToHandle.VAOs.entrySet().stream().filter(entry -> entry.getKey().get() == null).collect(Collectors.toList());

            removedVBOs.forEach(weakReferenceIntegerEntry -> {
                managerToHandle.VBOs.remove(weakReferenceIntegerEntry.getKey());
                GL15.glDeleteBuffers(weakReferenceIntegerEntry.getValue());
            });

            removedVAOs.forEach(weakReferenceIntegerEntry -> {
                managerToHandle.VAOs.remove(weakReferenceIntegerEntry.getKey());
                GL30.glDeleteVertexArrays(weakReferenceIntegerEntry.getValue());
            });

            //Reschedule clearing task.
            //We will have to wait a minimal amount of time.
            final Thread rescheduleThread = new Thread(() -> {
                try
                {
                    Thread.sleep(500);
                }
                catch (final InterruptedException ignored)
                {
                }

                try
                {
                    Minecraft.getInstance().execute(this);
                }
                catch (final Exception ex)
                {
                    HollowCore.LOGGER.error("Failed to reregister the GPUManager. GPU memory leaks will occur.", ex);
                }
            });
            rescheduleThread.start();
        }

        @Override
        public String toString()
        {
            return "ClearingRunnable{" +
                    "managerToHandle=" + managerToHandle +
                    '}';
        }
    }

    @Override
    public String toString()
    {
        return "GPUManager{" +
                "VBOs=" + VBOs +
                ", VAOs=" + VAOs +
                '}';
    }
}
