package com.example.myapplication.models;

public class Vitals {
    private int systolic;
    private int diastolic;
    private int heartRate;
    
    public Vitals() {}
    
    public Vitals(int systolic, int diastolic, int heartRate) {
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.heartRate = heartRate;
    }
    
    public int getSystolic() { return systolic; }
    public void setSystolic(int systolic) { this.systolic = systolic; }
    
    public int getDiastolic() { return diastolic; }
    public void setDiastolic(int diastolic) { this.diastolic = diastolic; }
    
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }
}
