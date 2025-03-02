package ru.hollowhorizon.hc.mixins;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.common.objects.recipe.condition.DefaultHollowConditionContext;
import ru.hollowhorizon.hc.common.objects.recipe.deep.HollowRecipeManager;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Shadow @Final private RecipeManager recipes;

    @Shadow @Final private TagManager tagManager;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void init(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel, CallbackInfo ci) {
        ((HollowRecipeManager) this.recipes).setHc_context(new DefaultHollowConditionContext(this.tagManager));
    }
}
