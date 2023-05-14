package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.util.math.vector.Quaternion
import org.lwjgl.assimp.Assimp
import ru.hollowhorizon.hc.client.models.HollowModel
import ru.hollowhorizon.hc.client.models.HollowModelRenderer
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.objects.entities.TestEntityV2

class TestEntityV2Renderer(entityManager: EntityRendererManager) :
    EntityRenderer<TestEntityV2>(entityManager) {
    var rotation = 0f

    private val model = HollowModel(
        Assimp.aiImportFile(
            "C:/Users/user/Downloads/ent.geo.fbx",
            Assimp.aiProcess_GenSmoothNormals or
                    Assimp.aiProcess_JoinIdenticalVertices or
                    Assimp.aiProcess_Triangulate or
                    Assimp.aiProcess_FixInfacingNormals or
                    Assimp.aiProcess_LimitBoneWeights or
                    Assimp.aiProcess_OptimizeMeshes
        ) ?: throw IllegalStateException("Error: " + Assimp.aiGetErrorString())
    )
    private val renderer = HollowModelRenderer(model)

    override fun getTextureLocation(entity: TestEntityV2) = "hc:models/entity/lololowka.png".rl


    override fun render(arg: TestEntityV2, f: Float, g: Float, stack: MatrixStack, arg3: IRenderTypeBuffer, i: Int) {
        super.render(arg, f, g, stack, arg3, i)
        //rotation += 0.5f

        //model.root.find("Body")?.nodeMatrix?.apply { multiply(Quaternion(rotation, 1.0f, 0.0f, 0.0f)) }
        renderer.render(arg3, stack, g, i)
    }
}