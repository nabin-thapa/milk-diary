package com.milkdiary.app.model;

public class Inventory {
    private long id;
    private String date;
    private double morningCollected;
    private double eveningCollected;
    private double totalCollected;
    private double sold;
    private double waste;
    private double closingStock;
    private String note;

    public Inventory() {}

    public Inventory(String date) {
        this.date = date;
    }

    public void calculateClosing() {
        this.totalCollected = this.morningCollected + this.eveningCollected;
        this.closingStock = this.totalCollected - this.sold - this.waste;
        if (this.closingStock < 0) this.closingStock = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getMorningCollected() { return morningCollected; }
    public void setMorningCollected(double v) { this.morningCollected = v; }
    public double getEveningCollected() { return eveningCollected; }
    public void setEveningCollected(double v) { this.eveningCollected = v; }
    public double getTotalCollected() { return totalCollected; }
    public void setTotalCollected(double v) { this.totalCollected = v; }
    public double getSold() { return sold; }
    public void setSold(double v) { this.sold = v; }
    public double getWaste() { return waste; }
    public void setWaste(double waste) { this.waste = waste; }
    public double getClosingStock() { return closingStock; }
    public void setClosingStock(double v) { this.closingStock = v; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
