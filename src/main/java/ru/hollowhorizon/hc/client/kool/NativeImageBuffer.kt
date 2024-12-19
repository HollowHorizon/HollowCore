package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.NativeImage
import de.fabmax.kool.util.Buffer

class NativeImageBuffer(
    val image: NativeImage,
) : Buffer {
    override val capacity: Int get() = image.width * image.height * 4
    override var isAutoLimit: Boolean
        get() = false
        set(value) {}
    override var limit: Int
        get() = -1
        set(value) {}
    override var position: Int
        get() = -1
        set(value) {}

    override fun clear() {
        image.close()
    }

}