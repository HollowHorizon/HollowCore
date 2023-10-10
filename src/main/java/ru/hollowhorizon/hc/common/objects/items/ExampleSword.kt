package ru.hollowhorizon.hc.common.objects.items

import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tiers
import net.minecraftforge.client.extensions.common.IClientItemExtensions
//import ru.hollowhorizon.hc.client.render.items.GLTFItemRenderer
import java.util.function.Consumer

class ExampleSword : SwordItem(Tiers.NETHERITE, 10, -2.4f, Properties()) {
    override fun initializeClient(consumer: Consumer<IClientItemExtensions>) {
        //consumer.accept(GLTFItemRenderer())
    }
}