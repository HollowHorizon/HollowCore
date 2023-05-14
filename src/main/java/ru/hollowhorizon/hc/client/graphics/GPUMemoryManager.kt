package ru.hollowhorizon.hc.client.graphics

import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.HollowCore
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors


@OnlyIn(Dist.CLIENT)
class GPUMemoryManager private constructor() {
    private val vboMap = ConcurrentHashMap<WeakReference<VBO?>, Int>()
    private val vaoMap = ConcurrentHashMap<WeakReference<VAO?>, Int>()

    fun initialize() {
        Minecraft.getInstance().execute(ClearingRunnable(this))
    }

    fun createVAO(): VAO {
        val id = GL30.glGenVertexArrays()
        val vao = VAO(id)
        vaoMap[WeakReference(vao)] = id
        return vao
    }

    fun createVBO(type: Int): VBO {
        val id = GL15.glGenBuffers()
        val vbo = VBO(id, type)
        vboMap[WeakReference(vbo)] = id
        return vbo
    }

    private class ClearingRunnable(private val managerToHandle: GPUMemoryManager) : Runnable {
        override fun run() {
            val removedVBOs = managerToHandle.vboMap.entries.stream()
                .filter { it.key.get() == null }
                .collect(Collectors.toList())
            val removedVAOs = managerToHandle.vaoMap.entries.stream()
                .filter { it.key.get() == null }
                .collect(Collectors.toList())

            removedVBOs.forEach {
                managerToHandle.vboMap.remove(it.key)
                GL15.glDeleteBuffers(it.value)
            }
            removedVAOs.forEach {
                managerToHandle.vaoMap.remove(it.key)
                GL15.glDeleteBuffers(it.value)
            }

            val rescheduleThread = Thread {
                Thread.sleep(500)
                try {
                    Minecraft.getInstance().execute(this)
                } catch (ex: Exception) {
                    HollowCore.LOGGER.error("Failed to clear the GPUManager. GPU memory leaks will occur.", ex)
                }
            }
            rescheduleThread.start()
        }
    }

    companion object {
        val instance = GPUMemoryManager()
    }
}