package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.math.set
import de.fabmax.kool.scene.PerspectiveCamera
import de.fabmax.kool.scene.Scene
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture

fun Scene.mcCamera() {
    val camera = mainRenderPass.screenView.camera as PerspectiveCamera

    camera.onCameraUpdated += { event ->
        val capture = RenderStateCapture.LEVEL
        val view = capture.pose
        val pos = capture.camera?.position ?: Vec3.ZERO

        view.pushPose()
        view.translate(-pos.x, -pos.y, -pos.z)

        val modelView = Matrix4f(view.last().pose()).transpose() // MODEL VIEW MATRIX
        val projection = Matrix4f(capture.projection).transpose() // PROJECTION MATRIX

        event.camera.proj.set(
            projection.m00(), projection.m01(), projection.m02(), projection.m03(),
            projection.m10(), projection.m11(), projection.m12(), projection.m13(),
            projection.m20(), projection.m21(), projection.m22(), projection.m23(),
            projection.m30(), projection.m31(), projection.m32(), projection.m33(),
        )

        event.camera.dataF.view.set(
            modelView.m00(), modelView.m01(), modelView.m02(), modelView.m03(),
            modelView.m10(), modelView.m11(), modelView.m12(), modelView.m13(),
            modelView.m20(), modelView.m21(), modelView.m22(), modelView.m23(),
            modelView.m30(), modelView.m31(), modelView.m32(), modelView.m33()
        )
        val viewProj = projection.transpose().mul(modelView.transpose()).transpose()
        event.camera.dataF.viewProj.set(
            viewProj.m00(), viewProj.m01(), viewProj.m02(), viewProj.m03(),
            viewProj.m10(), viewProj.m11(), viewProj.m12(), viewProj.m13(),
            viewProj.m20(), viewProj.m21(), viewProj.m22(), viewProj.m23(),
            viewProj.m30(), viewProj.m31(), viewProj.m32(), viewProj.m33()
        )
        event.camera.dataF.lazyInvView.isDirty = true
        event.camera.dataF.lazyInvViewProj.isDirty = true

        event.camera.dataD.view.set(event.camera.dataF.view)
        event.camera.dataD.viewProj.set(event.camera.dataF.viewProj)
        event.camera.dataD.lazyInvView.isDirty = true
        event.camera.dataD.lazyInvViewProj.isDirty = true
        view.popPose()
    }
}