package com.milkdiary.app.model;

public class SupplierProfile {
    private long id;
    private long userId;
    private long supplierId;
    private String village;
    private String milkType;

    public SupplierProfile() {}

    public SupplierProfile(long userId, long supplierId, String village, String milkType) {
        this.userId = userId;
        this.supplierId = supplierId;
        this.village = village;
        this.milkType = milkType;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getSupplierId() { return supplierId; }
    public void setSupplierId(long supplierId) { this.supplierId = supplierId; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getMilkType() { return milkType; }
    public void setMilkType(String milkType) { this.milkType = milkType; }
}
