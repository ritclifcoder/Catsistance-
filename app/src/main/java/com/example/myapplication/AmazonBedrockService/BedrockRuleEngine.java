package com.example.myapplication.AmazonBedrockService;

import java.util.*;

public class BedrockRuleEngine {
    
    private static final String[] RULE_BASED_MESSAGES = {
        "🏃 Silver group is crushing it with 12,000 daily steps while Gold averages only 8,500!",
        "⚠️ Performance anomaly detected: Lower tier outperforming higher tier by 35%",
        "💤 Sleep quality analysis shows Silver members getting 8+ hours vs Gold's 6.5 hours",
        "📊 Engagement metrics reveal Silver group has 30% higher app interaction rates",
        "🎯 Recommendation: Review Gold tier admission criteria immediately",
        "💡 Consider implementing step challenges specifically for Gold tier members",
        "🔄 Tier rebalancing suggested: Promote top 3 Silver performers to Gold",
        "⭐ Alice from Silver leads all groups with 13,000 steps and 8.5 hours sleep",
        "📉 Gold tier showing declining trends in all key health metrics this week",
        "🚨 Urgent: Gold group needs immediate intervention for sleep improvement",
        "💪 Silver demonstrates superior consistency in daily health tracking",
        "📱 Push notification strategy needed for underperforming Gold members",
        "🏆 Elite tier maintains expected performance with 14,500 average steps",
        "⚡ Master group shows steady improvement, approaching Elite benchmarks",
        "🎪 Group dynamics assessment reveals motivation gaps in Gold tier",
        "🔍 Deep analysis: Silver's success linked to peer support and competition",
        "📈 Trend prediction: Silver members likely to reach Gold standards within 2 weeks",
        "🌟 Bob from Silver shows 95% daily tracking compliance vs Gold's 60%",
        "💊 Health intervention priority: Gold tier cardiovascular activity boost needed",
        "🎨 Gamification elements working exceptionally well for Silver cohort",
        "🔔 Automated alerts configured for Gold members falling below minimum thresholds",
        "📊 Weekly report: Silver outperforms Gold in 8 out of 10 health categories",
        "🎯 Target action: Implement mentorship program pairing Silver with Gold members",
        "💬 Community engagement scores: Silver 85%, Gold 55%, Master 70%, Elite 90%",
        "🏃‍♀️ Charlie from Silver maintains perfect 30-day streak with 12,000+ steps",
        "⚠️ Risk assessment: Gold tier at risk of demotion if trends continue",
        "🌈 Wellness score calculation shows Silver averaging 87/100 vs Gold's 65/100",
        "🔥 Motivation analysis: Silver responds well to group challenges and rewards",
        "📉 Gold member David shows concerning decline: 8,500 to 7,200 steps in 2 weeks",
        "💡 AI recommendation: Personalized coaching sessions for Gold tier members",
        "🎪 Social dynamics: Silver has stronger peer accountability networks",
        "📱 App usage patterns: Silver checks health data 3x more frequently than Gold",
        "🏆 Performance ranking update: Silver #2 overall, Gold drops to #3 position",
        "⚡ Real-time alert: Emma from Gold missed sleep target 5 consecutive nights",
        "🎯 Intervention deployed: Custom workout plans sent to all Gold members",
        "🌟 Success factor identified: Silver group uses buddy system effectively",
        "📊 Comparative analysis: Silver sleep quality 25% better than Gold average",
        "💪 Strength assessment: Silver shows better heart rate recovery times",
        "🔍 Root cause analysis: Gold tier lacks consistent daily routine structure",
        "🚀 Action plan: 30-day intensive program designed specifically for Gold recovery"
    };
    
    public static List<String> getRuleBasedMessages() {
        return Arrays.asList(RULE_BASED_MESSAGES);
    }
}
