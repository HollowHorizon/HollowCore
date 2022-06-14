package ru.hollowhorizon.hc.client.video.media;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import uk.co.caprica.vlcj.media.callback.nonseekable.NonSeekableInputStreamMedia;

import java.io.IOException;
import java.io.InputStream;

public class ResourceLocationMedia extends NonSeekableInputStreamMedia {
    private final ResourceLocation location;

    public ResourceLocationMedia(ResourceLocation location) throws IOException {
        super(HollowJavaUtils.getResourceLocationSize(location));
        this.location = location;
    }


    @Override
    protected InputStream onOpenStream() throws IOException {
        return Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
    }

    @Override
    protected void onCloseStream(InputStream inputStream) throws IOException {
        inputStream.close();
    }

    @Override
    protected long onGetSize() {
        return 0;
    }
}
