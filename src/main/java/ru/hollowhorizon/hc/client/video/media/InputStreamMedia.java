package ru.hollowhorizon.hc.client.video.media;

import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import uk.co.caprica.vlcj.media.callback.nonseekable.NonSeekableInputStreamMedia;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class InputStreamMedia extends NonSeekableInputStreamMedia {
    private final Supplier<InputStream> stream;

    public InputStreamMedia(Supplier<InputStream> supplier) throws IOException {
        super(HollowJavaUtils.getInputStreamSize(supplier.get()));
        this.stream = supplier;
    }

    @Override
    protected InputStream onOpenStream() throws IOException {
        return stream.get();
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
