package com.daniil.canmonitor.parser;
import java.util.ArrayList;
import java.util.List;

public class Message {

    private String messageString = "";
    private String name;
    private String sender;
    private String id;
    private int length;
    private ArrayList<Signal> signals;
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Signal> getSignals() {
        return signals;
    }


    public Message(String messageString,String name, String id, int length, String sender) {
        this.messageString = messageString;
        this.name = name;
        this.id = id;
        this.length = length;
        this.sender = sender;
        signals = new ArrayList<>();
    }

    public void addSignal(Signal signal) {
        signals.add(signal);
    }


    @Override
    public String toString() {
        return "Message[" + "\n" + "\t" +
                "name='" + name + '\'' + "\n"+ "\t" +
                "sender='" + sender + '\'' + "\n"+ "\t" +
                "id=" + id + "\n"+ "\t" +
                "length=" + length + "\n"+ "\t";

    }
}