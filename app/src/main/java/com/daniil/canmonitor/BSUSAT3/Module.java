package com.daniil.canmonitor.BSUSAT3;

public class Module {
    private String name;
    private boolean isActive;
    private double voltage;
    private double current;
    public Module(String name,boolean isActive, double voltage, double current) {
        this.name = name;
        this.isActive = isActive;
        this.voltage = voltage;
        this.current = current;
    }

    public boolean getisActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }
}
