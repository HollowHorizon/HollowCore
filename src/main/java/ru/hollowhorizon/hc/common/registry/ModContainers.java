package ru.hollowhorizon.hc.common.registry;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.container.HollowContainer;

public class ModContainers {
    @HollowRegister
    public static final ContainerType<HollowContainer> HOLLOW_CONTAINER = IForgeContainerType.create(HollowContainer::new);
}
