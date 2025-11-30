package com.example.myapplication.AmazonBedrockService;

import java.util.*;

public class GroupRankingSystem {
    
    public enum Tier {
        SILVER(1), GOLD(2), MASTER(3), ELITE(4);
        private final int level;
        Tier(int level) { this.level = level; }
        public int getLevel() { return level; }
    }
    
    public static class Person {
        String name;
        int steps;
        double sleepHours;
        int heartRate;
        
        public Person(String name, int steps, double sleepHours, int heartRate) {
            this.name = name;
            this.steps = steps;
            this.sleepHours = sleepHours;
            this.heartRate = heartRate;
        }
    }
    
    public static class Group {
        Tier tier;
        List<Person> members;
        
        public Group(Tier tier) {
            this.tier = tier;
            this.members = new ArrayList<>();
        }
        
        public double getAvgSteps() {
            return members.stream().mapToInt(p -> p.steps).average().orElse(0);
        }
        
        public double getAvgSleep() {
            return members.stream().mapToDouble(p -> p.sleepHours).average().orElse(0);
        }
        
        public double getAvgHeartRate() {
            return members.stream().mapToInt(p -> p.heartRate).average().orElse(0);
        }
        
        public double getPerformanceScore() {
            return (getAvgSteps() / 100) + (getAvgSleep() * 15) + (80 - Math.abs(70 - getAvgHeartRate()));
        }
    }
    
    public static class Assessment {
        String summary;
        List<String> anomalies;
        List<String> suggestions;
        
        public Assessment() {
            this.anomalies = new ArrayList<>();
            this.suggestions = new ArrayList<>();
        }
    }
    
    public static List<Group> getMockData() {
        List<Group> groups = new ArrayList<>();
        
        Group silver = new Group(Tier.SILVER);
        silver.members.add(new Person("Alice", 12000, 8.5, 68));
        silver.members.add(new Person("Bob", 11500, 8.0, 72));
        silver.members.add(new Person("Charlie", 13000, 7.5, 70));
        groups.add(silver);
        
        Group gold = new Group(Tier.GOLD);
        gold.members.add(new Person("David", 8500, 6.5, 75));
        gold.members.add(new Person("Emma", 9000, 7.0, 73));
        gold.members.add(new Person("Frank", 8000, 6.0, 78));
        groups.add(gold);
        
        Group master = new Group(Tier.MASTER);
        master.members.add(new Person("Grace", 10000, 7.5, 71));
        master.members.add(new Person("Henry", 10500, 7.8, 69));
        master.members.add(new Person("Ivy", 9800, 7.2, 72));
        groups.add(master);
        
        Group elite = new Group(Tier.ELITE);
        elite.members.add(new Person("Jack", 14000, 8.5, 68));
        elite.members.add(new Person("Kate", 15000, 9.0, 66));
        elite.members.add(new Person("Leo", 14500, 8.8, 67));
        groups.add(elite);
        
        return groups;
    }
    
    public static Assessment assessGroups(List<Group> groups) {
        Assessment assessment = new Assessment();
        groups.sort((a, b) -> Integer.compare(a.tier.getLevel(), b.tier.getLevel()));
        
        for (int i = 0; i < groups.size() - 1; i++) {
            Group lower = groups.get(i);
            Group higher = groups.get(i + 1);
            
            if (lower.getPerformanceScore() > higher.getPerformanceScore()) {
                assessment.anomalies.add(lower.tier + " outperforming " + higher.tier);
                
                if (lower.getAvgSteps() > higher.getAvgSteps()) {
                    assessment.suggestions.add("📊 " + higher.tier + " needs step boost: " + 
                        (int)lower.getAvgSteps() + " vs " + (int)higher.getAvgSteps());
                }
                
                if (lower.getAvgSleep() > higher.getAvgSleep() + 0.5) {
                    assessment.suggestions.add("😴 " + higher.tier + " sleep intervention needed");
                }
                
                assessment.suggestions.add("🔄 Review " + higher.tier + " tier requirements");
                assessment.suggestions.add("🎯 Promote top " + lower.tier + " members to " + higher.tier);
            }
        }
        
        if (assessment.anomalies.isEmpty()) {
            assessment.summary = "✅ All groups performing as expected";
        } else {
            assessment.summary = "⚠️ Anomalies: " + String.join(", ", assessment.anomalies);
        }
        
        return assessment;
    }
}
