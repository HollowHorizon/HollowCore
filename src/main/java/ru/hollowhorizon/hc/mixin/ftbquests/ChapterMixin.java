package ru.hollowhorizon.hc.mixin.ftbquests;

import dev.ftb.mods.ftbquests.quest.Chapter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.common.integration.ftb.quests.HollowChapter;

@Mixin(Chapter.class)
public class ChapterMixin implements HollowChapter {
    private CompoundNBT extra = new CompoundNBT();

    @Inject(method = "writeData", at = @At(value = "TAIL"), remap = false)
    public void writeData(CompoundNBT nbt, CallbackInfo ci) {
        nbt.put("hollow_data", extra);
    }

    @Inject(method = "readData", at = @At(value = "TAIL"), remap = false)
    public void readData(CompoundNBT nbt, CallbackInfo ci) {
        extra = nbt.getCompound("hollow_data");
    }

    @Inject(method = "writeNetData", at = @At(value = "TAIL"), remap = false)
    public void writeNetData(PacketBuffer buffer, CallbackInfo ci) {
        buffer.writeNbt(extra);
    }

    @Inject(method = "readNetData", at = @At(value = "TAIL"), remap = false)
    public void readNetData(PacketBuffer buffer, CallbackInfo ci) {
        extra = buffer.readNbt();
    }

    public CompoundNBT getExtra() {
        return extra;
    }

    public void setExtra(CompoundNBT extra) {
        this.extra = extra;
    }
}
