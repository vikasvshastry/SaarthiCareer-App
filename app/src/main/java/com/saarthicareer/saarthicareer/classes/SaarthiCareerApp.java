package com.saarthicareer.saarthicareer.classes;

import com.firebase.client.Firebase;

public class SaarthiCareerApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
