package com.wfp.mediasdk;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.util.Log;

public class AudioTrackUtil {
    private static final String TAG = "WFP_LOG";

    public static AudioTrack createAudioTrack(MediaFormat format) {
        int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        Log.i(TAG, "channelCount: " + channelCount + " " + sampleRate);

        int maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int inputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
        int frameSizeInBytes = channelCount * 2;
        inputBufferSize = (inputBufferSize / frameSizeInBytes) * frameSizeInBytes;
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                44100,
                (channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                AudioFormat.ENCODING_PCM_16BIT,
                inputBufferSize,
                AudioTrack.MODE_STREAM);

        audioTrack.play();
        return audioTrack;
    }
}
