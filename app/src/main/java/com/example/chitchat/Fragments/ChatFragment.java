package com.example.chitchat.Fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.ChatActivity;
import com.example.chitchat.Conv;
import com.example.chitchat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mMessageDatabase;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mMainView = inflater.inflate(R.layout.fragment_chat, container, false);

        mConvList = mMainView.findViewById(R.id.chatRecycleList);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("chats").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);

        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("time_stamp");
        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(conversationQuery, Conv.class)
                        .build();

        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ConvViewHolder convViewHolder, int position, @NonNull Conv conv) {
                final String listUserId = getRef(position).getKey();
                Query lastMessageQuery = mMessageDatabase.child(listUserId).limitToLast(1);

                mMessageDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        String data = snapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(data, conv.isSeen());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String userName = snapshot.child("name").getValue().toString();
                        String userThumb = snapshot.child("thumb_image").getValue().toString();

                        if (snapshot.hasChild("online")) {
                            String userOnline = snapshot.child("online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);
                        }

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recycle_list_single_user, parent, false);

                return new ConvViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        mConvList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);
            mView  = itemView;
        }

        public void setMessage(String message, boolean isSeen) {
            TextView userStatusView = mView.findViewById(R.id.textViewSingleListStatus);
            userStatusView.setText(message);

            if(isSeen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            }
            else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.textViewSingleListName);
            userNameView.setText(name);
        }


        public void setUserImage(String userThumb) {
            CircleImageView userImageView = mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(userThumb).placeholder(R.drawable.user_img).into(userImageView);
        }


        public void setUserOnline(String onlineStatus) {

            ImageView userOnlineView = mView.findViewById(R.id.userSingleOnlineIcon);
            if(onlineStatus.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }

}
