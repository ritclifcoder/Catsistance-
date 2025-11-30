package com.example.myapplication.utils;

import android.content.Context;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.util.Collections;

public class GoogleAuthHelper {

    public static String getAccessToken(Context context, String jsonFileName) throws Exception {
        InputStream stream = context.getAssets().open(jsonFileName);
        GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}