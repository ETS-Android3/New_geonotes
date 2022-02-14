package com.abhijith.nanodegree.geonotes.Model;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

public class Friends {

    private String email;
    private String addressee;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public Friends(String addressee,String email) {
        this.email = email;
        this.addressee = addressee;
    }

    public Friends() {
    }
}
