package com.example.meteors.plantheal;

public class Officer1 {
    private String name;
    private String location;
    private boolean is_available;

    public Officer1() {
        // Default constructor required for calls to DataSnapshot.getValue(Officer.class)
    }

    public Officer1(String name, String location, boolean is_available) {
        this.name = name;
        this.location = location;
        this.is_available = is_available;
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
}
