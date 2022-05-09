package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.story.events.StoryEventListener;

import java.util.function.Supplier;

public class UpdateStoryEventToClient {
    private final CompoundNBT nbt;

    public UpdateStoryEventToClient(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public static UpdateStoryEventToClient decode(PacketBuffer buf) {
        return new UpdateStoryEventToClient(buf.readNbt());
    }

    public static void onReceived(UpdateStoryEventToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);
        CompoundNBT nbt = message.nbt;
        String storyName = nbt.getString("story_name");

        StoryEventListener.updateStory(storyName, nbt);
    }

    public void encode(PacketBuffer buf) {
        buf.writeNbt(this.nbt);
    }
}
