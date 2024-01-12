package com.example.fas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SelectMode extends AppCompatActivity {
    private Button enrollButton, checkButton, dailybutton;
    private String pattern = "dd-MM-yyyy";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_mode);
        enrollButton = findViewById(R.id.EnrollButton);
        checkButton = findViewById(R.id.CheckButton);
        dailybutton = findViewById(R.id.DailyRecord);
        String dateInString =new SimpleDateFormat(pattern).format(new Date());

        enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectMode.this, EnrollPage.class);
                startActivity(intent);
            }
        });
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectMode.this, QueryPage.class);
                startActivity(intent);
            }
        });

        dailybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Records");
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()){
                            checkAlready(dateInString);

                        }else {
                            Toast.makeText(SelectMode.this, "No Users Enrolled", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    public void checkAlready(String date){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Attendance")
                .child(getWeek(date));
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(date)){
                    firebase(date, getWeek(date));
                    Toast.makeText(SelectMode.this, "Daily Record Made", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(SelectMode.this, "Record already made for today", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String getWeek(String date){
        ArrayList<ArrayList<String>> weeks = AcademicYear.getAllWeeks();
        for(int i= 0; i<weeks.size();i++){
            if(weeks.get(i).contains(date))
                if(i <25){
                    return ("Week " + i);
                }else{
                    return ("Summer Week " + (i-24));
                }

        }
        return "";
    }


    public void firebase(String date, String week){
        ArrayList<String> previous = new ArrayList<>();
        final int[] num = {0};
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Records");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    num[0] +=1;
                    for(DataSnapshot snapshot2: snapshot1.getChildren()){
                        previous.add(snapshot2.getValue().toString());

                    }
                    addData(num[0], previous.get(0).toString(),previous.get(2).toString(), previous.get(3), date, week);
                    previous.clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void addData(int i, String f, String mi, String mo, String date, String week){
        String id = Integer.toString(i);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference root = db.getReference().child("Attendance").child(week).child(date).child(mo).child(id);
        root.child("Attendance").setValue("Absent");
        root.child("First Name").setValue(f);
        root.child("Misis Number").setValue(mi).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SelectMode.this, "Failed! Try Again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}