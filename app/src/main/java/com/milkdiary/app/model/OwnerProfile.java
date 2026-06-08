package com.milkdiary.app.model;

public class OwnerProfile {
    private long id;
    private long userId;
    private String dairyName;
    private String address;

    public OwnerProfile() {}

    public OwnerProfile(long userId, String dairyName, String address) {
        this.userId = userId;
        this.dairyName = dairyName;
        this.address = address;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getDairyName() { return dairyName; }
    public void setDairyName(String dairyName) { this.dairyName = dairyName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
