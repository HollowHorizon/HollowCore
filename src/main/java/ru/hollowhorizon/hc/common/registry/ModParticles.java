package ru.hollowhorizon.hc.common.registry;

import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import ru.hollowhorizon.hc.client.render.particles.HollowParticleType;

public class ModParticles {
    //@HollowRegister(texture = "hc:blue_flame")
    public static final HollowParticleType BLUE_FLAME = new HollowParticleType();

    public static void onRegisterParticleFactories(ParticleFactoryRegisterEvent event) {
        //Minecraft.getInstance().particleEngine.register(BLUE_FLAME, FXBlueFlame.Factory::new);

    }

}