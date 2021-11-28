package com.wfp.mediasdk;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;

public class MediaCodecUtil {
    public static MediaCodec createMediaCodec(MediaFormat format, Surface surface) {
        MediaCodec codec = null;
        try {
            codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
        } catch (IOException error) {
            error.printStackTrace();
        }

        codec.configure(format, surface, null, 0);
        codec.start();
        return codec;
    }
}
