package ru.hollowhorizon.hc.client.render.data

import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel


class BTAnimatedModelRenderData(private val animatedModel: BTAnimatedModel, doCombined: Boolean) :
    BTModelRenderData(animatedModel, doCombined) {

    override fun updateRenderData(): HashMap<String, IBTRenderData> {
        val ret: HashMap<String, IBTRenderData> = HashMap()
        if (doCombined) {
            val mesh = animatedModel.combinedAnimatedMesh
            ret[mesh.name] = BTAnimatedMeshRenderData(mesh)
        } else {
            for (mesh in animatedModel.animatedMeshes) {
                ret[mesh.name] = BTAnimatedMeshRenderData(mesh)
            }
        }
        return ret
    }
}