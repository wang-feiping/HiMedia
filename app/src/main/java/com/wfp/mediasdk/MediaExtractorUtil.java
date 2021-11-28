package com.wfp.mediasdk;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;

public final class MediaExtractorUtil {
    private static final String TAG = "WFP_Log";

    public static MediaExtractor getExtractor(String path) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(path);
        } catch (IOException error) {
            error.printStackTrace();
        }

        return extractor;
    }

    // 创建视频的MediaExtractor
    public static MediaFormat getVideoFormat(MediaExtractor extractor) {
        // 遍历轨道
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = extractor.getTrackFormat(i);
            Log.i(TAG, "format: " + mediaFormat.getString(MediaFormat.KEY_MIME));
            if (mediaFormat.getString(MediaFormat.KEY_MIME).contains("video/")) {
                Log.i(TAG, "track: " + i + " is video trunk");
                extractor.selectTrack(i);
                return mediaFormat;
            }
        }
        return null;
    }

    public static MediaFormat getAudioFormat(MediaExtractor extractor) {
        // 遍历轨道
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = extractor.getTrackFormat(i);
            Log.i(TAG, "format: " + mediaFormat.getString(MediaFormat.KEY_MIME));
            if (mediaFormat.getString(MediaFormat.KEY_MIME).contains("audio/")) {
                Log.i(TAG, "track: " + i + " is audio trunk");
                extractor.selectTrack(i);
                return mediaFormat;
            }
        }
        return null;
    }
}
