package com.example.myapplication.AmazonBedrockService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

public class AWSConfig {
    // TODO: Replace with your valid AWS credentials
    public static final String AWS_ACCESS_KEY_ID = "AKIA2DNFYY2R757JNMS3";
    public static final String AWS_SECRET_ACCESS_KEY = "xG3oPp54i/XoWuOZMMBw1dsOEjPalfSvdhXDmJ6e";
    public static final String AWS_REGION = "eu-central-1";
    public static final String SERVICE = "bedrock";
    
    public static Map<String, String> getAuthHeaders(String payload) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String timestamp = sdf.format(new Date());
            String date = timestamp.substring(0, 8);
            
            String host = "bedrock-runtime." + AWS_REGION + ".amazonaws.com";
            String uri = "/model/amazon.titan-text-express-v1/invoke";
            String credentialScope = date + "/" + AWS_REGION + "/" + SERVICE + "/aws4_request";
            String algorithm = "AWS4-HMAC-SHA256";
            
            String payloadHash = sha256Hex(payload);
            
            String canonicalHeaders = "host:" + host + "\n" +
                                      "x-amz-date:" + timestamp + "\n";
            
            String signedHeaders = "host;x-amz-date";
            
            String canonicalRequest = "POST\n" + uri + "\n\n" + 
                                      canonicalHeaders + "\n" + 
                                      signedHeaders + "\n" + 
                                      payloadHash;
            
            String stringToSign = algorithm + "\n" + timestamp + "\n" + 
                                 credentialScope + "\n" + 
                                 sha256Hex(canonicalRequest);
            
            byte[] signingKey = getSignatureKey(AWS_SECRET_ACCESS_KEY, date, AWS_REGION, SERVICE);
            String signature = bytesToHex(hmacSHA256(stringToSign, signingKey));
            
            String authorization = algorithm + " Credential=" + AWS_ACCESS_KEY_ID + "/" + 
                                  credentialScope + ", SignedHeaders=" + signedHeaders + 
                                  ", Signature=" + signature;
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", authorization);
            headers.put("X-Amz-Date", timestamp);
            headers.put("Content-Type", "application/json");
            headers.put("Host", host);
            return headers;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kDate = hmacSHA256(dateStamp, ("AWS4" + key).getBytes("UTF-8"));
        byte[] kRegion = hmacSHA256(regionName, kDate);
        byte[] kService = hmacSHA256(serviceName, kRegion);
        return hmacSHA256("aws4_request", kService);
    }
    
    private static byte[] hmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes("UTF-8"));
    }
    
    private static String sha256Hex(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes("UTF-8"));
        return bytesToHex(hash);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
