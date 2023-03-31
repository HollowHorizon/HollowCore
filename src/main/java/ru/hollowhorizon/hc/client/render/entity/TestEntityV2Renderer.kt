package ru.hollowhorizon.hc.client.render.entity

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import ru.hollowhorizon.hc.client.models.Mesh
import ru.hollowhorizon.hc.client.models.StaticMeshesLoader
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.objects.entities.TestEntityV2

class TestEntityV2Renderer(entityManager: EntityRendererManager) :
    EntityRenderer<TestEntityV2>(entityManager) {

    //private var model: Array<Mesh?> = StaticMeshesLoader.load("C:\\Users\\user\\Downloads\\bunny.obj")

    override fun getTextureLocation(entity: TestEntityV2) = "hc:models/entity/lololowka.png".rl


    override fun render(arg: TestEntityV2?, f: Float, g: Float, arg2: MatrixStack?, arg3: IRenderTypeBuffer?, i: Int) {
        super.render(arg, f, g, arg2, arg3, i)

        //model.forEach { it?.render() }
    }
}