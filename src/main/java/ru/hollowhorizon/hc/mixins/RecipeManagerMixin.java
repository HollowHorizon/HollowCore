package ru.hollowhorizon.hc.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.common.objects.recipe.HollowRecipeHelper;
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition;
import ru.hollowhorizon.hc.common.objects.recipe.deep.HollowRecipeManager;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin implements HollowRecipeManager {
    @Shadow @Final private static Logger LOGGER;
    @Unique
    private HollowCondition.ConditionContext hc$conditionContext = HollowCondition.ConditionContext.EMPTY;

    @Override
    public @NotNull HollowCondition.ConditionContext getHc_context() {
        return hc$conditionContext;
    }

    @Override
    public void setHc_context(@NotNull HollowCondition.ConditionContext conditionContext) {
        this.hc$conditionContext = conditionContext;
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/Recipe;",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void onApply(
            Map<ResourceLocation, JsonElement> object,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci,
            @Local Map.Entry<ResourceLocation, JsonElement> entry,
            @Local ResourceLocation resourceLocation
    ) {
        if (resourceLocation.getPath().startsWith("_")) {
            ci.cancel();
            return;
        }

        if (entry.getValue().isJsonObject()) {
            JsonElement conditions = entry.getValue().getAsJsonObject().get("conditions");
            if (conditions != null && !HollowRecipeHelper.processConditions(conditions.getAsJsonObject(), "conditions", this.hc$conditionContext)) {
                LOGGER.debug("Skipping loading recipe {} as its conditions were not met", resourceLocation);
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void onAfterFromJson(
            Map<ResourceLocation, JsonElement> object,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci,
            @Local ResourceLocation resourceLocation,
            @Local(ordinal = 0) Recipe<?> recipe
    ) {
        if (recipe == null) {
            LOGGER.info("Skipping loading recipe {} as its serializer returned null", resourceLocation);
            ci.cancel();
        }
    }
}
