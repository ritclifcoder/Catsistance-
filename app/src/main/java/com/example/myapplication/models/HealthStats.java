package com.example.myapplication.models;

public class HealthStats {
    private int steps;
    private double sleepHours;
    
    public HealthStats() {}
    
    public HealthStats(int steps, double sleepHours) {
        this.steps = steps;
        this.sleepHours = sleepHours;
    }
    
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    
    public double getSleepHours() { return sleepHours; }
    public void setSleepHours(double sleepHours) { this.sleepHours = sleepHours; }
}
