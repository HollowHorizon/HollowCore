package ru.hollowhorizon.hc.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.item.BuildTabContentsEvent;
import ru.hollowhorizon.hc.common.handlers.CreativeTabHandler;
import java.util.*;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
    @Shadow
    private Collection<ItemStack> displayItems;

    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @Shadow @Final private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;

    @Inject(method = "buildContents", at = @At("TAIL"))
    private void onUpdateContents(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci, @Local CreativeModeTab.ItemDisplayBuilder displayParameters, @Local ResourceKey<CreativeModeTab> tabKey) {
        var mutableDisplayStacks = new LinkedHashSet<>(displayItems);
        var mutableSearchTabStacks = new LinkedHashSet<>(displayItemsSearchTab);

        //? if fabric
        this.hc$tabContents((CreativeModeTab) (Object) this, tabKey, this.displayItemsGenerator, parameters, displayParameters);

        mutableDisplayStacks.addAll(CreativeTabHandler.INSTANCE.getITEMS().computeIfAbsent(JavaHacks.forceCast(this), (i) -> new ArrayList<>()));
        mutableSearchTabStacks.addAll(CreativeTabHandler.INSTANCE.getITEMS().computeIfAbsent(JavaHacks.forceCast(this), (i) -> new ArrayList<>()));

        displayItems = mutableDisplayStacks;
        displayItemsSearchTab = mutableSearchTabStacks;
    }

    //? if fabric {
    @Unique
    private void hc$tabContents(
            CreativeModeTab tab,
            ResourceKey<CreativeModeTab> tabKey,
            CreativeModeTab.DisplayItemsGenerator generator,
            CreativeModeTab.ItemDisplayParameters params,
            CreativeModeTab.Output out
    ) {
        LinkedHashMap<ItemStack, CreativeModeTab.TabVisibility> entries = new LinkedHashMap<>();

        generator.accept(params, (stack, vis) -> {
            if (stack.getCount() != 1) {
                throw new IllegalArgumentException("The stack count must be 1");
            }

            entries.merge(stack, vis, (left, right) -> CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        });

        EventBus.post(new BuildTabContentsEvent(tab, tabKey, params, entries));

        entries.forEach(out::accept);
    }
    //?}
}
