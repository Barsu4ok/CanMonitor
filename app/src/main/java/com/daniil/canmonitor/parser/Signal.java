package com.daniil.canmonitor.parser;


public class Signal {

    private String messageId;
    private String signalString = "";
    private String name;
    private int startBit;
    private int length;
    private int byteOrder;
    private String valueType;
    private double factor;
    private double offset;
    private double min;
    private double max;
    private String unit;
    private String receivers;

    public Signal(String signalString,String messageId,String name, int startBit, int length, int byteOrder, String valueType,
                  double factor, double offset, double min, double max, String unit, String receivers) {
        this.signalString = signalString;
        this.messageId = messageId;
        this.name = name;
        this.startBit = startBit;
        this.length = length;
        this.byteOrder = byteOrder;
        this.valueType = valueType;
        this.factor = factor;
        this.offset = offset;
        this.min = min;
        this.max = max;
        this.unit = unit;
        this.receivers = receivers;
    }

    public String getName() {
        return name;
    }

    public int getStartBit() {
        return startBit;
    }

    public int getLength() {
        return length;
    }

    public int getByteOrder() {
        return byteOrder;
    }

    public double getFactor() {
        return factor;
    }

    public double getOffset() {
        return offset;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getUnit() {
        return unit;
    }


    @Override
    public String toString() {
        return "Signal{" +
                "signalString='" + signalString + '\'' +
                ", name='" + name + '\'' +
                ", startBit=" + startBit +
                ", length=" + length +
                ", byteOrder=" + byteOrder +
                ", valueType='" + valueType + '\'' +
                ", factor=" + factor +
                ", offset=" + offset +
                ", min=" + min +
                ", max=" + max +
                ", unit='" + unit + '\'' +
                ", receivers='" + receivers + '\'' +
                '}';
    }
}
