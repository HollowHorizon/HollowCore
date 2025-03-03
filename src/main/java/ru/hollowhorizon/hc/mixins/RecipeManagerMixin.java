package ru.hollowhorizon.hc.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
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
import ru.hollowhorizon.hc.common.objects.recipe.deep.HollowRecipeSerializer;
import ru.hollowhorizon.hc.common.utils.ForgeKotlinKt;

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
            at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;")
    )
    public void apply(
            Map<ResourceLocation, JsonElement> object,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci,
            @Local Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map,
            @Local ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder,
            @Local Map.Entry<ResourceLocation, JsonElement> entry
    ) {
        var id = entry.getKey();

        try {
            if (entry.getValue().isJsonObject() && !HollowRecipeHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions", this.hc$conditionContext)) {
                LOGGER.debug("Skipping loading recipe {} as it's conditions were not met", id);
                return;
            }
            Recipe<?> recipe = hc$fromJson(id, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), this.hc$conditionContext);
            if (recipe == null) {
                throw new JsonParseException("Error parsing recipe " + id + " as it's serializer returned null");
            }
            map.computeIfAbsent(recipe.getType(), (p_44075_) -> ImmutableMap.builder()).put(id, recipe);
            builder.put(id, recipe);
        } catch (IllegalArgumentException | JsonParseException e) {
            LOGGER.error("Parsing error loading recipe {}", id, e);
        }
    }

    private static Recipe<?> hc$fromJson(ResourceLocation id, JsonObject json, HollowCondition.ConditionContext ctx) {
        String s = GsonHelper.getAsString(json, "type");
        return ((HollowRecipeSerializer<?>) BuiltInRegistries.RECIPE_SERIALIZER.getOptional(ForgeKotlinKt.getRl(s))
                .orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'")))
                .fromJson(id, json, ctx);
    }
}
