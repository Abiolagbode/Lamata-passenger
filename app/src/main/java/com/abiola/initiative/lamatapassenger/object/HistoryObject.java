package com.abiola.initiative.lamatapassenger.object;

import com.google.android.gms.maps.model.LatLng;

public class HistoryObject {

    private String history_id;
    private String driver_id;
    private String dropoffName;
    private String pickupName;
    private LatLng pickupLoc;
    private LatLng dropoffLoc;
    private String date;
    private String time;
    private int driver_rating;

    public HistoryObject(String history_id, String driver_id, String dropoffName, String pickupName, LatLng pickupLoc, LatLng dropoffLoc, String date, String time, int driver_rating) {
        this.history_id = history_id;
        this.driver_id = driver_id;
        this.dropoffName = dropoffName;
        this.pickupName = pickupName;
        this.pickupLoc = pickupLoc;
        this.dropoffLoc = dropoffLoc;
        this.date = date;
        this.time = time;
        this.driver_rating = driver_rating;
    }

    public String getHistory_id() {
        return history_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public String getDropoffName() {
        return dropoffName;
    }

    public String getPickupName() {
        return pickupName;
    }

    public LatLng getPickupLoc() {
        return pickupLoc;
    }

    public LatLng getDropoffLoc() {
        return dropoffLoc;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getDriver_rating() {
        return driver_rating;
    }
}
