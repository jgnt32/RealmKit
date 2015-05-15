package com.timewastingguru.customannotations;


import com.timewastingguru.realmkit.annotation.RealmKitObject;

/**
 * Created by artoymtkachenko on 13.05.15.
 */

@RealmKitObject
public class User {

    String name;

    String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
