package com.milkdiary.app.model;

public class MonthlySummary {
    private double totalCowLiters;
    private double totalBuffaloLiters;
    private double totalLiters;
    private double totalEarnings;
    private double avgDailyEarnings;
    private int recordCount;

    public MonthlySummary() {}

    public double getTotalCowLiters() { return totalCowLiters; }
    public void setTotalCowLiters(double totalCowLiters) { this.totalCowLiters = totalCowLiters; }

    public double getTotalBuffaloLiters() { return totalBuffaloLiters; }
    public void setTotalBuffaloLiters(double totalBuffaloLiters) { this.totalBuffaloLiters = totalBuffaloLiters; }

    public double getTotalLiters() { return totalLiters; }
    public void setTotalLiters(double totalLiters) { this.totalLiters = totalLiters; }

    public double getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }

    public double getAvgDailyEarnings() { return avgDailyEarnings; }
    public void setAvgDailyEarnings(double avgDailyEarnings) { this.avgDailyEarnings = avgDailyEarnings; }

    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }
}
