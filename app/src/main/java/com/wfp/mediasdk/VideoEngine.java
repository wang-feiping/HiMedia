package com.wfp.mediasdk;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

public class VideoEngine {
    private static final String TAG = "WFP_LOG";

    private String mPath;

    private MediaExtractor extractor;

    private MediaCodec codec;

    private volatile boolean isPlay = false;

    private volatile boolean isInitialed = false;

    private volatile boolean isInputBufferEnd = false;

    private volatile boolean isOutputBufferEnd = false;

    private EngineThread engineThread;

    public VideoEngine(String path) {
        this.mPath = path;
    }

    public void initEngine(Surface surface) {
        if (isInitialed) {
            Log.i(TAG, "VideoEngine already initial");
            return;
        }

        this.extractor = MediaExtractorUtil.getExtractor(mPath);
        MediaFormat format = MediaExtractorUtil.getVideoFormat(extractor);
        this.codec = MediaCodecUtil.createMediaCodec(format, surface);
        this.engineThread = new EngineThread("VideoEngine");
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
            Log.e(TAG, "video already play");
            return;
        }

        isInputBufferEnd = false;
        isOutputBufferEnd = false;
        isPlay = true;
        handler.post(this::onDrawFrame);
    }

    public void stop() {
        isPlay = false;
        isInputBufferEnd = true;
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

        isPlay = false;
        isInitialed = false;
    }

    private void onDrawFrame() {
        ByteBuffer[] inputBuffers = codec.getInputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (isPlay) {
            if (!isInputBufferEnd) {
                int inputIndex = codec.dequeueInputBuffer(-1);
                if (inputIndex < 0) {
                    Log.i(TAG, "video inputIndex is invalid");
                    continue;
                }

                ByteBuffer byteBuffer = inputBuffers[inputIndex];

                // 解码数据
                int sampleSize = extractor.readSampleData(byteBuffer, 0);
                if (sampleSize >= 0) {
                    // 送入Input队列中
                    codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                    extractor.advance();
                } else {
                    Log.i(TAG, "video input end of stream");
                    // 读取到了结尾。
                    codec.queueInputBuffer(inputIndex, 0, 0,
                            extractor.getSampleTime(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isInputBufferEnd = true;
                }
            }

            if (!isOutputBufferEnd) {
                // 取解码后到数据
                // 从Output队列中取
                int outputIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                if (outputIndex >= 0) {
                    // output 读到了末尾，重置Codec和Extractor
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.i(TAG, "outputBuffer end of stream");

                        // 播放到了末尾，恢复各种状态
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        codec.flush();
                        isPlay = false;
                        isOutputBufferEnd = true;
                    } else {
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        codec.releaseOutputBuffer(outputIndex, true);
                    }
                }
            }
        }
        Log.i(TAG, "play finish");
    }
}
