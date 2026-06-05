package com.milkdiary.app.model;

public class Payment {
    private long id;
    private String date;    // YYYY-MM-DD
    private double amount;
    private String note;

    public Payment() {}

    public Payment(String date, double amount, String note) {
        this.date = date;
        this.amount = amount;
        this.note = note;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
