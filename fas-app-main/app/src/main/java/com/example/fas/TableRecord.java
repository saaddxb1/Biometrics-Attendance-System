package com.example.fas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TableRecord extends AppCompatActivity {
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_records);
        progressDialog = new ProgressDialog(TableRecord.this);
        progressDialog.setMessage("Fetching Data...");
        addheaders();
        everyDate();
    }

    public void everyDate() {
        String Start = getIntent().getStringExtra("DateFrom");
        String End = getIntent().getStringExtra("DateTo");
        boolean send = false;
        ArrayList<ArrayList<String>> allweeks = AcademicYear.getAllWeeks();
        for (int i = 0; i < allweeks.size(); i++) {
            for (int x = 0; x < allweeks.get(i).size(); x++) {
                if (allweeks.get(i).get(x).equals(Start)) {
                    send = true;
                }
                if (send) {
                    String date = allweeks.get(i).get(x);
                    if (i < 25){
                        String week = "Week " + i;
                        firebasePullAdd(date, week);
                    }else{
                        String week = "Summer Week " + (i-24);
                        firebasePullAdd(date, week);
                    }
                }
                if (allweeks.get(i).get(x).equals(End)) {
                    send = false;
                }
            }
        }
        progressDialog.hide();
    }

    public void firebasePullAdd(String date, String week){
        ArrayList<String> data = new ArrayList<>();
        String AP = getIntent().getStringExtra("AP");
        String module = getIntent().getStringExtra("Module");
        DatabaseReference fu = FirebaseDatabase.getInstance().getReference().child("Attendance").child(week).child(date).child(module);
        fu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        data.add(dataSnapshot1.getValue().toString());
                    }
                    if (AP.toLowerCase().equals("both")){
                        addRows(date, data.get(2), data.get(1),data.get(0) );
                        data.clear();
                    }else if(AP.toLowerCase().equals("absent")){
                        if(data.get(0).toLowerCase().equals("absent")){
                            addRows(date, data.get(2), data.get(1),data.get(0) );
                            data.clear();
                        }
                    }else if(AP.toLowerCase().equals("present")){
                        if(data.get(0).toLowerCase().equals("present")){
                            addRows(date, data.get(2), data.get(1),data.get(0) );
                            data.clear();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TableRecord.this, "Could not fetch the data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addRows(String d, String m, String n, String a){
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table_data_view);
        TableRow tableRow = new TableRow(TableRecord.this);
        tableRow.setBackgroundColor(Color.WHITE);
        TextView textView11 = new TextView(TableRecord.this);
        textView11.setText(d);
        textView11.setTextColor(Color.BLACK);
        textView11.setTextSize(10);
        textView11.setPadding(45, 14, 0, 14);
        textView11.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView11);
        TextView textView1 = new TextView(TableRecord.this);
        textView1.setText(m);
        textView1.setTextColor(Color.BLACK);
        textView1.setTextSize(10);
        textView1.setPadding(45, 14, 0, 14);
        textView1.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView1);
        TextView textView2 = new TextView(TableRecord.this);
        textView2.setText(n);
        textView2.setTextColor(Color.BLACK);
        textView2.setTextSize(10);
        textView2.setPadding(65, 14, 0, 14);
        textView2.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView2);
        TextView textView3 = new TextView(TableRecord.this);
        textView3.setText(a);
        textView3.setTextColor(Color.BLACK);
        textView3.setTextSize(10);
        textView3.setPadding(100, 14, 0, 14);
        textView3.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView3);
        tableLayout.addView(tableRow);
    }

    public void addheaders(){
        progressDialog.show();
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table_data_view);
        TableRow tableRow = new TableRow(TableRecord.this);
        tableRow.setBackgroundColor(Color.rgb(33, 81, 166));
        TextView textView11 = new TextView(TableRecord.this);
        textView11.setText("Date");
        textView11.setTextColor(Color.WHITE);
        textView11.setTextSize(14);
        textView11.setPadding(45, 25, 0, 25);
        textView11.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView11);
        TextView textView1 = new TextView(TableRecord.this);
        textView1.setText("MISIS No.");
        textView1.setTextColor(Color.WHITE);
        textView1.setTextSize(14);
        textView1.setPadding(45, 25, 0, 25);
        textView1.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView1);
        TextView textView2 = new TextView(TableRecord.this);
        textView2.setText("Name");
        textView2.setTextColor(Color.WHITE);
        textView2.setTextSize(14);
        textView2.setPadding(65, 25, 0, 25);
        textView2.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView2);
        TextView textView3 = new TextView(TableRecord.this);
        textView3.setText("Attendance");
        textView3.setTextColor(Color.WHITE);
        textView3.setTextSize(14);
        textView3.setPadding(100, 25, 0, 25);
        textView3.setGravity(Gravity.CENTER_HORIZONTAL);
        tableRow.addView(textView3);
        tableLayout.addView(tableRow);
    }
}