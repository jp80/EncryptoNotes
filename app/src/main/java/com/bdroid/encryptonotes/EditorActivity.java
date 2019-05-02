package com.bdroid.encryptonotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class EditorActivity extends AppCompatActivity {

    static dbHelper db;
    //final EditText theNote;
    public EditText theNote;
    String replaceId = "-1";
    Context ctx = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_editor);

        theNote = findViewById(R.id.editText);

        super.onCreate(savedInstanceState);
        this.setTitle("Edit Note:");

        Intent intent = getIntent();
        if(intent.hasExtra("body")) {

            Bundle bob = intent.getExtras();
            Log.d("Enotes:", bob.toString());
           theNote.setText(bob.getString("body"));
 //           theNote.setText("fuck right off");
            Log.d("Enotes:", "recieved body from main: " + bob.getString("body"));
            replaceId = bob.getString("id");
        }

            db = Utils.dbH;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        int ts = (int) (System.currentTimeMillis() / 1000L);

        if (id == R.id.edit_save) {
            Intent i = new Intent(ctx, MainActivity.class);

            if(replaceId.equals("-1")) {
                db.insertNOTE("1", theNote.getText().toString(), "text/plain", ts);
            } else {
                db.alterNOTE(replaceId, theNote.getText().toString(), "text/plain", ts);
            }
            startActivity(i);
            return true;
        }
        if (id == R.id.edit_delete) {
            Intent i = new Intent(ctx, MainActivity.class);
            db.alterNOTE(replaceId, "", "delete", 0);
            startActivity(i);
            return true;
        }
        if (id == R.id.edit_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, theNote.getText().toString());

            startActivity(Intent.createChooser(i, "Share via.."));
        }

        return super.onOptionsItemSelected(item);
    }



}
