package com.daniil.canmonitor.parser;

public class MessageCAN {
    private String id;
    private String data;

    private String name;

    private String idParametr;

    public MessageCAN(String id, String data, String name, String idParametr) {
        this.id = id;
        this.data = data;
        this.name = name;
        this.idParametr = idParametr;
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public String getIdParametr() {
        return idParametr;
    }
}
