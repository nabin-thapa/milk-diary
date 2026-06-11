package com.milkdiary.app.model;

public class MilkRecord {
    public static final String SESSION_MORNING = "morning";
    public static final String SESSION_EVENING = "evening";
    public static final int MAX_EDIT_COUNT = 1;

    private long id;
    private String date;          // YYYY-MM-DD
    private String sessionType;   // "morning" or "evening"
    private double cowLiters;
    private double cowRate;
    private double cowAmount;
    private double buffaloLiters;
    private double buffaloRate;
    private double buffaloAmount;
    private double total;
    private String note;
    private int editCount;
    private long supplierId;
    private double fatPercentage;
    private boolean isPaid;

    public MilkRecord() {}

    public MilkRecord(String date, String sessionType, double cowLiters, double cowRate,
                      double buffaloLiters, double buffaloRate, String note) {
        this.date = date;
        this.sessionType = sessionType;
        this.cowLiters = cowLiters;
        this.cowRate = cowRate;
        this.cowAmount = cowLiters * cowRate;
        this.buffaloLiters = buffaloLiters;
        this.buffaloRate = buffaloRate;
        this.buffaloAmount = buffaloLiters * buffaloRate;
        this.total = this.cowAmount + this.buffaloAmount;
        this.note = note;
        this.editCount = 0;
    }

    public void recalculate() {
        this.cowAmount = cowLiters * cowRate;
        this.buffaloAmount = buffaloLiters * buffaloRate;
        this.total = cowAmount + buffaloAmount;
    }

    public boolean canEdit() {
        return editCount < MAX_EDIT_COUNT;
    }

    public void incrementEditCount() {
        this.editCount++;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public boolean isMorning() { return SESSION_MORNING.equals(sessionType); }
    public boolean isEvening() { return SESSION_EVENING.equals(sessionType); }

    public double getCowLiters() { return cowLiters; }
    public void setCowLiters(double cowLiters) { this.cowLiters = cowLiters; }

    public double getCowRate() { return cowRate; }
    public void setCowRate(double cowRate) { this.cowRate = cowRate; }

    public double getCowAmount() { return cowAmount; }
    public void setCowAmount(double cowAmount) { this.cowAmount = cowAmount; }

    public double getBuffaloLiters() { return buffaloLiters; }
    public void setBuffaloLiters(double buffaloLiters) { this.buffaloLiters = buffaloLiters; }

    public double getBuffaloRate() { return buffaloRate; }
    public void setBuffaloRate(double buffaloRate) { this.buffaloRate = buffaloRate; }

    public double getBuffaloAmount() { return buffaloAmount; }
    public void setBuffaloAmount(double buffaloAmount) { this.buffaloAmount = buffaloAmount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getEditCount() { return editCount; }
    public void setEditCount(int editCount) { this.editCount = editCount; }

    public long getSupplierId() { return supplierId; }
    public void setSupplierId(long supplierId) { this.supplierId = supplierId; }

    public double getFatPercentage() { return fatPercentage; }
    public void setFatPercentage(double fatPercentage) { this.fatPercentage = fatPercentage; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
}
