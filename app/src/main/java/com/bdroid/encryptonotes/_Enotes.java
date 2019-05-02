package com.bdroid.encryptonotes;

import android.app.Application;
import android.content.Context;

public class _Enotes extends  Application{

        private static Context context;

        public void onCreate() {
            super.onCreate();
            _Enotes.context = getApplicationContext();
        }

        public static Context getAppContext() {
            return _Enotes.context;
        }


        public static dbHelper db = new dbHelper(_Enotes.getAppContext());
}
