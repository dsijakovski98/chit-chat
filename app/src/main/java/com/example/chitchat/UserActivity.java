package com.example.chitchat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUsersList = findViewById(R.id.recyclerViewUsersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabaseReference.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(mUsersDatabaseReference, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, UserViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull UserViewHolder viewHolder, int position, @NonNull User user) {
                        Log.i("daniel", user.toString());
                        viewHolder.setName(user.getName());
                        viewHolder.setStatus(user.getStatus());
                        viewHolder.setImage(user.getThumbImage());
                        final String user_id = getRef(position).getKey();

                        viewHolder.mView.setOnClickListener(v -> {
                            Intent profileIntent = new Intent(UserActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("user_id", user_id);
                            startActivity(profileIntent);
                        });
                    }

                    @NonNull
                    @Override
                    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.recycle_list_single_user, parent, false);

                        return new UserViewHolder(view);
                    }
                };

        firebaseRecyclerAdapter.startListening();
        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            Log.i("daniel", "HERE " + name);
            TextView userNameView = mView.findViewById(R.id.textViewSingleListName);
            Log.i("daniel", userNameView.toString());
            userNameView.setText(name);
        }


        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.textViewSingleListStatus);
            userStatusView.setText(status);
        }

        public void setImage(String thumb_image) {
            CircleImageView userImageView = mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(thumb_image).placeholder(R.drawable.user_img).into(userImageView);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
