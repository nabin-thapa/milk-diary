package com.milkdiary.app.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Customer {
    public static final String TYPE_HOTEL = "hotel";
    public static final String TYPE_TEA_SHOP = "tea_shop";
    public static final String TYPE_HOME = "home";
    public static final String TYPE_OTHER = "other";

    private long id;
    private String name;
    private String phone;
    private String customerType;
    private String address;
    private String createdAt;

    public Customer() {
        this.customerType = TYPE_OTHER;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public Customer(String name, String phone, String customerType, String address) {
        this();
        this.name = name;
        this.phone = phone;
        this.customerType = customerType;
        this.address = address;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
