package com.example.chitchat;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class ChitChat extends Application{

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        if(mUser != null) {

            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            mAuth = FirebaseAuth.getInstance();
            Picasso.Builder builder = new Picasso.Builder(this);
            builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);

           mUserDatabase = FirebaseDatabase.getInstance().
                   getReference().child("users").child(mAuth.getCurrentUser().getUid());
           mUserDatabase.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });




       }

    }



}
