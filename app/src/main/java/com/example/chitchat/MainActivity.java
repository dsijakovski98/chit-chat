package com.example.chitchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chitchat.Fragments.MyFragmentPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ViewPager mViewPager;
    MyFragmentPagerAdapter mFragmentPagerAdapter;
    TabLayout mTabLayout;
    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mViewPager = findViewById(R.id.viewPager);

        mFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mFragmentPagerAdapter);

        mTabLayout = findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Really Exit ??");
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new MainActivity.MyListener());
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }
    public class MyListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) {
            startFn();
        }
        else {
            mDatabaseReference.child(user.getUid()).child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.allUsers) {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
        }

        if(item.getItemId() == R.id.logout) {
            mDatabaseReference.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    FirebaseAuth.getInstance().signOut();
                    startFn();
                }
                else {
                    Toast.makeText(MainActivity.this, "Try again..", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return true;
    }

    private void startFn() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
