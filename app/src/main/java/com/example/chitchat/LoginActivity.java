package com.example.chitchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    TextInputLayout emailTextInputLayout, passTextInputLayout;
    DatabaseReference mDatabaseReference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailTextInputLayout = (TextInputLayout)findViewById(R.id.editText1);
        passTextInputLayout = (TextInputLayout)findViewById(R.id.editText2);
        progressDialog = new ProgressDialog(LoginActivity.this);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage("Really Exit ??");
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new MyListener());
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }
    public class MyListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    public void buttonIsClicked(View view) {

        switch(view.getId()) {

            case R.id.buttonSign:

                String email = emailTextInputLayout.getEditText().getText().toString().trim();
                String password = passTextInputLayout.getEditText().getText().toString().trim();

                if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please Fill all blocks", Toast.LENGTH_SHORT).show();
                    return ;
                }
                progressDialog.setTitle("Logging in");
                progressDialog.setMessage("Please wait while we are checking the credentials..");
                progressDialog.setCancelable(false);
                progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                login_user(email, password);
                break;

            case R.id.buttonRegister:

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    private void login_user(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            progressDialog.dismiss();

            if(task.isSuccessful()) {

                String user_id = mAuth.getCurrentUser().getUid();
                String token_id = FirebaseMessaging.getInstance().getToken().toString();
                Map addValue = new HashMap();
                addValue.put("device_token", token_id);
                addValue.put("online", "true");

                mDatabaseReference.child(user_id).updateChildren(addValue, (databaseError, databaseReference) -> {

                    if(databaseError == null) {

                        Log.e("Login : ", "Logged in Successfully" );
                        Toast.makeText(getApplicationContext(), "Logged in Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(LoginActivity.this, databaseError.toString()  , Toast.LENGTH_SHORT).show();
                        Log.e("Error is : ", databaseError.toString());

                    }
                });



            }
            else {
                Toast.makeText(LoginActivity.this, "Wrong Credentials" + "", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
