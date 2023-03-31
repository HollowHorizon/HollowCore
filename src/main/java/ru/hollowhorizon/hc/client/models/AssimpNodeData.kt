package ru.hollowhorizon.hc.client.models

import net.minecraft.util.math.vector.Matrix4f

class AssimpNodeData(var name: String? = null) {
    var transformation = Matrix4f().apply { setIdentity() }
    var children = ArrayList<AssimpNodeData>()

}