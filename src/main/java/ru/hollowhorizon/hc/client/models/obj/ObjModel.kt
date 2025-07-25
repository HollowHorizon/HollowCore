package ru.hollowhorizon.hc.client.models.obj

import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.models.internal.Model
import ru.hollowhorizon.hc.client.models.internal.manager.ModelLoader

object ObjModelLoader: ModelLoader {
    override val supportedFormats: Set<String>
        get() = setOf("obj")

    override suspend fun load(location: ResourceLocation): Model {
        return OBJModel(location).toInternalModel()
    }

}