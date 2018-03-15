package com.example.antu.antfinder;

import android.os.Debug;
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

    private Button bug_btn;
    private TextView bug_txt;
    private EditText bug_edit;
    private Button bug_submit;

    private  boolean indoors = false;

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
                    if( !(Collections.frequency(popList, hash.get("Room"))> 1))
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
        header_txt.setText("Is it busy around you");
        srch_btn.setVisibility(view.VISIBLE);
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
