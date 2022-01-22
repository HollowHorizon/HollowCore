package ru.hollowhorizon.hc.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.LocateCommand;
import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.common.network.data.GeneratedStructuresData;
import ru.hollowhorizon.hc.common.world.structures.objects.StoryStructure;

import static net.minecraft.command.impl.LocateCommand.showLocateResult;

@Mixin(LocateCommand.class)
public class MixinLocateCommand {

    @Inject(method = "locate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;findNearestMapFeature(Lnet/minecraft/world/gen/feature/structure/Structure;Lnet/minecraft/util/math/BlockPos;IZ)Lnet/minecraft/util/math/BlockPos;"), cancellable = true)
    private static void locate(CommandSource command, Structure<?> structure, CallbackInfoReturnable<Integer> cir) {
        if (structure instanceof StoryStructure) {
            StoryStructure storyStructure = (StoryStructure) structure;
            if (GeneratedStructuresData.INSTANCE.hasData(storyStructure.getRegistryName().toString())) {
                cir.setReturnValue(1);
                try {
                    showLocateResult(command, structure.getFeatureName(), command.getPlayerOrException().blockPosition(), GeneratedStructuresData.INSTANCE.getStructurePos(storyStructure.getRegistryName().toString()), "commands.locate.success");
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
