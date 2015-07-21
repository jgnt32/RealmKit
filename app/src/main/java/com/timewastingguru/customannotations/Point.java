package com.timewastingguru.customannotations;

import com.timewastingguru.realmkit.annotation.RealmKitObject;

import io.realm.RealmObject;

/**
 * Created by artoymtkachenko on 04.07.15.
 */
@RealmKitObject
public class Point extends RealmObject {

    public static final int DEFAULT_RADIUS = 20;

    private String id;
    private String title;
    private int radius;
    private boolean visited;
    private String audio;
    private String image;
    private double lat;
    private double lon;
    private String description;
    private String descriptionShort;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionShort() {
        return descriptionShort;
    }

    public void setDescriptionShort(String descriptionShort) {
        this.descriptionShort = descriptionShort;
    }

}
