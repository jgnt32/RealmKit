package com.timewastingguru.customannotations;

import com.timewastingguru.realmkit.annotation.RealmKitObject;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by artoymtkachenko on 04.07.15.
 */
@RealmKitObject
public class Route extends RealmObject{

    private String title;

    private RealmList<Point> points;

    private int distance;

    private int duration;

    private String photo;

    private String description;
    private String descriptionShort;

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RealmList<Point> getPoints() {
        return points;
    }

    public void setPoints(RealmList<Point> points) {
        this.points = points;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDescriptionShort() {
        return descriptionShort;
    }

    public void setDescriptionShort(String descriptionShort) {
        this.descriptionShort = descriptionShort;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
