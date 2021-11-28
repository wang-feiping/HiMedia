package com.wfp.mediasdk;

import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;

public class AudioEngine {
    private static final String TAG = "WFP_LOG";

    private String mPath;

    private MediaCodec codec;

    private AudioTrack audioTrack;

    private MediaExtractor extractor;

    private EngineThread engineThread;

    private volatile boolean isPlay = false;

    private volatile boolean isInitialed = false;

    private volatile boolean isInputBufferEnd = false;

    private volatile boolean isOutputBufferEnd = false;

    public AudioEngine(String path) {
        this.mPath = path;
    }

    public void initEngine() {
        if (isInitialed) {
            Log.i(TAG, "AudioEngine already initial");
            return;
        }

        this.extractor = MediaExtractorUtil.getExtractor(mPath);
        MediaFormat format = MediaExtractorUtil.getAudioFormat(extractor);
        this.codec = MediaCodecUtil.createMediaCodec(format, null);
        this.audioTrack = AudioTrackUtil.createAudioTrack(format);
        this.engineThread = new EngineThread("AudioEngine");
        this.engineThread.start();
        isInitialed = true;
    }

    public void play() {
        Handler handler = engineThread.getHandler();
        if (handler == null) {
            Log.e(TAG, "handler is null");
            return;
        }

        if (isPlay) {
            Log.e(TAG, "audio already play");
            return;
        }

        isPlay = true;
        isInputBufferEnd = false;
        isOutputBufferEnd = false;
        handler.post(this::onDraw);
    }

    public void stop() {
        isPlay = false;
        isInputBufferEnd  = true;
        isOutputBufferEnd = true;
    }

    public void release() {
        if (engineThread != null) {
            engineThread.close();
            engineThread = null;
        }

        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }

        if (extractor != null) {
            extractor.release();
            extractor = null;
        }

        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }

        isInitialed = false;
        isPlay = false;
    }

    private void onDraw() {
        MediaCodec.BufferInfo decodeBufferInfo = new MediaCodec.BufferInfo();
        while (isPlay) {
            if (!isInputBufferEnd) {
                int inputIndex = codec.dequeueInputBuffer(10000);
                if (inputIndex < 0) {
                    continue;
                }

                ByteBuffer inputBuffer = codec.getInputBuffer(inputIndex);
                int sampleSize = extractor.readSampleData(inputBuffer, 0);
                if (sampleSize > 0) {
                    codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                    extractor.advance();
                } else {
                    Log.i(TAG, "audio input end of stream");

                    codec.queueInputBuffer(inputIndex, 0, 0,
                            extractor.getSampleTime(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isInputBufferEnd = true;
                }
            }

            if (!isOutputBufferEnd) {
                int outputIndex = codec.dequeueOutputBuffer(decodeBufferInfo, 0);
                if ((decodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.i(TAG, "audio output end of stream");

                    extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    codec.flush();
                    isOutputBufferEnd = true;
                } else {
                    ByteBuffer outputBuffer;
                    byte[] chunkPCM;

                    if (outputIndex >= 0) {
                        outputBuffer = codec.getOutputBuffer(outputIndex);
                        chunkPCM = new byte[decodeBufferInfo.size];
                        outputBuffer.get(chunkPCM);
                        outputBuffer.clear();
                        audioTrack.write(chunkPCM, 0, decodeBufferInfo.size);
                        codec.releaseOutputBuffer(outputIndex, false);
                    }
                }
            }
        }
    }
}
