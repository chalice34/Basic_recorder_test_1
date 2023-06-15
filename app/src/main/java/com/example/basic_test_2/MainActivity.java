package com.example.basic_test_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import android.Manifest;
import android.content.pm.PackageManager;


public class MainActivity extends AppCompatActivity {
    private MediaRecorder recorder;
    File audiofile = null;
    static final String TAG = "MediaRecording";
    Button startButton, stopButton;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final int REQUEST_SAVE_AUDIO = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    try {
                        startRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    requestPermissions();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
    }

    private boolean checkPermissions() {
        int recordPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return recordPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    startRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() throws IOException {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        //Creating file
        File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        try {
            audiofile = File.createTempFile("sound", ".3gp", dir);
            Log.d(TAG, "Audio file path: " + audiofile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "External storage access error: " + e.getMessage());
            return;
        }

        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder prepare() failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        recorder.start();
        Log.d(TAG, "Recording started");

    }

    private void stopRecording() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        //stopping recorder
        recorder.stop();
        recorder.release();

        //after stopping the recorder, create the sound file and add it to media library.
        saveRecordingToMediaStore();
    }

    private void saveRecordingToMediaStore() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "audio.3gp");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);

        ContentResolver contentResolver = getContentResolver();
        Uri audioUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if (audioUri != null) {
            try {
                contentResolver.openOutputStream(audioUri).close();
                Toast.makeText(this, "Audio saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving audio", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error creating audio file", Toast.LENGTH_SHORT).show();
        }
    }
}