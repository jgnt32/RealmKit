package com.timewastingguru.customannotations;

import com.timewastingguru.realmkit.annotation.RealmKitObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by artoymtkachenko on 25.05.15.
 */
@RealmKitObject
public class Address extends RealmObject {

    private String city;

    private String couuntry;

    private String street;

    @PrimaryKey
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCouuntry() {
        return couuntry;
    }

    public void setCouuntry(String couuntry) {
        this.couuntry = couuntry;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
