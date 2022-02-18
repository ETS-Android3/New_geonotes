package com.abhijith.nanodegree.geonotes.Model;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

public class NotesPrivate implements Serializable, ClusterItem {

    private String email;
    private String title;
    private String description;
    private String date;
    private String location;
    private String latitude;
    private String longitute;
    private String range;
    private String expiration;
    private String hour;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public LatLng getPosition() {
        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitute)) {
            return new LatLng(Double.valueOf(latitude), Double.valueOf(longitute));
        }
        return null;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return String.format("%s%n%n%s%s%n%n%s", description, "user: ", email, getLocation());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitute() {
        return longitute;
    }

    public void setLongitute(String longitute) {
        this.longitute = longitute;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration ) {
        this.expiration = expiration;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour ) {
        this.hour = hour;
    }

    public NotesPrivate(String title, String description, String range, String expiration, String hour, String email, String date, String location, String latitude, String longitute) {
        this.email = email;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitute = longitute;
        this.range = range;
        this.expiration = expiration;
        this.hour = hour;
    }
}
