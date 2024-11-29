package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import de.fabmax.kool.math.AngleF
import de.fabmax.kool.math.Mat4f
import de.fabmax.kool.math.QuatF
import de.fabmax.kool.scene.PerspectiveCamera
import de.fabmax.kool.scene.Scene
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import org.joml.Matrix4f
import ru.hollowhorizon.hc.client.utils.mc

fun Scene.mcCamera() {
    val mcCamera = Minecraft.getInstance().gameRenderer.mainCamera
    val camera = mainRenderPass.screenView.camera as PerspectiveCamera

    onUpdate {
        camera.clipNear = 0.05f
        camera.clipFar = Minecraft.getInstance().gameRenderer.depthFar
        camera.fovY = AngleF(Minecraft.getInstance().options.fov().get() *Mth.DEG_TO_RAD)
        camera.transform.apply {
            setIdentity()

            val poseStack = PoseStack()
            poseStack.mulPose(Axis.XP.rotationDegrees(mcCamera.xRot).normalize())
            poseStack.mulPose(Axis.YP.rotationDegrees(mcCamera.yRot+180f).normalize())
            val rot = poseStack.last().pose()
            setMatrix(Mat4f(
                rot.m00(), rot.m01(), rot.m02(), rot.m03(),
                rot.m10(), rot.m11(), rot.m12(), rot.m13(),
                rot.m20(), rot.m21(), rot.m22(), rot.m23(),
                rot.m30(), rot.m31(), rot.m32(), rot.m33(),
            ))
            translate(mcCamera.position.x, mcCamera.position.y, mcCamera.position.z)
        }
    }
}