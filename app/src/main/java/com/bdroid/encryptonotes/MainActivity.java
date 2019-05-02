package com.bdroid.encryptonotes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    static dbHelper db;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<HashMap<String, String>>();
    public SimpleAdapter InboxAdapter;
    Context ctx;



    @Override
    public void onResume() {
        super.onResume();
        readTheFile2();
        InboxAdapter.notifyDataSetChanged();
        final ListView lv = findViewById(R.id.reCycView);
        lv.smoothScrollByOffset(0);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("EncryptoNotes");

        db = Utils.dbH;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchView inputSearch = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();

        inputSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                    MainActivity.this.InboxAdapter.getFilter().filter(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if((newText.length()>1) || (newText.length()==0)) {
                    MainActivity.this.InboxAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_add){
            Intent i = new Intent(ctx, EditorActivity.class);
            startActivity(i);

        }
        if (id == R.id.app_bar_search) {
            readTheFile2();
        }
        if (id == R.id.app_bar_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public void readTheFile2() {
        feedList.clear();
        Cursor theThread = db.getNOTES();

        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();

        if (theThread.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < theThread.getColumnCount(); i++) {
                    map.put(theThread.getColumnName(i), theThread.getString(i));

                }
                maplist.add(map);
            } while (theThread.moveToNext());
        }
        String dtemp;
        for (int j = 0; j < maplist.size(); j++) {
            HashMap<String, String> poo = maplist.get(j);
            dtemp = getDate(Long.parseLong(poo.get("ts")));
            Log.d("PooPoos", poo.toString());
            poo.remove("ts");
            poo.put("date", dtemp);
            poo.put("cnam", db.getContactName(this, poo.get("num")));



            feedList.add(poo);
        }

//        Collections.reverse(feedList);
        InboxAdapter = fillListView(feedList);
    }

    public String getDate(Long ts) {
        //   Log.d("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy HH:mm").format(df);
        return (rc);
    }


    public SimpleAdapter fillListView(final ArrayList lines) {
        final SimpleAdapter simpleAdapter =
                new SimpleAdapter(this, lines, R.layout.noteslistitem, new String[]{"num", "body", "type", "date"}, new int[]{R.id.liUser, R.id.liBody, R.id.liType, R.id.liDate}){
                    @Override
                    public View getView (int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView cs = view.findViewById(R.id.liType);
                        String type = cs.getText().toString();
                        view.invalidate();
                        return view;
                    }
                };

        final ListView lv = findViewById(R.id.reCycView);
        lv.setAdapter(simpleAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(_Enotes.getAppContext(), EditorActivity.class);
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);
                String pooo = theItem.get("body");
                     Log.d("zzzz", "body:" + pooo);
                     String _id = theItem.get("_id");
                //dumper(theItem);
                i.putExtra("body", pooo);
                i.putExtra("id", _id);
                Log.d("pppp", i.toString());
                startActivity(i);

            }
        });

        return simpleAdapter;
    }

}
