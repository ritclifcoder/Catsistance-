package com.example.myapplication.WithingAPI;

public class HealthData {
    private BloodPressure bloodPressure;
    private int stepsToday;
    private Sleep sleep;
    private double weight;
    private int heartRate;

    public HealthData() {}

    public HealthData(BloodPressure bloodPressure, int stepsToday, Sleep sleep, double weight) {
        this.bloodPressure = bloodPressure;
        this.stepsToday = stepsToday;
        this.sleep = sleep;
        this.weight = weight;
    }

    // Getters and Setters
    public BloodPressure getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(BloodPressure bloodPressure) { this.bloodPressure = bloodPressure; }

    public int getStepsToday() { return stepsToday; }
    public void setStepsToday(int stepsToday) { this.stepsToday = stepsToday; }

    public Sleep getSleep() { return sleep; }
    public void setSleep(Sleep sleep) { this.sleep = sleep; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public static class BloodPressure {
        private int systolic;
        private int diastolic;

        public BloodPressure() {}

        public BloodPressure(int systolic, int diastolic) {
            this.systolic = systolic;
            this.diastolic = diastolic;
        }

        public int getSystolic() { return systolic; }
        public void setSystolic(int systolic) { this.systolic = systolic; }

        public int getDiastolic() { return diastolic; }
        public void setDiastolic(int diastolic) { this.diastolic = diastolic; }

        @Override
        public String toString() {
            return systolic + "/" + diastolic;
        }
    }

    public static class Sleep {
        private int hours;
        private int minutes;

        public Sleep() {}

        public Sleep(int hours, int minutes) {
            this.hours = hours;
            this.minutes = minutes;
        }

        public int getHours() { return hours; }
        public void setHours(int hours) { this.hours = hours; }

        public int getMinutes() { return minutes; }
        public void setMinutes(int minutes) { this.minutes = minutes; }

        @Override
        public String toString() {
            return hours + "h " + minutes + "m";
        }
    }
}