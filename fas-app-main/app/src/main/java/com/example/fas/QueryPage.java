package com.example.fas;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class QueryPage extends AppCompatActivity {
    private EditText dateFrom, dateTo;
    private Button findButton;
    private String pattern = "([0-9]{2})-([0-9]{2})-([0-9]{4})";
    String[] attendance = {"Absent", "Present", "Both"};
    String [] year = {"1", "2", "3"};
    String[] OneModules = {"PDE1110", "PDE1120", "PDE1130", "PDE1140"};
    String[] TwoModules = {"PDE2100", "PDE2101", "PDE2102", "PDE2103"};
    String[] ThreeModules = {"PDE3100", "PDE3111", "PDE3112"};
    DatePickerDialog.OnDateSetListener onDateSetListener, onDateSetListener1;
    AutoCompleteTextView AP, Modules, Year;
    ArrayAdapter<String> adapter, adapter2, yearAdpt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_page);
        final Calendar calendar = Calendar.getInstance();
        int yearqq = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dateFrom = findViewById(R.id.StartDate);

        dateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        QueryPage.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, onDateSetListener, yearqq, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = dayOfMonth+"-"+month+"-"+year;
                dateFrom.setText(convertDate(date));
            }
        };
        dateTo = findViewById(R.id.EndDate);
        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        QueryPage.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, onDateSetListener1, yearqq, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        onDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String date = dayOfMonth+"-"+month+"-"+year;
                dateTo.setText(convertDate(date));
            }
        };
        findButton = findViewById(R.id.findButton);
        Year = findViewById(R.id.year);
        Modules = findViewById(R.id.module);
        AP = findViewById(R.id.AP);
        yearAdpt = new ArrayAdapter<String>(this, R.layout.list_item, year);
        Year.setAdapter(yearAdpt);
        Year.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mYears = Year.getText().toString().trim();
                Modules.setText("");
                if (mYears.equals("1")){
                    adapter2 = new ArrayAdapter<String>(QueryPage.this, R.layout.list_item, OneModules);
                    Modules.setAdapter(adapter2);
                }else if (mYears.equals("2")){
                    adapter2 = new ArrayAdapter<String>(QueryPage.this, R.layout.list_item, TwoModules);
                    Modules.setAdapter(adapter2);
                } else if (mYears.equals("3")){
                    adapter2 = new ArrayAdapter<String>(QueryPage.this, R.layout.list_item, ThreeModules);
                    Modules.setAdapter(adapter2);
                }
            }
        });

        adapter = new ArrayAdapter<String>(this,R.layout.list_item, attendance);
        AP.setAdapter(adapter);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Attendance");
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });
    }
    public void send(){
        String nAP = AP.getText().toString().trim();
        String nModule = Modules.getText().toString().trim();
        String DateFrom = dateFrom.getText().toString().trim();
        String DateTo = dateTo.getText().toString().trim();
        if(DateFrom.isEmpty()){
            dateFrom.setError("Start Date Required");
            dateFrom.requestFocus();
            return;
        }
        if (DateTo.isEmpty()){
            dateTo.setError("End Date Required");
            dateTo.requestFocus();
            return;
        }

        if (!DateFrom.matches(pattern)){
            dateFrom.setError("Invalid Format. Input date as DD-MM-YY");
            dateFrom.requestFocus();
            return;
        }
        if (!DateTo.matches(pattern)){
            dateTo.setError("Invalid Format. Input date as DD-MM-YY");
            dateTo.requestFocus();
            return;
        }
        if (!validDate(DateFrom)){
            dateFrom.setError("Date not in record");
            dateFrom.requestFocus();
            return;
        }
        if (!validDate(DateTo)){
            dateTo.setError("Date not in record");
            dateTo.requestFocus();
            return;
        }
        if (!afterStart(DateFrom ,DateTo)){
            dateTo.setError("End date should be after start");
            dateTo.requestFocus();
            return;
        }
        Intent intent = new Intent(QueryPage.this, TableRecord.class);
        intent.putExtra("AP", nAP);
        intent.putExtra("DateFrom", DateFrom);
        intent.putExtra("DateTo", DateTo);
        intent.putExtra("Module", nModule);
//        Toast.makeText(QueryPage.this, "M: "+auto, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    public boolean validDate(String d) {
        ArrayList<ArrayList<String>> allweeks = AcademicYear.getAllWeeks();
        for (int i = 0; i < allweeks.size(); i++) {
            if (allweeks.get(i).contains(d)) {
                return true;
            }
        }
    return false;
    }
    public boolean afterStart(String s, String e){
        boolean endFound = false;
        if (s.equals(e)){
            return true;
        }
        ArrayList<ArrayList<String>> allweeks = AcademicYear.getAllWeeks();
        for (int i = 0; i <allweeks.size(); i++){
            for (int x = 0; x<allweeks.get(i).size(); x++){
                if (allweeks.get(i).get(x).equals(e)){
                    endFound = true;
                }
                if (allweeks.get(i).get(x).equals(s) && !endFound){
                    return true;
                }
                if (allweeks.get(i).get(x).equals(s) && endFound){
                    return false;
                }
            }
        }
        return false;
    }

    public String convertDate(String date){
        int size = date.length();
        switch (size){
            case 9:
                if(Character.toString(date.charAt(1)).equals("-")){
                    String aa = "0"+Character.toString(date.charAt(0));
                    String newDate = aa + date.substring(1,9);
                    return newDate;
                }else if(Character.toString(date.charAt(2)).equals("-")){
                    String aa = "0"+Character.toString(date.charAt(3));
                    String newDate =date.substring(0,3) + aa + date.substring(4,9);
                    return newDate;
                }
            case 8:
                String aa = "0"+Character.toString(date.charAt(0));
                String ab = "0"+Character.toString(date.charAt(2));
                String newDate = aa+"-"+ab+date.substring(3,8);
                return newDate;
        }
        return date;
    }


}