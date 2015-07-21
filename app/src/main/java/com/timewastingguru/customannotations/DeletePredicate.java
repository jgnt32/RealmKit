package com.timewastingguru.customannotations;

import io.realm.RealmObject;

/**
 * Created by artoymtkachenko on 26.05.15.
 */
public interface DeletePredicate {

    boolean shouldToDelete(RealmObject container, RealmObject targetOfDelete);
    RealmKi


}
