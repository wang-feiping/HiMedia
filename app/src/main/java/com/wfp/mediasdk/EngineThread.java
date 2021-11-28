package com.wfp.mediasdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.CountDownLatch;

/**
 * 解码引擎线程。
 */
public class EngineThread extends Thread {
    private static final String TAG = "WFP_LOG";

    private static final int ENGINE_MSG = 1;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private Handler mHandler;

    public EngineThread(String threadName) {
        super(threadName);
    }

    @Override
    public void run() {
        Looper.prepare();
        if (Looper.myLooper() == null) {
            return;
        }

        mHandler = new Handler(Looper.myLooper(), new MyHandler());
        countDownLatch.countDown();
        Looper.loop();
    }

    public Handler getHandler() {
        try {
            countDownLatch.await();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
            return null;
        }

        return mHandler;
    }

    public void close() {
        if (mHandler != null) {
            Log.i(TAG, getName() + " close");
            mHandler.getLooper().quit();
            mHandler = null;
        }
    }

    private static class MyHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            int what = msg.what;
            if (what == ENGINE_MSG) {
                if (msg.obj instanceof Runnable) {
                    Runnable runnable = (Runnable) msg.obj;
                    runnable.run();
                }
            }
            return true;
        }
    }
}
