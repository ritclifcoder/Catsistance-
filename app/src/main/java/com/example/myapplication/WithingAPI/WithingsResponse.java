package com.example.myapplication.WithingAPI;

import java.util.List;

public class WithingsResponse {
    private int status;
    private Body body;
    
    public int getStatus() { return status; }
    public Body getBody() { return body; }
    
    public static class Body {
        private List<MeasureGroup> measuregrps;
        
        public List<MeasureGroup> getMeasuregrps() { return measuregrps; }
    }
    
    public static class MeasureGroup {
        private List<Measure> measures;
        
        public List<Measure> getMeasures() { return measures; }
    }
    
    public static class Measure {
        private int value;
        private int type;
        private int unit;
        
        public int getValue() { return value; }
        public int getType() { return type; }
        public int getUnit() { return unit; }
        
        public double getRealValue() {
            return value * Math.pow(10, unit);
        }
    }
    
    public HealthData toHealthData() {
        HealthData data = new HealthData();
        
        if (body == null || body.getMeasuregrps() == null || body.getMeasuregrps().isEmpty()) {
            return data;
        }
        
        boolean weightFound = false;
        boolean bpFound = false;
        
        // Process all groups, but take first occurrence of each type
        for (MeasureGroup group : body.getMeasuregrps()) {
            if (group.getMeasures() == null) continue;
            
            for (Measure measure : group.getMeasures()) {
                switch (measure.getType()) {
                    case 1: // Weight
                        if (!weightFound) {
                            data.setWeight(measure.getRealValue());
                            weightFound = true;
                        }
                        break;
                    case 9: // Diastolic
                        if (!bpFound) {
                            if (data.getBloodPressure() == null) {
                                data.setBloodPressure(new HealthData.BloodPressure());
                            }
                            data.getBloodPressure().setDiastolic((int) measure.getRealValue());
                        }
                        break;
                    case 10: // Systolic
                        if (!bpFound) {
                            if (data.getBloodPressure() == null) {
                                data.setBloodPressure(new HealthData.BloodPressure());
                            }
                            data.getBloodPressure().setSystolic((int) measure.getRealValue());
                            bpFound = true;
                        }
                        break;
                    case 11: // Heart Rate
                        data.setHeartRate((int) measure.getRealValue());
                        break;
                }
            }
        }
        
        return data;
    }
}
