package com.example.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendCount;
    private Button mProfileSendReqButton, mProfileDeclineReqButton;

    ProgressDialog mProgressDialog;
    private String mCurrent_state;

    DatabaseReference mFriendReqReference;
    DatabaseReference mDatabaseReference;
    DatabaseReference mFriendDatabase;
    DatabaseReference mNotificationReference;
    DatabaseReference mRootReference;
    DatabaseReference getDatabaseReference;

    FirebaseUser mFirebaseUser;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user_id = getIntent().getStringExtra("user_id");

        if(user_id == null) {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        mProfileImage = findViewById(R.id.profileUserImage);
        mProfileName = findViewById(R.id.profileUserName);
        mProfileStatus = findViewById(R.id.profileUserStatus);
        mProfileFriendCount = findViewById(R.id.profileUserFriends);
        mProfileSendReqButton = findViewById(R.id.profileSendReqButton);
        mProfileDeclineReqButton = findViewById(R.id.profileDeclineReqButton);

        mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
        mProfileDeclineReqButton.setEnabled(false);

        mFriendReqReference = FirebaseDatabase.getInstance().getReference().child("friend_request");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationReference = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        getDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseUser.getUid());

        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Fetching Details");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mCurrent_state = "not_friends"; // 4 types--- "not_friends" , "req_sent"  , "req_received" & "friends"

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                String display_image = dataSnapshot.child("image").getValue().toString();
                mProfileName.setText(display_name);
                mProfileStatus.setText(display_status);
                Picasso.get().load(display_image).placeholder(R.drawable.user_img).into(mProfileImage);

                mFriendDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long len = dataSnapshot.getChildrenCount();
                        mProfileFriendCount.setText("TOTAL FRIENDS : " + len);
                        mFriendReqReference.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(user_id)) {

                                    String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                                    if(request_type.equals("sent")) {

                                        mCurrent_state = "req_sent";
                                        mProfileSendReqButton.setText("Cancel Friend Request");
                                        mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                                        mProfileDeclineReqButton.setEnabled(false);

                                    }

                                    else if(request_type.equals("received")) {
                                        mCurrent_state = "req_received";
                                        mProfileSendReqButton.setText("Accept Friend Request");
                                        mProfileDeclineReqButton.setVisibility(View.VISIBLE);
                                        mProfileDeclineReqButton.setEnabled(true);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                else {
                                    mFriendDatabase.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                                            mProfileDeclineReqButton.setEnabled(false);

                                            if(dataSnapshot.hasChild(user_id)){
                                                mCurrent_state = "friends";
                                                mProfileSendReqButton.setText("Unfriend This Person");
                                            }
                                            mProgressDialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            mProgressDialog.dismiss();
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(ProfileActivity.this, "Error fetching Friend request data", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });

        mProfileSendReqButton.setOnClickListener(v -> {
            String mUserId= mFirebaseUser.getUid();

            if(mUserId.equals(user_id)) {
                Toast.makeText(ProfileActivity.this, "Cannot send request to your own", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.e("m_current_state is : ", mCurrent_state);
            mProfileSendReqButton.setEnabled(false);

            if(mCurrent_state.equals("not_friends")) {

                DatabaseReference newNotificationReference = mRootReference.child("notifications").child(user_id).push();

                String newNotificationId = newNotificationReference.getKey();

                HashMap<String, String> notificationData = new HashMap<String, String>();
                notificationData.put("from", mFirebaseUser.getUid());
                notificationData.put("type", "request");

                Map requestMap = new HashMap();
                requestMap.put("friend_request/" + mFirebaseUser.getUid() + "/" + user_id + "/request_type", "sent");
                requestMap.put("friend_request/" + user_id + "/" + mFirebaseUser.getUid() + "/request_type", "received");
                requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                mRootReference.updateChildren(requestMap, (databaseError, databaseReference) -> {
                    if(databaseError == null) {

                        Toast.makeText(ProfileActivity.this, "Friend Request sent successfully", Toast.LENGTH_SHORT).show();

                        mProfileSendReqButton.setEnabled(true);
                        mCurrent_state= "req_sent";
                        mProfileSendReqButton.setText("Cancel Friend Request");

                    }
                    else{
                        mProfileSendReqButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Some error in sending friend Request", Toast.LENGTH_SHORT).show();
                    }

                });
            }

            if(mCurrent_state.equals("req_sent")) {

                Map valueMap = new HashMap();
                valueMap.put("friend_request/" + mFirebaseUser.getUid() + "/" + user_id, null);
                valueMap.put("friend_request/" + user_id + "/" + mFirebaseUser.getUid(), null);

                mRootReference.updateChildren(valueMap, (databaseError, databaseReference) -> {
                    if(databaseError == null) {
                        mCurrent_state = "not_friends";
                        mProfileSendReqButton.setText("Send Friend Request");
                        mProfileSendReqButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Friend Request Cancelled Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mProfileSendReqButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Cannot cancel friend request...", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if(mCurrent_state.equals("req_received")) {
                Date current_date = new Date(System.currentTimeMillis());

                String date[] = current_date.toString().split(" ");
                final String today_date = (date[1] + " " + date[2] + ", " + date[date.length-1]+" "+date[3]);

                Map friendMap = new HashMap();
                friendMap.put("friends/" + mFirebaseUser.getUid() + "/" + user_id + "/date", today_date);
                friendMap.put("friends/" + user_id + "/" + mFirebaseUser.getUid() + "/date", today_date);

                friendMap.put("friend_request/" + mFirebaseUser.getUid() + "/" + user_id, null);
                friendMap.put("friend_request/" + user_id + "/" + mFirebaseUser.getUid(), null);

                mRootReference.updateChildren(friendMap, (databaseError, databaseReference) -> {

                    if(databaseError== null) {
                        mProfileSendReqButton.setEnabled(true);
                        mCurrent_state = "friends";
                        mProfileSendReqButton.setText("Unfriend this person");
                        mProfileDeclineReqButton.setEnabled(false);
                        mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                    }
                    else {
                        mProfileSendReqButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Error is " +databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }


            if(mCurrent_state.equals("friends")) {
                Map valueMap = new HashMap();
                valueMap.put("friends/" + mFirebaseUser.getUid() + "/" + user_id, null);
                valueMap.put("friends/" + user_id + "/" + mFirebaseUser.getUid(), null);

                mRootReference.updateChildren(valueMap, (databaseError, databaseReference) -> {
                    if(databaseError == null) {
                        mCurrent_state = "not_friends";
                        mProfileSendReqButton.setText("Send Friend Request");
                        mProfileSendReqButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Successfully Unfriended...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mProfileSendReqButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Cannot Unfriend... Sorry you're stuck with that person for life", Toast.LENGTH_SHORT).show();
                    }
                });


            }

        });


        mProfileDeclineReqButton.setOnClickListener(v -> {
            Map valueMap = new HashMap();
            valueMap.put("friend_request/" + mFirebaseUser.getUid() + "/" + user_id, null);
            valueMap.put("friend_request/" + user_id + "/" + mFirebaseUser.getUid(), null);

            mRootReference.updateChildren(valueMap, (databaseError, databaseReference) -> {
                if(databaseError == null) {

                    mCurrent_state = "not_friends";
                    mProfileSendReqButton.setText("Send Friend Request");
                    mProfileSendReqButton.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Friend Request Declined Successfully...", Toast.LENGTH_SHORT).show();

                    mProfileDeclineReqButton.setEnabled(false);
                    mProfileDeclineReqButton.setVisibility(View.INVISIBLE);
                }
                else{

                    mProfileSendReqButton.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Cannot decline friend request...", Toast.LENGTH_SHORT).show();

                }
            });


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
