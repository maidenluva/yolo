package com.example.antu.antfinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    final FirebaseDatabase db = FirebaseDatabase.getInstance();
    final DatabaseReference scheduleRef = db.getReference("schedule");
    final String TAG = "YO ERROR:";
    ArrayList<HashMap<String,String>> todayList = new ArrayList<HashMap<String,String>>();
    HashSet<String> avaliableRooms = new HashSet<String>();
    Calendar calendar = Calendar.getInstance();

    private TextView classLst;
    public String classStr = "";

    private Button indoor_btn;
    private Button outdoor_btn;
    private TextView header_txt;

    private TextView building_txt;
    private EditText building_edit;
    private TextView room_txt;
    private EditText room_edit;



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



    }
    public void searchClasses(View view){
        scheduleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ArrayList<HashMap<String,String>> values = (ArrayList<HashMap<String,String>>)dataSnapshot.getValue();
                String day = getDay();
                for(HashMap<String,String> hash: values)
                    avaliableRooms.add(hash.get("Room"));
                for(HashMap<String,String> hash2: values) {
                    if(hash2.get(day).equals("T")){
                        todayList.add(hash2);
                        if(!isTimeBetween(hash2.get("startTime"),hash2.get("endTime"))) {
                            avaliableRooms.remove(hash2.get("Room"));
                        }
                    }
                }
                for(String s : avaliableRooms){
                    Log.w(TAG, "Failed to read value."+ s);
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
    }
    public void on_indoors(View view){
        outdoor_btn.setVisibility(view.GONE);
        indoor_btn.setVisibility(view.GONE);
        header_txt.setText("Tell us about where you are");
        building_txt.setVisibility(view.VISIBLE);
        building_edit.setVisibility(view.VISIBLE);
        room_txt.setVisibility(view.VISIBLE);
        room_edit.setVisibility(view.VISIBLE);
    }
}
