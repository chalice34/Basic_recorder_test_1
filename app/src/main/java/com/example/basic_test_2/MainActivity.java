package com.example.basic_test_2;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import android.Manifest;



public class MainActivity extends AppCompatActivity {
    private MediaRecorder recorder;
    File audiofile = null;
    static final String TAG = "MediaRecording";
    Button startButton, stopButton;
    private static final int REQUEST_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        if(Build.VERSION.SDK_INT=>Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS)
    !=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},1000);
        }*/

        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);
        startButton.setOnClickListener(view -> {

                if (checkPermissions()) {
                    try {
                        startRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    requestPermissions();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());
        String fileName = "sound_" + timestamp + ".3gp";

        File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        audiofile = new File(dir, fileName);
        Log.d(TAG, "Audio file path: " + audiofile.getAbsolutePath());

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
                String filename=audiofile.getName();
                Toast.makeText(this, "Audio saved successfully", Toast.LENGTH_SHORT).show();

                Toast.makeText(this,"Audio file name is: "+filename,Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving audio", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error creating audio file", Toast.LENGTH_SHORT).show();
        }
    }
}