package com.wfp.mediasdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MediaWfp";

    private final String[] mPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private SurfaceView surfaceView;

    private Button start_bt;
    private Button stop_bt;
    private Button release_bt;

    private VideoEngine videoEngine;

    private AudioEngine audioEngine;

    private static final String PATH = "/storage/emulated/0/DCIM/Camera/2020-07-05-194509579.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyStoragePermissions(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.surfaceView = findViewById(R.id.surfaceView);
        this.start_bt = findViewById(R.id.play);
        this.stop_bt = findViewById(R.id.stop);
        this.release_bt = findViewById(R.id.release);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                if (videoEngine == null) {
                    videoEngine = new VideoEngine(PATH);
                }

                if (audioEngine == null) {
                    audioEngine = new AudioEngine(PATH);
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

        start_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "start onClick");

                // 开启播放
                videoEngine.initEngine(surfaceView.getHolder().getSurface());
                videoEngine.play();

                audioEngine.initEngine();
                audioEngine.play();
            }
        });

        stop_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "stop onClick");

                // 暂停播放
                if (videoEngine != null) {
                    videoEngine.stop();
                }

                if (audioEngine != null) {
                    audioEngine.stop();
                }
            }
        });

        release_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "release onClick");

                if (videoEngine != null) {
                    videoEngine.release();
                }

                if (audioEngine != null) {
                    audioEngine.release();
                }
            }
        });
    }

    // 动态权限申请
    private void verifyStoragePermissions(MainActivity activity) {
        final int REQUEST_EXTERNAL_STORAGE = 1;

        try {
            int permissionRead =
                    ActivityCompat.checkSelfPermission(activity, "android.permission.READ_EXTERNAL_STORAGE");
            if (permissionRead != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, mPermissions, REQUEST_EXTERNAL_STORAGE);
            }

            int permissionWrite =
                    ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, mPermissions, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage());
        }
    }
}
