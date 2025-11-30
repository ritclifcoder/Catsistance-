package com.example.myapplication.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Base64;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TTSHelperREST {

    public static void speakText(final Context context, final String text, final boolean female, final boolean child) {
        new Thread(() -> {
            try {
                // Assets'teki JSON key (Cloud Auth) okuyup OAuth token alabiliriz
                // Basitlik için, service account key ile Access Token üreten helper kullanabiliriz
                String accessToken = GoogleAuthHelper.getAccessToken(context, "fir-adapterrecyclerview-1f230-9c71c990aa98.json");

                // REST API isteği
                URL url = new URL("https://texttospeech.googleapis.com/v1/text:synthesize");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Pitch ve speed ayarı
                double pitch = 16.0;   // normal ton
                double rate = 0.8;    // normal hız
                // biraz daha hızlı → kedi mırıltısı gibi tını

                String gender = female ? "FEMALE" : "MALE";

                JSONObject json = new JSONObject();
                JSONObject input = new JSONObject();
                input.put("text", text);
                JSONObject voice = new JSONObject();
                voice.put("languageCode", "en-US");
                voice.put("ssmlGender", gender);
                JSONObject audioConfig = new JSONObject();
                audioConfig.put("audioEncoding", "MP3");
                audioConfig.put("pitch", pitch);
                audioConfig.put("speakingRate", rate);

                json.put("input", input);
                json.put("voice", voice);
                json.put("audioConfig", audioConfig);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                InputStream is = conn.getInputStream();
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = is.read()) != -1) sb.append((char) ch);
                is.close();

                JSONObject response = new JSONObject(sb.toString());
                String audioContent = response.getString("audioContent");

                byte[] audioBytes = Base64.decode(audioContent, Base64.DEFAULT);
                File outFile = new File(context.getCacheDir(), "tts_rest.mp3");
                FileOutputStream fos = new FileOutputStream(outFile);
                fos.write(audioBytes);
                fos.close();

                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(outFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
