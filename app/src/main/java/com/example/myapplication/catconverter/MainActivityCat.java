package com.example.myapplication.catconverter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.myapplication.R;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class MainActivityCat extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 1;
    // CRITICAL FIX: Match Flask API sample rate!
    private static final int SAMPLE_RATE = 44100;  // Changed from 22050 to 44100

    private CatVoiceAPI catVoiceAPI;
    private AudioRecord audioRecord;
    private MediaPlayer mediaPlayer;
    private Thread recordingThread;

    private Button btnRecord, btnStop, btnConvert, btnPlay;
    private TextView tvStatus, tvCatText;
    private ProgressBar progressBar;
    private SpeechRecognizer speechRecognizer;

    private File recordedFile;
    private File catVoiceFile;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_cat);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        catVoiceAPI = new CatVoiceAPI(this);

        btnRecord = findViewById(R.id.btnRecord);
        btnStop = findViewById(R.id.btnStop);
        btnConvert = findViewById(R.id.btnConvert);
        btnPlay = findViewById(R.id.btnPlay);
        tvStatus = findViewById(R.id.tvStatus);
        tvCatText = findViewById(R.id.tvCatText);
        progressBar = findViewById(R.id.progressBar);

        btnRecord.setOnClickListener(v -> startRecording());
        btnStop.setOnClickListener(v -> stopRecording());
        btnConvert.setOnClickListener(v -> convertToCat());
        btnPlay.setOnClickListener(v -> playCatVoice());

        checkPermissions();
        testServerConnection();
        updateUI();
    }

    private void testServerConnection() {
        tvStatus.setText("Testing server connection...");
        catVoiceAPI.checkHealth(new CatVoiceAPI.HealthCallback() {
            @Override
            public void onHealthCheck(boolean isHealthy, String message) {
                runOnUiThread(() -> {
                    if (isHealthy) {
                        tvStatus.setText("✅ Server connected! Ready to record");
                        Toast.makeText(MainActivityCat.this, "Server is ready!", Toast.LENGTH_SHORT).show();
                    } else {
                        tvStatus.setText("❌ Server error: " + message);
                        Toast.makeText(MainActivityCat.this, "Server connection failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    private void startRecording() {
        try {
            recordedFile = new File(getCacheDir(), "recorded_voice.wav");

            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

            // CRITICAL FIX: Calculate proper buffer size
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, channelConfig, audioFormat);
            int bufferSize = minBufferSize * 4;  // 4x for smoother recording, prevents cracks

            android.util.Log.d("Recording", "=== RECORDING CONFIG ===");
            android.util.Log.d("Recording", "Sample Rate: " + SAMPLE_RATE + " Hz");
            android.util.Log.d("Recording", "Min Buffer: " + minBufferSize + " bytes");
            android.util.Log.d("Recording", "Using Buffer: " + bufferSize + " bytes");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission needed", Toast.LENGTH_SHORT).show();
                return;
            }

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,  // Changed from VOICE_RECOGNITION to MIC for better quality
                    SAMPLE_RATE,
                    channelConfig,
                    audioFormat,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Toast.makeText(this, "AudioRecord initialization failed", Toast.LENGTH_SHORT).show();
                return;
            }

            audioRecord.startRecording();
            isRecording = true;
            tvStatus.setText("🎤 Recording at " + SAMPLE_RATE + " Hz...");
            updateUI();

            recordingThread = new Thread(() -> writeAudioDataToFile());
            recordingThread.start();

        } catch (Exception e) {
            android.util.Log.e("Recording", "Failed to start recording", e);
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writeAudioDataToFile() {
        // CRITICAL FIX: Use optimal buffer size
        int bufferSize = 8192;  // 8KB buffer for smooth recording
        byte[] audioData = new byte[bufferSize];
        FileOutputStream fos = null;
        long totalBytesRead = 0;

        try {
            fos = new FileOutputStream(recordedFile);

            // Reserve space for WAV header (44 bytes)
            byte[] header = new byte[44];
            fos.write(header);

            android.util.Log.d("Recording", "Started writing audio data...");

            while (isRecording) {
                int bytesRead = audioRecord.read(audioData, 0, bufferSize);

                if (bytesRead > 0) {
                    fos.write(audioData, 0, bytesRead);
                    totalBytesRead += bytesRead;
                } else if (bytesRead < 0) {
                    android.util.Log.e("Recording", "Error reading audio: " + bytesRead);
                    break;
                }
            }

            fos.close();

            android.util.Log.d("Recording", "Total audio bytes: " + totalBytesRead);

            // CRITICAL FIX: Write proper WAV header
            writeWavHeader(recordedFile, totalBytesRead);

            android.util.Log.d("Recording", "WAV file complete: " + recordedFile.length() + " bytes");

        } catch (IOException e) {
            android.util.Log.e("Recording", "Error writing audio", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeWavHeader(File file, long audioDataSize) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0);

        int channels = 1;  // Mono
        int bitsPerSample = 16;  // 16-bit PCM
        long byteRate = SAMPLE_RATE * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;

        // RIFF chunk descriptor
        raf.writeBytes("RIFF");
        raf.writeInt(Integer.reverseBytes((int) (36 + audioDataSize)));  // File size - 8
        raf.writeBytes("WAVE");

        // fmt sub-chunk
        raf.writeBytes("fmt ");
        raf.writeInt(Integer.reverseBytes(16));  // Sub-chunk size
        raf.writeShort(Short.reverseBytes((short) 1));  // PCM format
        raf.writeShort(Short.reverseBytes((short) channels));  // Channels
        raf.writeInt(Integer.reverseBytes(SAMPLE_RATE));  // Sample rate
        raf.writeInt(Integer.reverseBytes((int) byteRate));  // Byte rate
        raf.writeShort(Short.reverseBytes((short) blockAlign));  // Block align
        raf.writeShort(Short.reverseBytes((short) bitsPerSample));  // Bits per sample

        // data sub-chunk
        raf.writeBytes("data");
        raf.writeInt(Integer.reverseBytes((int) audioDataSize));  // Data size

        raf.close();

        android.util.Log.d("Recording", "=== WAV HEADER ===");
        android.util.Log.d("Recording", "Sample Rate: " + SAMPLE_RATE);
        android.util.Log.d("Recording", "Channels: " + channels);
        android.util.Log.d("Recording", "Bits/Sample: " + bitsPerSample);
        android.util.Log.d("Recording", "Audio Size: " + audioDataSize);
        android.util.Log.d("Recording", "Total Size: " + (44 + audioDataSize));
    }

    private void stopRecording() {
        if (audioRecord != null && isRecording) {
            try {
                isRecording = false;

                // Wait for recording thread to finish
                if (recordingThread != null && recordingThread.isAlive()) {
                    recordingThread.join(1000);  // Wait max 1 second
                }

                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;

                if (recordedFile != null && recordedFile.exists()) {
                    long fileSize = recordedFile.length();
                    float duration = (float)(fileSize - 44) / (SAMPLE_RATE * 2);  // 2 bytes per sample

                    android.util.Log.d("Recording", "=== RECORDING COMPLETE ===");
                    android.util.Log.d("Recording", "File: " + recordedFile.getAbsolutePath());
                    android.util.Log.d("Recording", "Size: " + fileSize + " bytes");
                    android.util.Log.d("Recording", "Duration: " + duration + " seconds");

                    tvStatus.setText(String.format("✅ Recorded %.1fs (%d KB)", duration, fileSize/1024));
                    Toast.makeText(this,
                            String.format("Recording: %.1fs, %d KB", duration, fileSize/1024),
                            Toast.LENGTH_SHORT).show();
                } else {
                    tvStatus.setText("❌ Recording failed - file not found");
                }

                updateUI();

            } catch (Exception e) {
                android.util.Log.e("Recording", "Stop recording error", e);
                tvStatus.setText("❌ Stop recording error: " + e.getMessage());
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void convertToCat() {
        if (recordedFile == null || !recordedFile.exists()) {
            Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify file integrity
        if (recordedFile.length() < 44) {
            Toast.makeText(this, "Recording too short or corrupted", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("🔄 Converting to cat voice...");
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btnConvert.setEnabled(false);

        android.util.Log.d("Convert", "=== CONVERSION START ===");
        android.util.Log.d("Convert", "File: " + recordedFile.getAbsolutePath());
        android.util.Log.d("Convert", "Size: " + recordedFile.length());

        catVoiceAPI.convertToCatVoice(recordedFile, new CatVoiceAPI.ConversionCallback() {
            @Override
            public void onSuccess(File catFile) {
                runOnUiThread(() -> {
                    catVoiceFile = catFile;
                    android.util.Log.d("Convert", "=== CONVERSION SUCCESS ===");
                    android.util.Log.d("Convert", "Cat file: " + catFile.getAbsolutePath());
                    android.util.Log.d("Convert", "Cat size: " + catFile.length());

                    tvStatus.setText("🐱 Cat voice ready! (" + catFile.length()/1024 + " KB)");
                    progressBar.setVisibility(ProgressBar.GONE);
                    updateUI();
                    Toast.makeText(MainActivityCat.this, "Conversion successful!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e("Convert", "=== CONVERSION ERROR ===");
                    android.util.Log.e("Convert", "Error: " + error);

                    String displayError = parseError(error);
                    tvStatus.setText(displayError);
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnConvert.setEnabled(true);
                    Toast.makeText(MainActivityCat.this, displayError, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String parseError(String error) {
        if (error.contains("404")) {
            return "❌ 404: /convert endpoint not found\nCheck server URL";
        } else if (error.contains("400")) {
            return "❌ 400: Invalid file format or field name\nFile: " + recordedFile.getName();
        } else if (error.contains("500")) {
            return "❌ 500: Server processing error\nCheck server logs";
        } else if (error.contains("failed to connect") || error.contains("Connection")) {
            return "❌ Connection error\nIs server running?";
        } else {
            return "❌ Error: " + error;
        }
    }

    private void playCatVoice() {
        if (catVoiceFile == null || !catVoiceFile.exists()) {
            Toast.makeText(this, "No cat voice available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (catVoiceFile.length() < 44) {
            Toast.makeText(this, "❌ File too small - invalid WAV", Toast.LENGTH_LONG).show();
            return;
        }

        // Set volume to maximum
        android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, maxVolume, 0);

        tvStatus.setText("🔊 Playing cat voice...");

        android.util.Log.d("Playback", "=== PLAYING CAT VOICE ===");
        android.util.Log.d("Playback", "File: " + catVoiceFile.getAbsolutePath());
        android.util.Log.d("Playback", "Size: " + catVoiceFile.length());

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setDataSource(catVoiceFile.getAbsolutePath());

            mediaPlayer.setOnPreparedListener(mp -> {
                android.util.Log.d("Playback", "Duration: " + mp.getDuration() + "ms");
                tvStatus.setText("🔊 Playing cat voice...");
                tvCatText.setVisibility(TextView.VISIBLE);
                tvCatText.setText("🐱 Meow meow...");
                mp.start();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                android.util.Log.d("Playback", "Playback completed");
                tvStatus.setText("🐱 Cat voice ready!");
                tvCatText.setVisibility(TextView.GONE);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                android.util.Log.e("Playback", "Error - what: " + what + " extra: " + extra);
                tvStatus.setText("❌ Playback error");
                return true;
            });

            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            android.util.Log.e("Playback", "IOException", e);
            tvStatus.setText("❌ Playback failed: " + e.getMessage());
            Toast.makeText(this, "Playback failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI() {
        btnRecord.setEnabled(!isRecording);
        btnStop.setEnabled(isRecording);
        btnConvert.setEnabled(!isRecording && recordedFile != null && recordedFile.exists());
        btnPlay.setEnabled(catVoiceFile != null && catVoiceFile.exists());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord != null) {
            audioRecord.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}