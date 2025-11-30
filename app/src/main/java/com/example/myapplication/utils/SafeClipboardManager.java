package com.example.myapplication.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.TransactionTooLargeException;

public class SafeClipboardManager {
    private static final int MAX_CLIPBOARD_SIZE = 100000;

    public static boolean copyToClipboard(Context context, String label, String text) {
        if (text == null || text.length() > MAX_CLIPBOARD_SIZE) {
            return false;
        }
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
            return true;
        } catch (Exception e) {
            if (e.getCause() instanceof TransactionTooLargeException) {
                return false;
            }
            throw e;
        }
    }
}
