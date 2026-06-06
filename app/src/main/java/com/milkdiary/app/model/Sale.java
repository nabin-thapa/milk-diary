package com.milkdiary.app.model;

public class Sale {
    public static final String CUSTOMER_HOTEL = "hotel";
    public static final String CUSTOMER_TEA_SHOP = "tea_shop";
    public static final String CUSTOMER_HOME = "home";
    public static final String CUSTOMER_OTHER = "other";

    public static final String MILK_COW = "cow";
    public static final String MILK_BUFFALO = "buffalo";

    private long id;
    private String date;
    private String sessionType;
    private String customerName;
    private String customerType;
    private String milkType;
    private double quantity;
    private double rate;
    private double amount;
    private boolean isPaid;
    private String note;
    private String createdAt;

    public Sale() {
        this.customerType = CUSTOMER_OTHER;
        this.isPaid = false;
    }

    public Sale(String date, String sessionType, String customerName, String customerType,
                String milkType, double quantity, double rate, boolean isPaid, String note) {
        this();
        this.date = date;
        this.sessionType = sessionType;
        this.customerName = customerName;
        this.customerType = customerType;
        this.milkType = milkType;
        this.quantity = quantity;
        this.rate = rate;
        this.amount = quantity * rate;
        this.isPaid = isPaid;
        this.note = note;
    }

    public void recalculate() {
        this.amount = this.quantity * this.rate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    public String getMilkType() { return milkType; }
    public void setMilkType(String milkType) { this.milkType = milkType; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
