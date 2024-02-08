package com.example.meteors.plantheal;

public class Officer {
    private String name;
    private String location;
    private boolean is_available;
    private String phoneNumber;

    public Officer() {
        // Default constructor required for calls to DataSnapshot.getValue(Officer.class)
    }

    public Officer(String name, String location, boolean is_available, String phoneNumber) {
        this.name = name;
        this.location = location;
        this.is_available = is_available;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return is_available;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


}
