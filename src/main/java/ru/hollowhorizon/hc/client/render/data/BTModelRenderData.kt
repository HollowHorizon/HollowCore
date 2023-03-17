package ru.hollowhorizon.hc.client.render.data

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.client.models.core.model.BTModel


@OnlyIn(Dist.CLIENT)
open class BTModelRenderData(private val model: BTModel, protected val doCombined: Boolean) : IBTRenderDataContainer {
    final override var isInitialized = false
        private set
    final override var frameSinceLastRender: Int = 0
        private set
    private var meshData = HashMap<String, IBTRenderData>()

    open fun updateRenderData(): HashMap<String, IBTRenderData> {
        val ret: HashMap<String, IBTRenderData> = HashMap()
        if (doCombined) {
            val mesh = model.combinedMesh
            ret[mesh.name] = BTMeshRenderData(mesh)
        } else {
            for (mesh in model.meshes) {
                ret[mesh.name] = BTMeshRenderData(mesh)
            }
        }
        return ret
    }

    fun renderSubset(toRender: Set<String>) {
        if (!this.isInitialized) {
            return
        }
        for (key in toRender) {
            val data = meshData[key]
            data?.render()
        }
    }

    override fun upload() {
        meshData = updateRenderData()
        for (data in meshData.values) {
            data.upload()
        }
        frameSinceLastRender = 0
        this.isInitialized = true
    }

    override fun cleanup() {
        if (!this.isInitialized) {
            return
        }
        for (meshRenderData in meshData.values) {
            meshRenderData.cleanup()
        }
        meshData.clear()
        this.isInitialized = false
    }

    override fun incrementFrameCount() {
        frameSinceLastRender++
    }

    override fun render() {
        if (!this.isInitialized) {
            return
        }
        frameSinceLastRender = 0
        for (meshRenderData in meshData.values) {
            meshRenderData.render()
        }
    }
}