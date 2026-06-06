package com.milkdiary.app.model;

public class Payment {
    public static final String PARTY_SUPPLIER = "supplier";
    public static final String PARTY_CUSTOMER = "customer";

    private long id;
    private String date;
    private double amount;
    private String note;
    private String partyType;
    private long partyId;
    private String partyName;

    public Payment() {
        this.partyType = PARTY_CUSTOMER;
    }

    public Payment(String date, double amount, String note, String partyType, long partyId) {
        this.date = date;
        this.amount = amount;
        this.note = note;
        this.partyType = partyType;
        this.partyId = partyId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getPartyType() { return partyType; }
    public void setPartyType(String partyType) { this.partyType = partyType; }
    public long getPartyId() { return partyId; }
    public void setPartyId(long partyId) { this.partyId = partyId; }
    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public boolean isSupplierPayment() { return PARTY_SUPPLIER.equals(partyType); }
    public boolean isCustomerPayment() { return !isSupplierPayment(); }

    public String getPartyLabel() {
        return isSupplierPayment() ? "To Supplier" : "From Customer";
    }
}
