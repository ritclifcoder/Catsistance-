package com.example.myapplication.catconverter;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class CatVoiceAPI {

    private static final String TAG = "CatVoiceAPI";
    private static final String BASE_URL = "http://192.168.1.3:5000";
    private final OkHttpClient client;
    private final Context context;

    public CatVoiceAPI(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void convertToCatVoice(File audioFile, float f0Min, float f0Max,
                                   float jitter, float shimmer, ConversionCallback callback) {

        if (!audioFile.exists()) {
            callback.onError("Audio file does not exist");
            return;
        }

        Log.d(TAG, "=== CONVERT REQUEST ===");
        Log.d(TAG, "URL: " + BASE_URL + "/convert");
        Log.d(TAG, "File: " + audioFile.getAbsolutePath());
        Log.d(TAG, "File name: " + audioFile.getName());
        Log.d(TAG, "File size: " + audioFile.length() + " bytes");
        Log.d(TAG, "File exists: " + audioFile.exists());
        Log.d(TAG, "Field name: file");
        Log.d(TAG, "Media type: audio/3gpp");
        Log.d(TAG, "======================");

        MediaType mediaType = MediaType.parse("audio/wav");
        RequestBody fileBody = RequestBody.create(audioFile, mediaType);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/convert")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response";

                if (!response.isSuccessful()) {
                    Log.e(TAG, "=== SERVER ERROR ===");
                    Log.e(TAG, "Status: " + response.code());
                    Log.e(TAG, "Message: " + response.message());
                    Log.e(TAG, "Body: " + responseBody);
                    Log.e(TAG, "===================");
                    callback.onError("Server error " + response.code() + ": " + responseBody);
                    return;
                }

                try {
                    Log.d(TAG, "Response: " + responseBody);
                    JSONObject json = new JSONObject(responseBody);

                    if (json.getBoolean("success")) {
                        String jobId = json.getString("job_id");
                        downloadCatVoice(jobId, callback);
                    } else {
                        callback.onError("Conversion failed");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void convertToCatVoice(File audioFile, ConversionCallback callback) {
        convertToCatVoice(audioFile, 350, 700, 0.7f, 0.8f, callback);
    }

    private void downloadCatVoice(String jobId, ConversionCallback callback) {
        Log.d(TAG, "=== DOWNLOAD REQUEST ===");
        Log.d(TAG, "Job ID: " + jobId);
        Log.d(TAG, "URL: " + BASE_URL + "/download/" + jobId);
        Log.d(TAG, "========================");

        Request request = new Request.Builder()
                .url(BASE_URL + "/download/" + jobId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "=== DOWNLOAD FAILED ===");
                Log.e(TAG, "Error: " + e.getMessage());
                Log.e(TAG, "=======================");
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "=== DOWNLOAD RESPONSE ===");
                Log.d(TAG, "Status: " + response.code());
                Log.d(TAG, "Message: " + response.message());
                Log.d(TAG, "=========================");

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response";
                    Log.e(TAG, "Download error body: " + errorBody);
                    callback.onError("Download error: " + response.code() + " - " + errorBody);
                    return;
                }

                if (response.body() == null) {
                    Log.e(TAG, "Response body is null");
                    callback.onError("Empty response body");
                    return;
                }

                File outputFile = new File(context.getCacheDir(), "cat_voice_" + jobId + ".wav");
                Log.d(TAG, "Saving to: " + outputFile.getAbsolutePath());

                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytes = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                    }

                    Log.d(TAG, "Downloaded " + totalBytes + " bytes");
                    Log.d(TAG, "File saved: " + outputFile.exists());

                    callback.onSuccess(outputFile);
                    cleanupServerFile(jobId);

                } catch (IOException e) {
                    Log.e(TAG, "File save error: " + e.getMessage());
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    private void cleanupServerFile(String jobId) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/cleanup/" + jobId)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) {}
        });
    }

    public void checkHealth(HealthCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/health")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onHealthCheck(false, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onHealthCheck(response.isSuccessful(),
                    response.isSuccessful() ? "API is healthy" : "API error: " + response.code());
            }
        });
    }

    public void textToCatSpeech(String text, ConversionCallback callback) {
        Log.d(TAG, "Converting text to cat speech: " + text);

        RequestBody requestBody = new okhttp3.FormBody.Builder()
                .add("text", text)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/tts")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "TTS request failed: " + e.getMessage());
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response";

                if (!response.isSuccessful()) {
                    Log.e(TAG, "TTS error " + response.code() + ": " + responseBody);
                    callback.onError("TTS error " + response.code());
                    return;
                }

                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getBoolean("success")) {
                        String jobId = json.getString("job_id");
                        downloadCatVoice(jobId, callback);
                    } else {
                        callback.onError("TTS failed");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public interface ConversionCallback {
        void onSuccess(File catVoiceFile);
        void onError(String error);
    }

    public interface HealthCallback {
        void onHealthCheck(boolean isHealthy, String message);
    }
}
