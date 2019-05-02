package com.bdroid.encryptonotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        WebView w = findViewById(R.id.wv1);
        this.setTitle("About");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        w.loadData(wvContent(), "text/html; charset=utf-8", "UTF-8");
    }

    private String wvContent() {
        String wvContent =
                "<html><head></head><body><br/><h1><br/>EncryptoNotes v1.0</h1><br/>" +
                        "This is the initial release of EncryptoNotes by BrightDroid Apps<br/>" +
                        "This is an open source project.<br/><br/>";

        try {
            PackageInfo pInfo = _Enotes.getAppContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            wvContent += version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        wvContent += "</body></html>";

        return wvContent;
    }
}
