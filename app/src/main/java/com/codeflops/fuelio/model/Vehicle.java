package com.codeflops.fuelio.model;

public class Vehicle {
    private String id;
    private String brand;
    private String model;
    private String version;


    public Vehicle() {

    }

    public Vehicle(String id, String brand, String model, String version) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.version = version;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getDisplayName() {
        return String.format("%s %s (%s)", brand, model, version);
    }
}
