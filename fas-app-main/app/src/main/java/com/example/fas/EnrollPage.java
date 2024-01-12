package com.example.fas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EnrollPage extends AppCompatActivity {
    private EditText firstname, lastname, misis;
    private Button button;
    private String miRegex = "M([0-9]{8})";
    String[] Nyear = {"1", "2", "3"};
    String[] OneModules = {"PDE1110", "PDE1120", "PDE1130", "PDE1140"};
    String[] TwoModules = {"PDE2100", "PDE2101", "PDE2102", "PDE2103"};
    String[] ThreeModules = {"PDE3100", "PDE3111", "PDE3112"};
    AutoCompleteTextView Modules, nYear;
    ArrayAdapter<String>  moAdpt, yearAdpt;
    ProgressDialog progressDialog;
    ArrayList<String> years = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroll_page);
        years.add("1");
        years.add("2");
        years.add("3");
        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        nYear = findViewById(R.id.year);
        Modules = findViewById(R.id.module);
        misis = findViewById(R.id.misisnumner);
        button = findViewById(R.id.submit);
        progressDialog = new ProgressDialog(EnrollPage.this);
        progressDialog.setMessage("Loading...");
        yearAdpt = new ArrayAdapter<String>(this, R.layout.list_item, Nyear);
        nYear.setAdapter(yearAdpt);
        nYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mYears = nYear.getText().toString().trim();
                Modules.setText("");
                if (mYears.equals("1")){
                    moAdpt = new ArrayAdapter<String>(EnrollPage.this, R.layout.list_item, OneModules);
                    Modules.setAdapter(moAdpt);
                }else if (mYears.equals("2")){
                    moAdpt = new ArrayAdapter<String>(EnrollPage.this, R.layout.list_item, TwoModules);
                    Modules.setAdapter(moAdpt);
                } else if (mYears.equals("3")){
                    moAdpt = new ArrayAdapter<String>(EnrollPage.this, R.layout.list_item, ThreeModules);
                    Modules.setAdapter(moAdpt);
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatefirebase();
            }
        });
    }
    public void updatefirebase(){
        String FirstName = firstname.getText().toString().trim();
        String LastName = lastname.getText().toString().trim();
        String Year = nYear.getText().toString().trim();
        String Module = Modules.getText().toString().trim();
        String Misis = misis.getText().toString().trim();
        if (FirstName.isEmpty()){
            firstname.setError("First Name Required");
            firstname.requestFocus();
            return;
        }if (LastName.isEmpty()){
            lastname.setError("Last Name Required");
            lastname.requestFocus();
            return;
        }
        if (Misis.isEmpty()){
            misis.setError("Misis Number Required");
            misis.requestFocus();
            return;
        }if (!Misis.matches(miRegex)){
            misis.setError("Invalid Format");
            misis.requestFocus();
            return;
        }
        if (!isString(FirstName)){
            firstname.setError("Invalid Entry");
            firstname.requestFocus();
            return;
        }if (!isString(LastName)){
            lastname.setError("Invalid Entry");
            lastname.requestFocus();
            return;
        }
        ArrayList<String> previous = new ArrayList<>();
        final boolean[] found = {false};
        final boolean[] once = {false};
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Records");
       db.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(!snapshot.hasChildren()){
                   addData(1, FirstName, LastName, Misis, Module, Year);
               }else {
                   db.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                           for(DataSnapshot snapshot1: snapshot.getChildren()){
                               for(DataSnapshot snapshot2: snapshot1.getChildren()){
                                   previous.add(snapshot2.getValue().toString());
                               }
                           }
                           if(!once[0]){
                               once[0] = true;
                               for(int i=2; i<previous.size();i = i +5){
                                   String a = previous.get(i) + previous.get(i+1);
                                   String b = Misis + Module;
                                   if (b.equals(a)){
                                       found[0] = true;
                                       misis.setError("MISIS already enrolled for this module");
                                       misis.requestFocus();
                                       return;
                                   }
                               }
                           }
                           if(!found[0]){
                               found[0] = true;
                               int num = (previous.size()/5) + 1;
                               for(int i=2; i<previous.size();i = i +5){
                                   if (previous.get(i).equals(Misis)){
                                       if(previous.get(i-2).equals(FirstName) && previous.get(i-1).equals(LastName)) {
                                           addData(num, FirstName, LastName, Misis, Module, Year);
                                       }else{
                                           misis.setError("MISIS already enrolled");
                                           misis.requestFocus();
                                           return;
                                       }
                                   }else{
                                       addData(num, FirstName, LastName, Misis, Module, Year);
                                   }
                               }
                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {

                       }
                   });
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    public void clear(){
        firstname.setText("");
        lastname.setText("");
        nYear.setText("");
        Modules.setText("");
        misis.setText("");
        progressDialog.hide();
    }

    public boolean isString(String str)
    {
        str = str.replaceAll("[^\\d]", " ");
        str = str.trim();
        str = str.replaceAll(" +", " ");
        if (str.equals("")){
            return true;
        }
        else{
            return false;
        }
    }
    public void addData(int i, String f, String l, String mi, String mo, String y){
        String id = Integer.toString(i);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference root = db.getReference().child("Records").child(id);
        root.child("First Name").setValue(f);
        root.child("Last Name").setValue(l);
        root.child("Misis Number").setValue(mi);
        root.child("Module Number").setValue(mo);
        root.child("Year").setValue(y).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(EnrollPage.this, "Enrolled Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EnrollPage.this, "Enrollment Failed! Try Again", Toast.LENGTH_SHORT).show();
            }
        });
        clear();
    }
}