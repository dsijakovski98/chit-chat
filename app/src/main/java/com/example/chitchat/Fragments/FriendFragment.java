package com.example.chitchat.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.ChatActivity;
import com.example.chitchat.ProfileActivity;
import com.example.chitchat.R;
import com.example.chitchat.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUsersDatabase;

    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mMainView = inflater.inflate(R.layout.fragment_friend, container, false);

        mFriendsList = mMainView.findViewById(R.id.friendRecycleList);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrent_user_id);
        mFriendDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(mUsersDatabase, Friend.class)
                        .build();

        FirebaseRecyclerAdapter<Friend, FriendViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendViewHolder friendViewHolder, int position, @NonNull Friend friend) {
                friendViewHolder.setDate((friend.getDate()));
                final String listUserId = getRef(position).getKey();

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Log.i("daniel", snapshot.child("name").toString());
                        final String userName = snapshot.child("name").getValue().toString();
                        String userThumbImage = snapshot.child("thumb_image").getValue().toString();

                        if(snapshot.hasChild("online")) {
                            String userOnline = snapshot.child("online").getValue().toString();
                            friendViewHolder.setOnline(userOnline);
                        }
                        friendViewHolder.setName(userName);
                        friendViewHolder.setUserImage(userThumbImage);

                        friendViewHolder.mView.setOnClickListener(v -> {
                            CharSequence[] options = new CharSequence[]{"Open Profile" , "Send Message"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Select Options");

                            builder.setItems(options, (dialog, which) -> {
                                if (which == 0) {
                                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                                    intent.putExtra("user_id", listUserId);
                                    startActivity(intent);
                                }

                                else if (which == 1) {
                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                    intent.putExtra("user_id", listUserId);
                                    intent.putExtra("user_name", userName);
                                    startActivity(intent);
                                }
                            });

                            builder.show();

                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycle_list_single_user, parent, false);

                return new FriendViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        mFriendsList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }
        public void setDate(String date) {
            TextView userNameView = mView.findViewById(R.id.textViewSingleListStatus);
            userNameView.setText(date);

        }
        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.textViewSingleListName);
            userNameView.setText(name);

        }
        public void setUserImage(String userThumbImage) {
            CircleImageView userImageview = mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(userThumbImage).placeholder(R.drawable.user_img).into(userImageview);
        }
        public void setOnline(String isOnline) {
            ImageView online = mView.findViewById(R.id.userSingleOnlineIcon);
            if(isOnline.equals("true")) {
                online.setVisibility(View.VISIBLE);
            }
            else {
                online.setVisibility(View.INVISIBLE);
            }
        }
    }
}
