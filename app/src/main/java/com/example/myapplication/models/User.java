package com.example.myapplication.models;

public class User {
    private String userId;
    private String name;
    private String email;
    private int points;
    private String group;
    private HealthStats healthStats;
    private Vitals vitals;
    
    public User() {}
    
    public User(String userId, String name, String email, int points, String group, HealthStats healthStats, Vitals vitals) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.points = points;
        this.group = group;
        this.healthStats = healthStats;
        this.vitals = vitals;
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    
    public HealthStats getHealthStats() { return healthStats; }
    public void setHealthStats(HealthStats healthStats) { this.healthStats = healthStats; }
    
    public Vitals getVitals() { return vitals; }
    public void setVitals(Vitals vitals) { this.vitals = vitals; }
}
