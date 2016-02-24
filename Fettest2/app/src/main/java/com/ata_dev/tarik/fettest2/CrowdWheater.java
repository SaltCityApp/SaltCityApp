package com.ata_dev.tarik.fettest2;

import com.firebase.client.Firebase;

/**
 * Created by Tarik on 22.2.2016.
 */
public class CrowdWheater extends android.app.Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
