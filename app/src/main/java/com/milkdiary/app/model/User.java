package com.milkdiary.app.model;

public class User {
    public static final String ROLE_DAIRY = "dairy";
    public static final String ROLE_SUPPLIER = "supplier";

    private long id;
    private String fullName;
    private String phone;
    private String passwordHash;
    private String role;
    private String createdAt;

    public User() {}

    public User(String fullName, String phone, String passwordHash, String role) {
        this.fullName = fullName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isDairyOwner() { return ROLE_DAIRY.equals(role); }
    public boolean isSupplier() { return ROLE_SUPPLIER.equals(role); }
}
