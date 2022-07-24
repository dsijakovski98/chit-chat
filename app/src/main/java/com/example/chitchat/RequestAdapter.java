package com.example.chitchat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>{

    private final List<String> requestList;
    DatabaseReference mDatabaseReference ;

    private Context ctx;

    public RequestAdapter(List<String> requestList) {
        this.requestList = requestList;
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_list_single_user, parent, false);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        return new RequestAdapter.RequestViewHolder(view);
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        public TextView displayName;
        public TextView displayStatus;
        public CircleImageView displayImage;
        public ImageView imageView;

        public RequestViewHolder(View itemView) {
            super(itemView);

            ctx = itemView.getContext();

            displayName = itemView.findViewById(R.id.textViewSingleListName);
            displayStatus = itemView.findViewById(R.id.textViewSingleListStatus);
            displayImage = itemView.findViewById(R.id.circleImageViewUserImage);
            imageView = itemView.findViewById(R.id.userSingleOnlineIcon);
            imageView.setVisibility(View.INVISIBLE);
            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                Intent intent = new Intent(ctx, ProfileActivity.class);
                intent.putExtra("user_id", requestList.get(pos));
                ctx.startActivity(intent);
            });
        }
    }

    @Override
    public void onBindViewHolder(final RequestViewHolder holder, final int position) {

        String user_id = requestList.get(position);
        mDatabaseReference.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("name").getValue().toString();
                String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();

                holder.displayName.setText(userName);
                holder.displayStatus.setText(userStatus);
                Picasso.get().load(userThumbImage).placeholder(R.drawable.user_img).into(holder.displayImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

}
