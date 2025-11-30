package com.example.myapplication.AmazonBedrockService;

import android.content.Context;
import com.example.myapplication.WithingAPI.HealthData;

import java.util.concurrent.CompletableFuture;

public class AmazonQChatBot {
    
    public static String generateHealthAdvice(HealthData healthData) {
        return generateLocalAdvice(healthData);
    }
    
    public static CompletableFuture<String> generateAWSQAdvice(Context context, HealthData healthData) {
        AWSQService awsQService = new AWSQService(context);
        return awsQService.generateHealthResponse(healthData);
    }
    
    private static String generateLocalAdvice(HealthData healthData) {
        StringBuilder advice = new StringBuilder();
        
        // Blood pressure analysis
        if (healthData.getBloodPressure() != null) {
            int systolic = healthData.getBloodPressure().getSystolic();
            int diastolic = healthData.getBloodPressure().getDiastolic();
            
            if (systolic > 140 || diastolic > 90) {
                advice.append("🩺 Your blood pressure (").append(systolic).append("/").append(diastolic)
                      .append(") is elevated. Consider reducing salt intake and consulting your doctor. ");
            } else if (systolic < 90 || diastolic < 60) {
                advice.append("🩺 Your blood pressure seems low. Stay hydrated and avoid sudden movements. ");
            } else {
                advice.append("✅ Great! Your blood pressure (").append(systolic).append("/").append(diastolic)
                      .append(") is in healthy range. ");
            }
        }
        
        // Steps analysis
        if (healthData.getStepsToday() < 5000) {
            advice.append("🚶 You've walked ").append(String.format("%,d", healthData.getStepsToday()))
                  .append(" steps today. Try to reach 10,000 steps for better health! ");
        } else if (healthData.getStepsToday() >= 10000) {
            advice.append("🎉 Excellent! ").append(String.format("%,d", healthData.getStepsToday()))
                  .append(" steps today - you're very active! ");
        } else {
            advice.append("👍 Good job! ").append(String.format("%,d", healthData.getStepsToday()))
                  .append(" steps today. Keep it up! ");
        }
        
        // Sleep analysis
        if (healthData.getSleep() != null) {
            int totalMinutes = healthData.getSleep().getHours() * 60 + healthData.getSleep().getMinutes();
            if (totalMinutes < 420) { // Less than 7 hours
                advice.append("😴 You slept ").append(healthData.getSleep().toString())
                      .append(". Try to get 7-9 hours for better recovery. ");
            } else if (totalMinutes > 540) { // More than 9 hours
                advice.append("😴 You slept ").append(healthData.getSleep().toString())
                      .append(". That's quite a lot! Quality matters more than quantity. ");
            } else {
                advice.append("✨ Perfect! ").append(healthData.getSleep().toString())
                      .append(" of sleep is ideal for your health. ");
            }
        }
        
        // Weight analysis
        if (healthData.getWeight() > 0) {
            if (healthData.getWeight() < 50) {
                advice.append("⚖️ Your weight is ").append(String.format("%.1f kg", healthData.getWeight()))
                      .append(". Consider consulting a nutritionist. ");
            } else if (healthData.getWeight() > 100) {
                advice.append("⚖️ Your weight is ").append(String.format("%.1f kg", healthData.getWeight()))
                      .append(". Maintain a balanced diet and exercise. ");
            } else {
                advice.append("⚖️ Your weight is ").append(String.format("%.1f kg", healthData.getWeight()))
                      .append(". Keep maintaining a healthy lifestyle! ");
            }
        }
        
        return advice.toString().trim();
    }
}