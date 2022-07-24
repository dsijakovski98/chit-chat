package com.example.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextInputLayout etDisplayName, etEmail, etPassword;
    Button buttonSubmit;
    ProgressDialog progressDialog;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etDisplayName = findViewById(R.id.editText3);
        etEmail = findViewById(R.id.editText4);
        etPassword = findViewById(R.id.editText5);
        buttonSubmit = findViewById(R.id.button3);
        progressDialog = new ProgressDialog(RegisterActivity.this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    }

    public void buttonIsClicked(View view) {

        if(view.getId() == R.id.button3) {

            String displayName = etDisplayName.getEditText().getText().toString().trim();
            String email = etEmail.getEditText().getText().toString().trim();
            String password = etPassword.getEditText().getText().toString().trim();

            if(displayName.equals("")) {
                Toast.makeText(RegisterActivity.this, "Please Fill the name", Toast.LENGTH_SHORT).show();
                return;
            }

            if(email.equals("")) {
                Toast.makeText(RegisterActivity.this, "Please Fill the email", Toast.LENGTH_SHORT).show();
                return ;
            }

            if(password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password is too short", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setTitle("Registering User");
            progressDialog.setMessage("Please wait while we are creating your account... ");
            progressDialog.setCancelable(false);
            progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            register_user(displayName, email, password);
        }
    }


    private void register_user(final String displayName, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {

            if(task.isSuccessful()) {
                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                final String uid = current_user.getUid();
                String token_id = FirebaseMessaging.getInstance().getToken().toString();
                Map userMap = new HashMap();
                userMap.put("device_token", token_id);
                userMap.put("name", displayName);
                userMap.put("status", "Hello User");
                userMap.put("image", "default");
                userMap.put("thumb_image", "default");
                userMap.put("online", "true");

                mDatabase.child(uid).setValue(userMap).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){

                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "New User is created", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "YOUR NAME IS NOT REGISTERED... MAKE NEW ACCOUNT-- ", Toast.LENGTH_SHORT).show();
                    }

                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "ERROR REGISTERING USER....", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
