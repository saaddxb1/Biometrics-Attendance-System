package com.example.fas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends AppCompatActivity {

    private EditText emailLogin;
    private Button loginButton;
    private EditText loginPassword;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    private TextView forgotPassword;
    final String regexEmail = "^(.+)@(.+)$";
    final  String regexPassword =  "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}";

    private static final String TAG = "LoginPage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();
        forgotPassword = findViewById(R.id.forgotpassword);
        loginPassword = findViewById(R.id.passwordlogin);
        emailLogin = findViewById(R.id.resetEmail);
        loginButton = findViewById(R.id.resetButton);
        progressDialog = new ProgressDialog(LoginPage.this);
        progressDialog.setMessage("Loading...");

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPage.this, ForgotPassword.class);
                startActivity(intent);

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                login();
            }
        });





    }

    public void login(){

        String Email = emailLogin.getText().toString();
        String Password = loginPassword.getText().toString();


        if (Email.isEmpty()){
            emailLogin.setError("Email is required");
            emailLogin.requestFocus();
            return;
        }
        if (Password.isEmpty()){
            loginPassword.setError("Password is required");
            loginPassword.requestFocus();
            return;
        }

        if (!Password.matches(regexPassword)) {
            loginPassword.setError("Password must be at least 8 characters long, contain at least one number and have a mixture of uppercase and lowercase letters.");
            loginPassword.requestFocus();
            return;
        }

        if (!Email.matches(regexEmail)) {
            emailLogin.setError("invalid email format");
            emailLogin.requestFocus();
            return;
        }

        progressDialog.show();
        mAuth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.hide();
                    Intent intent = new Intent(LoginPage.this, SelectMode.class);
                    startActivity(intent);
                    finish();
                }else{
                    progressDialog.hide();
                    Toast.makeText(LoginPage.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }


    public void reset(){

        emailLogin.setText("");
        loginPassword.setText("");
    }
}
