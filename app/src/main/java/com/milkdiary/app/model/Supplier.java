package com.milkdiary.app.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Supplier {
    public static final String MILK_COW = "cow";
    public static final String MILK_BUFFALO = "buffalo";
    public static final String MILK_BOTH = "both";

    private long id;
    private String name;
    private String phone;
    private String village;
    private String milkType;
    private double defaultCowRate;
    private double defaultBuffaloRate;
    private boolean isActive;
    private String createdAt;

    public Supplier() {
        this.isActive = true;
        this.milkType = MILK_BOTH;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public Supplier(String name, String phone, String village, String milkType,
                    double defaultCowRate, double defaultBuffaloRate) {
        this();
        this.name = name;
        this.phone = phone;
        this.village = village;
        this.milkType = milkType;
        this.defaultCowRate = defaultCowRate;
        this.defaultBuffaloRate = defaultBuffaloRate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getMilkType() { return milkType; }
    public void setMilkType(String milkType) { this.milkType = milkType; }
    public double getDefaultCowRate() { return defaultCowRate; }
    public void setDefaultCowRate(double rate) { this.defaultCowRate = rate; }
    public double getDefaultBuffaloRate() { return defaultBuffaloRate; }
    public void setDefaultBuffaloRate(double rate) { this.defaultBuffaloRate = rate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public double getRateForMilkType(String milkType) {
        if (MILK_COW.equals(milkType)) return defaultCowRate;
        if (MILK_BUFFALO.equals(milkType)) return defaultBuffaloRate;
        return 0;
    }
}
