package com.example.myapplication.AmazonBedrockService;

public class TestGroupAssessment {
    
    public static void main(String[] args) {
        BedrockService service = new BedrockService();
        
        service.assessGroupRankings(response -> {
            System.out.println(response);
        });
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
