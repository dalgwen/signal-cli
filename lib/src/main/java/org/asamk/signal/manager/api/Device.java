package org.asamk.signal.manager.api;

public class Device {
    int id;
    String name;
    long created;
    long lastSeen;
    boolean isThisDevice;

    public Device(int id, String name, long created, long lastSeen, boolean isThisDevice) {
        super();
        this.id = id;
        this.name = name;
        this.created = created;
        this.lastSeen = lastSeen;
        this.isThisDevice = isThisDevice;
    }

}
