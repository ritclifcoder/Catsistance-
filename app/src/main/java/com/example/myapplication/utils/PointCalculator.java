package com.example.myapplication.utils;

import com.example.myapplication.models.HealthStats;
import com.example.myapplication.models.Vitals;

public class PointCalculator {
    
    private static final double STEPS_WEIGHT = 0.45;
    private static final double SLEEP_WEIGHT = 0.25;
    private static final double BP_WEIGHT = 0.20;
    private static final double HR_WEIGHT = 0.10;
    
    private static final int TARGET_STEPS = 10000;
    private static final double TARGET_SLEEP = 8.0;
    private static final int TARGET_SYSTOLIC = 120;
    private static final int TARGET_DIASTOLIC = 80;
    private static final int TARGET_HEART_RATE = 70;
    
    private static final int MAX_POINTS = 100000;
    
    public static int calculateTotalPoints(HealthStats healthStats, Vitals vitals) {
        double stepsScore = calculateStepsScore(healthStats.getSteps());
        double sleepScore = calculateSleepScore(healthStats.getSleepHours());
        double bpScore = calculateBPScore(vitals.getSystolic(), vitals.getDiastolic());
        double hrScore = calculateHRScore(vitals.getHeartRate());
        
        double totalScore = (stepsScore * STEPS_WEIGHT) + 
                           (sleepScore * SLEEP_WEIGHT) + 
                           (bpScore * BP_WEIGHT) + 
                           (hrScore * HR_WEIGHT);
        
        return (int) (totalScore * MAX_POINTS);
    }
    
    private static double calculateStepsScore(int steps) {
        if (steps >= TARGET_STEPS) return 1.0;
        return Math.min(1.0, (double) steps / TARGET_STEPS);
    }
    
    private static double calculateSleepScore(double sleepHours) {
        if (sleepHours >= 7 && sleepHours <= 9) return 1.0;
        if (sleepHours < 7) return Math.max(0, sleepHours / 7.0);
        return Math.max(0, 1.0 - ((sleepHours - 9) / 5.0));
    }
    
    private static double calculateBPScore(int systolic, int diastolic) {
        double systolicDiff = Math.abs(systolic - TARGET_SYSTOLIC);
        double diastolicDiff = Math.abs(diastolic - TARGET_DIASTOLIC);
        double avgDiff = (systolicDiff + diastolicDiff) / 2.0;
        return Math.max(0, 1.0 - (avgDiff / 40.0));
    }
    
    private static double calculateHRScore(int heartRate) {
        double diff = Math.abs(heartRate - TARGET_HEART_RATE);
        return Math.max(0, 1.0 - (diff / 50.0));
    }
}
