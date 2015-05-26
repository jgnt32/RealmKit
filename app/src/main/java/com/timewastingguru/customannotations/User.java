package com.timewastingguru.customannotations;


import com.timewastingguru.realmkit.annotation.RealmKitObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by artoymtkachenko on 13.05.15.
 */
@RealmKitObject
public class User extends RealmObject{

    @PrimaryKey
    private String id;

    private String name;

    private String email;

    private Address address;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
