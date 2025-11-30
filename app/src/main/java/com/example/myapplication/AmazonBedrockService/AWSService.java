package com.example.myapplication.AmazonBedrockService;

import java.util.Map;

public class AWSService {
    
    public static Map<String, String> getBedrockHeaders(String payload) {
        return AWSConfig.getAuthHeaders(payload);
    }
    
    public static String getBedrockUrl() {
        return "https://bedrock-runtime." + AWSConfig.AWS_REGION + ".amazonaws.com/model/amazon.titan-text-express-v1/invoke";
    }
}