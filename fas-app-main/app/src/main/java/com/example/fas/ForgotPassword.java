package com.example.fas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText resetEmail;
    private Button resetButton;
    private ProgressDialog progressDialog;

    FirebaseAuth auth;
    final String regexEmail = "^(.+)@(.+)$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        auth = FirebaseAuth.getInstance();
        resetButton = findViewById(R.id.resetButton);
        resetEmail = findViewById(R.id.resetEmail);
        progressDialog = new ProgressDialog(ForgotPassword.this);
        progressDialog.setMessage("Loading...");
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();

            }
        });

    }

    public void resetPassword(){
        progressDialog.show();

        String email = resetEmail.getText().toString().trim();

        if(email.isEmpty()){
            resetEmail.setError("Email is required");
            resetEmail.requestFocus();
            return;
        }

        if(!email.matches(regexEmail)){
            resetEmail.setText("Invalid email format");
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.hide();
                    Toast.makeText(ForgotPassword.this, "Check you email to reset your password", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPassword.this, ForgotPassword.class);
                    startActivity(intent);
                    finish();
                }else{
                    progressDialog.hide();
                    Toast.makeText(ForgotPassword.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}