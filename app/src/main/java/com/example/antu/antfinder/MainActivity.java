package com.example.antu.antfinder;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final FirebaseDatabase db = FirebaseDatabase.getInstance();
    final DatabaseReference scheduleRef = db.getReference("schedule"); //chsngr libsry nsme hrtr
    final DatabaseReference userRef = db.getReference("Userlocation");
    final DatabaseReference roomRef = db.getReference("Rooms");
    final String TAG = "YO ERROR:";
    ArrayList<HashMap<String,String>> todayList = new ArrayList<HashMap<String,String>>();
    HashSet<String> avaliableRooms = new HashSet<String>();
    Calendar calendar = Calendar.getInstance();

    public ArrayList<String> popList = new ArrayList<String>();
    private TextView classLst;
    public String classStr = "";

    private Button indoor_btn;
    private Button outdoor_btn;
    private TextView header_txt;

    private TextView building_txt;
    private EditText building_edit;
    private TextView room_txt;
    private EditText room_edit;

    private Button srch_btn;
    public static TextView place_lst;
    private Button bug_btn;
    private TextView bug_txt;
    private EditText bug_edit;
    private Button bug_submit;

    private  boolean indoors = false;

    public static double lon;
    public static double lat;
    private LocationManager locationManager;
    private LocationListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        classLst = (TextView) findViewById(R.id.class_list);
        indoor_btn = findViewById(R.id.indoor_btn);
        outdoor_btn = findViewById(R.id.outdoor_btn);
        header_txt = findViewById(R.id.header_txt);

        building_txt = findViewById(R.id.building_txt);
        building_edit = findViewById(R.id.building_edit);
        room_txt = findViewById(R.id.room_txt);
        room_edit = findViewById(R.id.room_edit);

        bug_btn = findViewById(R.id.bug_btn);
        bug_txt = findViewById(R.id.bug_txt);
        bug_edit = findViewById(R.id.bug_edit);
        bug_submit = findViewById(R.id.bug_submit);

        place_lst =findViewById(R.id.place_lst);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lon = location.getLongitude();
                lat = location.getLatitude();

                place_lst.setText("\n " + location.getLongitude() + " " + location.getLatitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        //configure_button();
        listener.onLocationChanged(location);
        new MyDownloadTask().execute();

        srch_btn = findViewById(R.id.search_btn);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists())
                        return;
                    HashMap<String, HashMap<String, String>> values = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                    Date cur = calendar.getTime();
                    for (Map.Entry<String, HashMap<String, String>> entry : values.entrySet()) {

                        Date t1 = new Date(entry.getKey());
                        if(cur.getTime() - t1.getTime() > 60000)
                            userRef.child(entry.getKey()).removeValue();
                        else
                            popList.add(entry.getValue().get("name"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });


    }
    public boolean isPopular(String s, int thresh){
        int count = 0;
        for(String sub :popList){
            if(sub.length() > 2 && s.toLowerCase().contains(sub.toLowerCase()))
                count++;
        }
        return count > thresh;
    }
    public void searchClasses(View view){
        header_txt.setText("Here are the aviable clases:");
        srch_btn.setVisibility(view.GONE);
        building_txt.setVisibility(view.GONE);
        building_edit.setVisibility(view.GONE);
        room_txt.setVisibility(view.GONE);
        room_edit.setVisibility(view.GONE);
        if(indoors){
            String building = building_edit.getText().toString();
            Building b = new Building(building,calendar.getTime().toString());
            userRef.child(calendar.getTime().toString()).setValue(new Building(building,calendar.getTime().toString()));

        }
        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ArrayList<HashMap<String,String>> values = (ArrayList<HashMap<String,String>>)dataSnapshot.getValue();
                String day = getDay();
                day = "W";
                for(HashMap<String,String> hash: values){
                    //if( !(Collections.frequency(popList, hash.get("Room"))> 1))
                    if(!isPopular(hash.get("Room"),1))
                        avaliableRooms.add(hash.get("Room"));
                }
                for(HashMap<String,String> hash2: values) {
                    if(hash2.get("Room").length() < 2){
                        avaliableRooms.remove(hash2.get("Room"));
                    }
                    if(hash2.get(day).equals("T")) {
                        todayList.add(hash2);
                        if (isTimeBetween(hash2.get("startTime"), hash2.get("endTime"))) {
                            avaliableRooms.remove(hash2.get("Room"));
                        }
                    }
                }
                avaliableRooms.remove("F 12:00-12:50p");
                for(String s : avaliableRooms){
                    classStr += new String(s) +"\n";
                }
                classLst.setText(classStr);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });
    }
    public String getDay(){
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String dayS = "None";
        switch(day){
            case(2):
                dayS = "M";
                break;
            case(3):
                dayS = "Tu";
                break;
            case(4):
                dayS = "W";
                break;
            case(5):
                dayS = "Th";
                break;
            case(6):
                dayS = "F";
                break;
        }
        return dayS;
    }
    public boolean isTimeBetween(String startTime, String endTime){
        try {
            Date t1 = new SimpleDateFormat("HH:mm:ss").parse(startTime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(t1);
            calendar1.add(Calendar.DATE, 1);

            Date t2 = new SimpleDateFormat("HH:mm:ss").parse(endTime);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(t2);
            calendar2.add(Calendar.DATE, 1);

            Date currentTime = calendar.getTime();
            currentTime = new Date(2018, 2, 21, 11, 45, 0);
            return currentTime.after(t1) && currentTime.before(t2);
        }
        catch(ParseException e){
            e.printStackTrace();
        }
        return false;
    }

    public void on_outdoors(View view){
        outdoor_btn.setVisibility(View.GONE);
        indoor_btn.setVisibility(view.GONE);
        header_txt.setText("Thanks, we will get your location");
        srch_btn.setVisibility(view.VISIBLE);
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    return;
                ArrayList<HashMap<String, String>> values = (ArrayList<HashMap<String, String>>) dataSnapshot.getValue();
                int count = 0;
                for(HashMap<String,String> h :values){
                    double arLat = Double.parseDouble(h.get("lat"));
                    double arlng = Double.parseDouble(h.get("lng"));
                    Log.w("auh",Double.toString(getDistanceFromLatLonInKm(lat,lon,arlng,arlng)));
                    if(getDistanceFromLatLonInKm(lat,lon,arlng,arlng) < 170000){
                        calendar.add(Calendar.MILLISECOND,count);
                        count ++ ;
                        Building b = new Building(h.get("name"), calendar.getTime().toString());
                        userRef.child(calendar.getTime().toString()).setValue(new Building(h.get("name"),calendar.getTime().toString()));
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }
    public void on_indoors(View view){
        outdoor_btn.setVisibility(view.GONE);
        indoor_btn.setVisibility(view.GONE);
        header_txt.setText("Tell us about where you are");
        building_txt.setVisibility(view.VISIBLE);
        building_edit.setVisibility(view.VISIBLE);
        room_txt.setVisibility(view.VISIBLE);
        room_edit.setVisibility(view.VISIBLE);
        indoors = true;
        srch_btn.setVisibility(view.VISIBLE);

    }
    public void bug_report(View view){
        bug_txt.setVisibility(view.VISIBLE);
        bug_edit.setVisibility(View.VISIBLE);
        bug_submit.setVisibility(View.VISIBLE);
        bug_btn.setVisibility(View.GONE);
    }
    public void bug_submit(View view){
        String s = bug_edit.getText().toString();
        Building b = new Building(s,calendar.getTime().toString());
        userRef.child(calendar.getTime().toString()).setValue(new Building(s,calendar.getTime().toString()));
        bug_edit.setVisibility(View.GONE);
        bug_submit.setVisibility(View.GONE);
        bug_txt.setText("Thank you for your report");
    }
    double getDistanceFromLatLonInKm(double lat1,double lon1, double lat2,double lon2) {
        long R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

}
class MyDownloadTask extends AsyncTask<Void,Void,Void>
{

    //public static String result;
    private String name;

    ArrayList<String> locationNames = new ArrayList<String>();
    ArrayList<String> address = new ArrayList<String>();
    protected void onPreExecute() {
        //display progress dialog.

    }
    protected Void doInBackground(Void... params) {
        try{
            //URL url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=33.6486988,-117.84201106&radius=500&opennow=true&types=cafe&key=AIzaSyBDVo38fszl9yWbDIWfsf-GGSY59gce4os");
            String API = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+Double.toString(MainActivity.lat)+","+Double.toString(MainActivity.lon)+"&radius=500&opennow=true&types=cafe&key=AIzaSyBDVo38fszl9yWbDIWfsf-GGSY59gce4os";
            URL url = new URL(API);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);

            //String responseMsg = con.getResponseMessage();
            //int response = con.getResponseCode();

            InputStream in = new BufferedInputStream(con.getInputStream());
            Reader reader = new InputStreamReader(in, "UTF-8");
            char[] buffer = new char[4096];

            StringBuilder builder = new StringBuilder();
            int len;
            while ((len = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, len);
            }

            JSONObject jsonObject = new JSONObject(builder.toString());
            JSONArray list = (JSONArray)jsonObject.get("results");

            //String haha = jsonObject.getString("status");



            Log.d("plz yes",Integer.toString(list.length()));
            int numOpens = list.length();


            // locationNames = new String[numOpens];
            for ( int i = 0 ; i < numOpens ; ++i)
            {   locationNames.add(list.getJSONObject(i).getString("name"));
                address.add(list.getJSONObject(i).getString("vicinity"));
                //locationNames[i] =list.getJSONObject(i).getString("name");
            }
            name = list.getJSONObject(1).getString("name");



            Log.d("plz yes",name);
            con.disconnect();

            API = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+Double.toString(MainActivity.lat)+","+Double.toString(MainActivity.lon)+"&radius=500&opennow=true&types=library&key=AIzaSyBDVo38fszl9yWbDIWfsf-GGSY59gce4os";
            url = new URL(API);
            //url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=33.6486988,-117.84201106&radius=500&opennow=true&types=library&key=AIzaSyBDVo38fszl9yWbDIWfsf-GGSY59gce4os");
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            //responseMsg = con.getResponseMessage();
            //response = con.getResponseCode();

            in = new BufferedInputStream(con.getInputStream());
            reader = new InputStreamReader(in, "UTF-8");
            buffer = new char[4096];

            builder = new StringBuilder();

            while ((len = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, len);
            }

            jsonObject = new JSONObject(builder.toString());
            list = (JSONArray)jsonObject.get("results");
            //haha = jsonObject.getString("status");



            Log.d("plz yes",Integer.toString(list.length()));

            numOpens = list.length();


            for ( int i = 0; i < numOpens ; ++i)
            {
                locationNames.add(list.getJSONObject(i).getString("name"));
                address.add(list.getJSONObject(i).getString("vicinity"));
            }
            name = list.getJSONObject(0).getString("name");



            Log.d("plz yes",name);
            con.disconnect();

            // so index to access which eleemnt, so cha or tea is 0, starbuks i 1... etc, then get wahtever is when u wanna get the info


            //JSONArray array= new JSONArray(builder.toString());
        }
        catch (Exception ex){
            //Log.d("plz no",":(");
            Log.e("plz no","ooo",ex);
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        //heare result is value you return from doInBackground() method
        //this is work on UI thread
        MainActivity.place_lst.append("\n");
        int len = locationNames.size();
        for(int i = 0; i<len; ++i) {
            MainActivity.place_lst.append(locationNames.get(i) + "\n");
            MainActivity.place_lst.append(address.get(i)+"\n");

        }
    }





}

