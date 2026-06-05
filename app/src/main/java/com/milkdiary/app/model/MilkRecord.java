package com.milkdiary.app.model;

public class MilkRecord {
    private long id;
    private String date;        // YYYY-MM-DD
    private double cowLiters;
    private double cowRate;
    private double cowAmount;
    private double buffaloLiters;
    private double buffaloRate;
    private double buffaloAmount;
    private double total;
    private String note;

    public MilkRecord() {}

    public MilkRecord(String date, double cowLiters, double cowRate,
                      double buffaloLiters, double buffaloRate, String note) {
        this.date = date;
        this.cowLiters = cowLiters;
        this.cowRate = cowRate;
        this.cowAmount = cowLiters * cowRate;
        this.buffaloLiters = buffaloLiters;
        this.buffaloRate = buffaloRate;
        this.buffaloAmount = buffaloLiters * buffaloRate;
        this.total = this.cowAmount + this.buffaloAmount;
        this.note = note;
    }

    public void recalculate() {
        this.cowAmount = cowLiters * cowRate;
        this.buffaloAmount = buffaloLiters * buffaloRate;
        this.total = cowAmount + buffaloAmount;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

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
}
