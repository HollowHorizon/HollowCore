package ru.hollowhorizon.hc.mixin.ftbteams;

import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamBase;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.hollowhorizon.hc.api.ICapabilityDispatcher;
import ru.hollowhorizon.hc.client.utils.JavaHacks;

@Mixin(value = TeamBase.class, remap = false)
public class AbstractTeamMixin implements ICapabilityProvider, ICapabilityDispatcher {
    @Unique
    private @Nullable CapabilityDispatcher hollowcore$capabilities;
    @Unique
    private boolean hollowcore$initialized = false;


    @Override
    public @NotNull CapabilityDispatcher getCapabilities() {
        if (!hollowcore$initialized) {
            hollowcore$capabilities = ForgeEventFactory.gatherCapabilities(JavaHacks.forceCast(Team.class), this, null);
            hollowcore$initialized = true;
        }
        return hollowcore$capabilities;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        final CapabilityDispatcher disp = getCapabilities();
        return disp.getCapability(cap, side);
    }
}
