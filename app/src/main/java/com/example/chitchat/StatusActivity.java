package com.example.chitchat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    TextInputLayout mTextInputLayout;
    Button mButtonSubmit;
    DatabaseReference mDatabaseReference;
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        this.setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();

        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mTextInputLayout = findViewById(R.id.textInputStatus);
        mButtonSubmit = findViewById(R.id.buttonChangeStatus);

        String currentStatus = getIntent().getStringExtra("current_status");
        mTextInputLayout.getEditText().setText(currentStatus);
    }
    public void buttonIsClicked(View view) {

        String status = mTextInputLayout.getEditText().getText().toString();
        if(TextUtils.isEmpty(status)) {
            Toast.makeText(StatusActivity.this, "Please write something...", Toast.LENGTH_SHORT).show();
            return ;
        }
        mProgressDialog.setTitle("Updating Status");
        mProgressDialog.setMessage("Please wait while status is updating..");
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();

        mDatabaseReference.child("status").setValue(status).addOnCompleteListener(task -> {
            mProgressDialog.dismiss();
            if(task.isSuccessful()) {
                Toast.makeText(StatusActivity.this, "Status Updated Successfully", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(StatusActivity.this, "Status cannot be updated", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
